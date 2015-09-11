/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.net.NIOAsyncServer;

/**
 *
 * @author thangpham
 */
public class MasterNode {

    private NIOAsyncServer server;

    public MasterNode() {
        server = new NIOAsyncServer();
        server.start();
        NodeManager.setMasterNode(this);
    }

    public void sendMessageToAllClient(String message) {
        server.sendMessage(message);
    }

    public void sendMessageToSingleClient(String id, String message) {
        server.sendMessage(id, message);
    }
}
