/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.ClientMessageListener;
import io.cluster.net.NIOAsyncClient;

/**
 *
 * @author thangpham
 */
public class WorkerNode {
    
    private NIOAsyncClient client;
    private static WorkerNode _instance;
    
    private WorkerNode(){
        client = new NIOAsyncClient();
        client.start();
        //
        ClientMessageListener listener = new ClientMessageListener();
        client.addListener(listener);
        //
    }
    
    public static void initialize() {
        _instance = new WorkerNode();
    }
    
    public static void sendRequest(String request) {
        _instance.client.sendRequest(request);
    }
    
    public static void closeConnectionToServer() {
        _instance.client.close();
    }
}
