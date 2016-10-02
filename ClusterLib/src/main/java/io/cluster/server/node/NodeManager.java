/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.node;

import io.cluster.shared.core.IMessageListener;
import io.cluster.server.bean.NodeBean;
import io.cluster.util.StringUtil;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author thangpham
 */
public class NodeManager {

    private final ConcurrentMap<String, ConcurrentMap<String, NodeBean>> groupedNodeMap;
    private final ConcurrentMap<String, NodeBean> nodeMap;
    private MasterNode master;
    private static NodeManager _instance = new NodeManager();

    private NodeManager() {
        this.groupedNodeMap = new ConcurrentHashMap<>();
        this.nodeMap = new ConcurrentHashMap();
        _init();
    }

    public static void setMasterNode(MasterNode master) {
        _instance.master = master;
    }

    /**
     * Run a task will check all nodes are still alive or not per 3 seconds.
     */
    public void _init() {
        Timer timer = new Timer();
        TimerTask checkNodeAliveTask = new TimerTask() {
            @Override
            public void run() {
                for (Entry<String, NodeBean> nodeEntry : nodeMap.entrySet()) {
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
        //
    }

    /**
     * Check all node statues
     *
     * @return
     */
    public static String checkAllNodeStatus() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Entry<String, NodeBean> nodeEntry : _instance.nodeMap.entrySet()) {
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
    public static NodeBean addNode(String group, String host, int port) {
        String name = "Test name" + port;//TODO: auto generate node name
        NodeBean addedNode = addNode(group, name, host, port);
        System.out.println(String.format("Node %s:%d is added succesfully", host, port));
        return addedNode;
    }

    private static NodeBean addNode(String group, String name, String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        NodeBean node = new NodeBean(hashId, name, host, port);
        node.setGroup(group);
        ConcurrentMap<String, NodeBean> groupMap = _instance.groupedNodeMap.get(group);
        if (null == groupMap) {
            groupMap = new ConcurrentHashMap();
            ConcurrentMap<String, NodeBean> putIfAbsent = _instance.groupedNodeMap.putIfAbsent(group, groupMap);
            groupMap = putIfAbsent == null ? groupMap : putIfAbsent;
        }
        groupMap.putIfAbsent(hashId, node);
        _instance.nodeMap.putIfAbsent(hashId, node);
        //
        return node;
    }

    /**
     * Remove one node from cluster
     *
     * @param group
     * @param host
     * @param port
     */
    public static void removeNode(String group, String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        ConcurrentMap<String, NodeBean> nodeMap = _instance.groupedNodeMap.get(group);
        if (null != nodeMap) {
            nodeMap.remove(hashId);
        }
        _instance.nodeMap.remove(hashId);
    }

    public static void addListener(String channel, IMessageListener listener) {
        _instance.master.addListenner(channel, listener);
    }

    /**
     * Send message to all nodes
     *
     * @param message
     */
    public static void sendMessageToAllClient(String channel, String message) {
        _instance.master.sendMessageToAllClient(channel, message);
    }

    /**
     * Send message to all nodes in a group
     *
     * @param channel
     * @param group
     * @param message
     */
    public static void sendMessageToGroupClient(String channel, String group, String message) {
        ConcurrentMap<String, NodeBean> groupNode = _instance.groupedNodeMap.get(group);
        if (null == groupNode || groupNode.isEmpty()) {
            return;
        }
        for (Entry<String, NodeBean> entry : groupNode.entrySet()) {
            _instance.master.sendMessageToSingleClient(channel, entry.getKey(), message);
        }
    }

    /**
     * Send message to single node
     *
     * @param id
     * @param message
     */
    public static void sendMessageToSingleClient(String channel, String id, String message) {
        _instance.master.sendMessageToSingleClient(channel, id, message);
    }

    /**
     * Ping from one node to make the node isn't timeout
     *
     * @param host
     * @param port
     */
    public static void ping(String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        NodeBean node = _instance.nodeMap.get(hashId);
        if (null != node) {
            node.ping();
        }
    }

    /**
     * Get one node by there id
     *
     * @param id
     * @return
     */
    public static NodeBean getNodeById(String id) {
        return _instance.nodeMap.get(id);
    }

    /**
     * Get one node by there index, if you're don't truly know the index, use
     * checkNodeStatues() to display all nodes
     *
     * @param index
     * @return
     */
    @Deprecated
    public static NodeBean getNodeByIndex(int index) {
        int count = 0;
        for (Entry<String, NodeBean> entry : _instance.nodeMap.entrySet()) {
            if (count == index) {
                return entry.getValue();
            }
            ++count;
        }
        return null;
    }

}
