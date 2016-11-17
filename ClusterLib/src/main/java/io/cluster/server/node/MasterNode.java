/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.node;

import io.cluster.http.HttpLogServer;
import io.cluster.server.bean.NodeBean;
import io.cluster.shared.core.IMessageListener;
import io.cluster.server.listener.ServerHardwareMonitoringListener;
import io.cluster.server.listener.ServerTaskMessageListener;
import io.cluster.server.net.NIOAsyncServer;
import io.cluster.shared.core.AbstractGroupManager;
import io.cluster.util.Constants;
import io.cluster.util.Constants.Channel;
import io.cluster.util.StringUtil;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class MasterNode {

    private static final Logger LOGGER = LogManager.getLogger(MasterNode.class.getName());

    private final ConcurrentMap<String, ConcurrentMap<String, NodeBean>> groupedNodeMap;

    private final ConcurrentMap<String, AbstractGroupManager> managers;
    private final ConcurrentMap<String, NodeBean> nodeMap;

    private final NIOAsyncServer server;
    private static MasterNode _instance = null;

    private Lock nodeLocker = new ReentrantLock(true);
    private Condition newNodeCondition = nodeLocker.newCondition();

    private MasterNode() {
        System.setProperty("logFileName", "master-node");
        //
        server = new NIOAsyncServer();
        server.start();
        this.groupedNodeMap = new ConcurrentHashMap<>();
        this.nodeMap = new ConcurrentHashMap();
        this.managers = new ConcurrentHashMap<>();
    }

    /**
     * Run a task will check all nodes are still alive or not per 3 seconds.
     */
    public void _init() {
        //****** Have to read config to read channel 
        ServerHardwareMonitoringListener listener = new ServerHardwareMonitoringListener();
        server.addListener(Channel.SYSTEM_CHANNEL, listener);
        ServerTaskMessageListener nodeListner = new ServerTaskMessageListener();
        server.addListener(Channel.NODE_CHANNEL, nodeListner);
        CoordinatorNodeManager coorManager = CoordinatorNodeManager.load();
        managers.put(Constants.Group.COORDINATOR_GROUP, coorManager);
        //
        new HttpLogServer().start();
        //
        Timer timer = new Timer();
        TimerTask checkNodeAliveTask = new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<String, NodeBean> nodeEntry : nodeMap.entrySet()) {
                    NodeBean node = nodeEntry.getValue();
                    if (System.currentTimeMillis() - node.getLastPingTime() > 10000) {
                        node.setStatus(NodeBean.TIMEOUT);
                    }
                    if (System.currentTimeMillis() - node.getLastPingTime() > 60000) {
                        node.setStatus(NodeBean.DIED);
                    }
                }
            }
        };
        timer.schedule(checkNodeAliveTask, 0, 3000);//Check node is dead per seconds
    }

    public synchronized static MasterNode load() {
        if (null == _instance) {
            _instance = new MasterNode();
            _instance._init();
        }
        return _instance;
    }

    public void addListener(String channel, IMessageListener listenner) {
        server.addListener(channel, listenner);
    }

    /**
     * Send message to all clients
     *
     * @param channel
     * @param message
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public void sendMessageToAllClient(String channel, String message) throws InterruptedException, ExecutionException {
        server.sendMessageToAllClient(channel, message);
    }

    /**
     * Send message to single client base on its id.
     *
     * @param id
     * @param channel
     * @param message
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean sendMessageToSingleClient(String id, String channel, String message) throws InterruptedException, ExecutionException {
        if (nodeMap.containsKey(id)) {
            server.sendMessageToSingleClient(id, channel, message);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendMessageToSingleClient(String host, int port, String channel, String message) throws InterruptedException, ExecutionException {
        String hashId = StringUtil.getHashAddress(host, port);
        return sendMessageToSingleClient(hashId, channel, message);
    }

    /**
     * Send message to all clients in a group
     *
     * @param channel
     * @param group
     * @param message
     */
    public void sendMessageToGroupClient(String channel, String group, String message) throws InterruptedException, ExecutionException {
        ConcurrentMap<String, NodeBean> groupNode = groupedNodeMap.get(group);
        if (null == groupNode || groupNode.isEmpty()) {
            return;
        }
        for (Map.Entry<String, NodeBean> entry : groupNode.entrySet()) {
            sendMessageToSingleClient(channel, entry.getKey(), message);
        }
    }

    /**
     * Check all node statues
     *
     * @return
     */
    public String checkAllNodeStatus() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Map.Entry<String, NodeBean> nodeEntry : nodeMap.entrySet()) {
            sb.append("\n").append(index).append(".").append(nodeEntry.getValue().toString());
        }
        //
        return sb.toString();
    }

    /**
     * Add one node to cluster.
     *
     * @param group
     * @param host
     * @param port
     * @return
     */
    public NodeBean addNode(String group, String host, int port) {
        String name = "Test name" + port;//TODO: auto generate node name
        NodeBean addedNode = addNode(group, name, host, port);
        LOGGER.info(String.format("Node %s:%d is added succesfully", host, port));
        return addedNode;
    }

    private AtomicReference<NodeBean> newNode = new AtomicReference<>();

    /**
     * Add one node to cluster.
     *
     * @param group
     * @param name
     * @param host
     * @param port
     * @return
     */
    public NodeBean addNode(String group, String name, String host, int port) {
        NodeBean node = null;
        nodeLocker.lock();
        try {
            String hashId = StringUtil.getHashAddress(host, port);
            node = new NodeBean(hashId, name, host, port);
            node.setGroup(group);
            ConcurrentMap<String, NodeBean> groupMap = groupedNodeMap.get(group);
            if (null == groupMap) {
                groupMap = new ConcurrentHashMap();
                ConcurrentMap<String, NodeBean> putIfAbsent = groupedNodeMap.putIfAbsent(group, groupMap);
                groupMap = putIfAbsent == null ? groupMap : putIfAbsent;
            }
            groupMap.putIfAbsent(hashId, node);
            nodeMap.putIfAbsent(hashId, node);
            //
            newNode.set(node);
            newNodeCondition.signalAll();
        } catch (Exception ex) {
            LOGGER.error("Could not add new node to cluster, error: ", ex);
        } finally {
            nodeLocker.unlock();
            return node;
        }
    }

    public NodeBean waitingForNewNode() {
        NodeBean returnNode = null;
        nodeLocker.lock();
        try {
            while (newNode.get() == null) {
                newNodeCondition.await();
            }
            returnNode = newNode.get();
            newNode.set(null);
        } catch (Exception ex) {
            LOGGER.error("Could not wait for new node to cluster, error: ", ex);
        } finally {
            nodeLocker.unlock();
            return returnNode;
        }
    }

    /**
     * Remove one node from cluster
     *
     * @param group
     * @param host
     * @param port
     */
    public void removeNode(String group, String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        ConcurrentMap<String, NodeBean> nodeMap = groupedNodeMap.get(group);
        if (null != nodeMap) {
            nodeMap.remove(hashId);
        }
        _instance.nodeMap.remove(hashId);
    }

    /**
     * Ping from one node to make the node isn't timeout
     *
     * @param host
     * @param port
     */
    public void ping(String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        NodeBean node = nodeMap.get(hashId);
        if (null != node) {
            node.ping();
        }
    }

    /**
     * Get one node by host and port
     *
     * @param host
     * @param port
     * @return
     */
    public NodeBean getNodeByHostPort(String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        return nodeMap.get(hashId);
    }

    /**
     * Get one node by there id
     *
     * @param id
     * @return
     */
    public NodeBean getNodeById(String id) {
        return nodeMap.get(id);
    }

    /**
     * Get one node by there index, if you're don't truly know the index, use
     * checkNodeStatues() to display all nodes
     *
     * @param index
     * @return
     */
    @Deprecated
    public NodeBean getNodeByIndex(int index) {
        int count = 0;
        for (Map.Entry<String, NodeBean> entry : nodeMap.entrySet()) {
            if (count == index) {
                return entry.getValue();
            }
            ++count;
        }
        return null;
    }
}
