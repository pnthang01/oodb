/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.listener.MessageListener;
import io.cluster.listener.ServerMessageListener;
import io.cluster.net.NIOAsyncServer;
import java.io.File;

/**
 *
 * @author thangpham
 */
public class MasterNode {

    private NIOAsyncServer server;

    public MasterNode() {
        System.out.println("No config file, system will use default config file at config/NIOServerConfig.txt");
        File defaultConfig = new File("config/NIOServerConfig.txt");
        init(defaultConfig);
    }

    public MasterNode(File configFile) {

        init(configFile);
    }

    private void init(File configFile) {
        if (!configFile.exists() || !configFile.isFile()) {
            System.err.println("Couldn't find default config file.");
            System.exit(0);
        }
        server = new NIOAsyncServer(configFile);
        server.start();
        ServerMessageListener listener = new ServerMessageListener();
        server.addListener("system", listener);
        NodeManager.setMasterNode(this);
    }

    public void addListenner(String channel, MessageListener listenner) {
        server.addListener(channel, listenner);
    }

    public void sendMessageToAllClient(String channel, String message) {
        server.sendMessageToAllBean(channel, message);
    }

    public void sendMessageToSingleClient(String id, String channel, String message) {
        server.sendMessageToSingleBean(id, channel, message);
    }
}
