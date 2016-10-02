/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.node;

import io.cluster.client.listener.ClientMessageListener;
import io.cluster.shared.core.IMessageListener;
import io.cluster.server.listener.ServerMessageListener;
import io.cluster.client.net.NIOAsyncClient;
import io.cluster.client.scheduler.MonitorHardwareClient;
import io.cluster.util.Constants;
import io.cluster.util.Constants.Channel;
import io.cluster.util.ShutdownHookCleanUp;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
public class WorkerNode {

    private final NIOAsyncClient client;
    private static WorkerNode _instance;
    private ScheduledExecutorService executor;
    
    public WorkerNode() {
        client = new NIOAsyncClient();
        client.start();
        
        //Background scheduled
        executor = Executors.newScheduledThreadPool(3);
        ShutdownHookCleanUp.addExecutor(executor);
        MonitorHardwareClient monitorHardware = new MonitorHardwareClient();
        executor.scheduleWithFixedDelay(monitorHardware, 5, 5, TimeUnit.SECONDS);
        //
        ClientMessageListener listener = new ClientMessageListener();
        client.addListener(Channel.SYSTEM_CHANNEL, listener);
    }
    
    public static void stop() {
        
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
