/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.ClientMessageListener;
import io.cluster.listener.MessageListener;
import io.cluster.net.NIOAsyncClient;
import java.io.File;

/**
 *
 * @author thangpham
 */
public class WorkerNode {

    private NIOAsyncClient client;
    private static WorkerNode _instance;

    public WorkerNode(File configFile) {
        if (!configFile.exists() || !configFile.isFile()) {
            System.err.println("Couldn't find default config file.");
            System.exit(0);
        }
        client = new NIOAsyncClient(configFile);
        client.start();
        //
        ClientMessageListener listener = new ClientMessageListener();
        client.addListener("system", listener);
        //
    }

    public static void initialize(String configFileDes) {
        File defaultConfig = new File(configFileDes);
        _instance = new WorkerNode(defaultConfig);
    }

    public static void addListener(String channel, MessageListener listener) {
        if(null == channel) throw new NullPointerException("Channel cannot be null.");
        if(null == listener) throw new NullPointerException("Listener cannot be null.");
        _instance.client.addListener(channel, listener);
    }
    
    public static void sendRequest(String channel, String request) {
        _instance.client.sendRequest(channel, request);
    }

    public static void closeConnectionToServer() {
        _instance.client.close();
    }
}
