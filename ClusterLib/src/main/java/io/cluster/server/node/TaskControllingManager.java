/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.node;

import io.cluster.server.bean.NodeBean;
import io.cluster.server.bean.TaskBean;
import io.cluster.server.listener.ServerTaskMessageListener;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.ThreadPoolUtil;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class TaskControllingManager {

    private static final Logger LOGGER = LogManager.getLogger(TaskControllingManager.class.getName());

    private static TaskControllingManager _instance;

    private MasterNode masterNode;
    private CoordinatorNodeManager coorManager;
    private Queue<TaskBean> taskQueue;

    private Lock taskLocker = new ReentrantLock(true);
    private Condition hasTaskCondition = taskLocker.newCondition();
    private AtomicBoolean hasTask = new AtomicBoolean();

    static {
        _instance = new TaskControllingManager();
    }

    public static TaskControllingManager load() {
        return _instance;
    }

    public TaskControllingManager() {
        masterNode = MasterNode.load();
        ServerTaskMessageListener taskListener = new ServerTaskMessageListener();
        masterNode.addListener(Constants.Channel.NODE_CHANNEL, taskListener);
        coorManager = CoordinatorNodeManager.load();
        taskQueue = new ConcurrentLinkedQueue<TaskBean>();
        run();
    }

    public boolean assignTaskToClient(String host, int port, Map<String, String> sendingMessage) throws InterruptedException, ExecutionException {
        masterNode.sendMessageToSingleClient(host, port, Constants.Channel.NODE_CHANNEL, MethodUtil.toJson(sendingMessage));
        return true;
    }

    public boolean addTask(TaskBean task) {
        taskLocker.lock();
        boolean res = false;
        try {
            taskQueue.add(task);
            hasTask.set(true);
            hasTaskCondition.signalAll();
        } catch (Exception ex) {
            LOGGER.error("Could not add new task, error", ex);
        } finally {
            taskLocker.unlock();
            return res;
        }
    }

    private void run() {
        ThreadPoolUtil threadpool = ThreadPoolUtil.load();
        threadpool.addThread(new Runnable() {
            @Override
            public void run() {
                TaskBean task = null;
                try {
                    while (true) {
                        while (taskQueue.isEmpty()) {
                            hasTaskCondition.await();
                        }
                        task = taskQueue.poll();
                        if (task.getSubmitedTimed() + task.getDelay() >= System.currentTimeMillis()) {
                            String host;
                            int port;
                            for (Map<String, String> inst : task.getInstructions()) {
                                if (task.isOnNewProcess()) {
                                    boolean res = false;
                                    if (task.getHost() != null && task.getPort() != 0) {
                                        res = coorManager.createNewNode(task.getHost(), task.getPort());
                                    } else {
                                        res = coorManager.createNewNode();
                                    }
                                    if (!res) {
                                        LOGGER.error("Could not create new node, consider wrong coordinator.");
                                        break;
                                    }
                                    NodeBean newNode = masterNode.waitingForNewNode();
                                    host = newNode.getHost();
                                    port = newNode.getPort();
                                } else {
                                    host = task.getHost();
                                    port = task.getPort();
                                }
                                assignTaskToClient(host, port, inst);
                                LOGGER.info("Assign task to node: " + host + ":" + port + " with detail: " + MethodUtil.toJson(task));
                                if (task.getPeriod() > 0) {
                                    task.setSubmitedTimed(System.currentTimeMillis());
                                    task.setDelay(task.getPeriod());
                                }
                            }
                        } else {
                            taskQueue.add(task);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("Could not assign task: " + MethodUtil.toJson(task), ex);
                }
            }
        }, 3, 0, TimeUnit.SECONDS);
    }
}
