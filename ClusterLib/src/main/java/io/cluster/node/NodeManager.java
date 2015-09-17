/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.MessageListener;
import io.cluster.node.bean.NodeBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thangpham
 */
public class NodeManager {

    private Map<String, NodeBean> clientNodeMap;
    private MasterNode master;
    private static NodeManager _instance = new NodeManager();

    private NodeManager() {
        this.clientNodeMap = new HashMap<>();
        _init();
    }

    public static void setMasterNode(MasterNode master) {
        _instance.master = master;
    }

    /**
     * Run a task will check all nodes are still alive or not per 4 seconds.
     */
    public void _init() {
        Timer timer = new Timer();
        TimerTask checkNodeAliveTask = new TimerTask() {
            @Override
            public void run() {
                for (String nodeId : clientNodeMap.keySet()) {
                    NodeBean node = clientNodeMap.get(nodeId);
                    if (System.currentTimeMillis() - node.getLastPingTime() > 10000) {
                        node.setStatus(0);
                    }
                    if (System.currentTimeMillis() - node.getLastPingTime() > 60000) {
                        clientNodeMap.remove(nodeId);
                    }
                }
            }
        };
        timer.schedule(checkNodeAliveTask, 0, 4000);//Check node is dead per seconds
        //
    }

    /**
     * Check all node statues
     *
     * @return
     */
    public static String checkNodeStatuses() {
        StringBuilder sb = new StringBuilder();
        synchronized (_instance.clientNodeMap) {
            int index = 1;
            for (Entry<String, NodeBean> entry : _instance.clientNodeMap.entrySet()) {
                sb.append("\n").append(index).append(". ").append(entry.getValue().toString());
            }
        }
        //
        return sb.toString();
    }

    /**
     * Add one node to cluster.
     *
     * @param host
     * @param port
     * @return
     */
    public static NodeBean addNode(String host, int port) {
        String hashId = getHashId(host, port);
        NodeBean node = new NodeBean(hashId, "Test name" + port, host, port);
        synchronized (_instance.clientNodeMap) {
            _instance.clientNodeMap.put(hashId, node);
        }
        //
        return node;
    }

    /**
     * Remove one node from cluster
     *
     * @param host
     * @param port
     */
    public static void removeNode(String host, int port) {
        String hashId = getHashId(host, port);
        synchronized (_instance.clientNodeMap) {
            _instance.clientNodeMap.remove(hashId);
        }
    }

    public static void addListener(String channel, MessageListener listener) {
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
     * Send message to single node
     *
     * @param id
     * @param message
     */
    public static void sendMessageToSingleClient(String id, String channel, String message) {
        _instance.master.sendMessageToSingleClient(id, channel, message);
    }

    /**
     * Ping from one node to make the node isn't timeout
     *
     * @param host
     * @param port
     */
    public static void ping(String host, int port) {
        String hashId = getHashId(host, port);
        NodeBean node = _instance.clientNodeMap.get(hashId);
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
        return _instance.clientNodeMap.get(id);
    }

    /**
     * Get one node by there index, if you're don't truly know the index, use
     * checkNodeStatues() to display all nodes
     *
     * @param index
     * @return
     */
    public static NodeBean getNodeByIndex(int index) {
        int count = 0;
        for (Entry<String, NodeBean> entry : _instance.clientNodeMap.entrySet()) {
            if (count == index) {
                return entry.getValue();
            }
            ++count;
        }
        return null;
    }

    /**
     * Get nodeId by host and port
     *
     * @param host
     * @param port
     * @return
     */
    private static String getHashId(String host, int port) {
        return host + ":" + port;
    }
}
