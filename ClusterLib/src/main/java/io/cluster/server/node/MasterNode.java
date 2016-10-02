/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.node;

import io.cluster.shared.core.IMessageListener;
import io.cluster.server.listener.ServerMessageListener;
import io.cluster.server.listener.NodeMonitoringListener;
import io.cluster.server.net.NIOAsyncServer;
import io.cluster.util.Constants;
import io.cluster.util.Constants.Channel;

/**
 *
 * @author thangpham
 */
public class MasterNode {

    private NIOAsyncServer server;
    private static MasterNode _instance;

    public MasterNode() {
        server = new NIOAsyncServer();
        server.start();
        //****** Have to read config to read channel 
        NodeMonitoringListener listener = new NodeMonitoringListener();
        server.addListener(Channel.SYSTEM_CHANNEL, listener);
        ServerMessageListener nodeListner = new ServerMessageListener();
        server.addListener(Channel.NODE_CHANNEL, nodeListner);
        NodeManager.setMasterNode(this);
    }

    public static void initialize(String configFileDes) {
        if (null == configFileDes || configFileDes.isEmpty()) {
            System.out.println("No config file, system will use default config file at config");
        } else {
            Constants.setBaseConfigFolder(configFileDes);
        }
        _instance = new MasterNode();
    }

    public static void addListenner(String channel, IMessageListener listenner) {
        _instance.server.addListener(channel, listenner);
    }

    public static void sendMessageToAllClient(String channel, String message) {
        _instance.server.sendMessageToAllBean(channel, message);
    }

    public static void sendMessageToSingleClient(String id, String channel, String message) {
        _instance.server.sendMessageToSingleBean(id, channel, message);
    }
}
