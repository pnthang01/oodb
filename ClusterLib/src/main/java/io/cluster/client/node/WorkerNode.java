/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.node;

import io.cluster.client.listener.JobClientMessageListener;
import io.cluster.shared.core.IMessageListener;
import io.cluster.client.net.NIOAsyncClient;
import io.cluster.client.scheduler.MonitorHardwareClient;
import io.cluster.util.Constants.Channel;
import io.cluster.util.ShutdownHookCleanUp;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
public class WorkerNode {

    private NIOAsyncClient client;
    private static WorkerNode _instance;
    private ScheduledExecutorService executor;
    private final ConcurrentMap<String, List<IMessageListener>> channelListeners;

    public WorkerNode() {
        channelListeners = new ConcurrentHashMap<>();
        client = new NIOAsyncClient(channelListeners);
        client.start();
    }

    public synchronized static WorkerNode load() {
        if (null == _instance) {
            _instance = new WorkerNode();
            _instance._init();
        }
        return _instance;
    }

    public void start() {

    }

    public void signalStart(Map<String, String> configs) {

    }

    public void signalStop() {

    }

    private void _init() {
        //Background scheduled
        executor = Executors.newScheduledThreadPool(3);
        ShutdownHookCleanUp.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit("WorkerNode", executor));
        MonitorHardwareClient monitorHardware = new MonitorHardwareClient();
        executor.scheduleWithFixedDelay(monitorHardware, 5, 5, TimeUnit.SECONDS);
        //
        JobClientMessageListener listener = new JobClientMessageListener();
        addListener(Channel.SYSTEM_CHANNEL, listener);
        //Wait if the connection to server is terminated or self closed, then restart it.
        Timer timer = new Timer();
        TimerTask checkNodeAliveTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    client.waitForSelfClose();
                    System.err.println("Lose connection to server due to internet or error occured. Will restart...");
                    client = new NIOAsyncClient(channelListeners);
                    client.start();
                } catch (Exception ex) {
                    System.err.println("Cannot restart it, error happended.");
                    ex.printStackTrace();
                }
            }
        };
        timer.schedule(checkNodeAliveTask, 0, 3000);//Check node is dead per seconds
    }

    public void addListener(String channel, IMessageListener listener) {
        if (null == channel) {
            throw new NullPointerException("Channel cannot be null.");
        }
        if (null == listener) {
            throw new NullPointerException("Listener cannot be null.");
        }
        List<IMessageListener> listeners = channelListeners.get(channel);
        if (null == listeners) {
            listeners = new ArrayList<>();
            List<IMessageListener> checkList = channelListeners.putIfAbsent(channel, listeners);
            listeners = checkList == null ? listeners : checkList;
        }
        listeners.add(listener);
    }

    public void sendRequest(String channel, String request) throws InterruptedException, ExecutionException {
        client.sendRequest(channel, request);
    }

    public void closeConnectionToServer() {
        client.close();
    }
    
    public SocketAddress getLocalAddress() throws IOException {
        return client.getLocalAddress();
    }
}
