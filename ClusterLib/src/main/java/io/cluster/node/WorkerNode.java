/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.ClientMessageListener;
import io.cluster.listener.IMessageListener;
import io.cluster.listener.NodeMessageListener;
import io.cluster.net.NIOAsyncClient;
import io.cluster.util.Constants;
import io.cluster.util.Constants.Channel;

/**
 *
 * @author thangpham
 */
public class WorkerNode {

    private final NIOAsyncClient client;
    private static WorkerNode _instance;

    public WorkerNode() {
        client = new NIOAsyncClient();
        client.start();
        //
        ClientMessageListener listener = new ClientMessageListener();
        client.addListener(Channel.SYSTEM_CHANNEL, listener);
        NodeMessageListener nodeListener = new NodeMessageListener();
        client.addListener(Channel.NODE_CHANNEL, nodeListener);
    }

    public static void initialize(String configFileDes) {
        if (null == configFileDes || configFileDes.isEmpty()) {
            System.out.println("No config file, system will use default config file at config");
        } else {
            Constants.setBaseConfigFolder(configFileDes);
        }
        _instance = new WorkerNode();
    }

    public static void addListener(String channel, IMessageListener listener) {
        if (null == channel) {
            throw new NullPointerException("Channel cannot be null.");
        }
        if (null == listener) {
            throw new NullPointerException("Listener cannot be null.");
        }
        _instance.client.addListener(channel, listener);
    }

    public static void sendRequest(String channel, String request) {
        _instance.client.sendRequest(channel, request);
    }

    public static void closeConnectionToServer() {
        _instance.client.close();
    }
}
