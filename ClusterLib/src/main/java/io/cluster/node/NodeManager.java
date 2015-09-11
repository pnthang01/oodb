/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

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

    public void _init() {
        Timer timer = new Timer();
        TimerTask checkNodeAliveTask = new TimerTask() {
            @Override
            public void run() {
                for (Entry<String, NodeBean> entry : clientNodeMap.entrySet()) {
                    NodeBean node = entry.getValue();
                    if (System.currentTimeMillis() - node.getLastPingTime() > 10000) {
                        node.setStatus(0);
                    }
                    if (System.currentTimeMillis() - node.getLastPingTime() > 60000) {
                        node.setStatus(-1);
                    }
                }
            }
        };
        timer.schedule(checkNodeAliveTask, 4000);//Check node is dead per seconds
        //
    }

    public static String checkNodeStatuses() {
        StringBuilder sb = new StringBuilder();
        synchronized (_instance.clientNodeMap) {
            for (Entry<String, NodeBean> entry : _instance.clientNodeMap.entrySet()) {
                sb.append("\n").append(entry.getValue().toString());
            }
        }
        //
        return sb.toString();
    }

    public static NodeBean addNode(String host, int port) {
        String hashId = getHashId(host, port);
        NodeBean node = new NodeBean(hashId, "Test name" + port, host, port);
        synchronized (_instance.clientNodeMap) {
            _instance.clientNodeMap.put(hashId, node);
        }
        //
        return node;
    }

    public static void ping(String host, int port) {
        String hashId = getHashId(host, port);
        _instance.clientNodeMap.get(hashId).ping();
    }

    public static void removeNode(String host, int port) {
        String hashId = getHashId(host, port);
        synchronized (_instance.clientNodeMap) {
            _instance.clientNodeMap.remove(hashId);
        }
    }

    public static NodeBean getNodeByIndex(int index) {
        int count = 0;
        for (Entry<String, NodeBean> entry : _instance.clientNodeMap.entrySet()) {
            if(count == index) return entry.getValue();
            ++count;
        }
        return null;
    }

    private static String getHashId(String host, int port) {
        return host + ":" + port;
    }
}
