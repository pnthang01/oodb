/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.IMessageListener;
import io.cluster.listener.NodeMessageListener;
import io.cluster.listener.ServerMessageListener;
import io.cluster.net.NIOAsyncServer;
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
        ServerMessageListener listener = new ServerMessageListener();
        server.addListener(Channel.SYSTEM_CHANNEL, listener);
        NodeMessageListener nodeListner = new NodeMessageListener();
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
