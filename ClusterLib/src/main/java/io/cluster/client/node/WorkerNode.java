/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.node;

import io.cluster.shared.core.IMessageListener;
import io.cluster.client.net.NIOAsyncClient;
import io.cluster.util.Constants.Channel;
import io.cluster.util.ShutdownHookCleanUp;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author thangpham
 */
public class WorkerNode {

    private NIOAsyncClient client;
    private static WorkerNode _instance;
//    private ScheduledExecutorService executor;

    private final ConcurrentMap<String, List<IMessageListener>> channelListeners;
    private ShutdownHookCleanUp shutdownHook = ShutdownHookCleanUp.load();
    private boolean shouldConnect;

    public WorkerNode(boolean shouldConnect) {
        this.shouldConnect = shouldConnect;
        channelListeners = new ConcurrentHashMap<>();
        if (shouldConnect) {
            client = new NIOAsyncClient(channelListeners);
            client.start();
            //Wait if the connection to server is terminated or self closed, then restart it.
            Timer timer = new Timer();
            TimerTask checkNodeAliveTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        client.waitForSelfClose();
                        System.err.println("Lose connection to server due to internet or error occured. Will restart...");
                        client = new NIOAsyncClient(channelListeners);
                        client.start();
                    } catch (Exception ex) {
                        System.err.println("Cannot restart it, error happended.");
                        ex.printStackTrace();
                    }
                }
            };
            timer.schedule(checkNodeAliveTask, 0, 3000);//Check node is dead per seconds
        }
        //Background scheduled
//        executor = Executors.newScheduledThreadPool(3);
//        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit("WorkerNode", executor));
    }

    /**
     * Use once and when starting application. Be carefully when call this
     * method again, will cause unthread-safe problem.
     *
     * @param shouldConnect
     * @return
     */
    public synchronized static WorkerNode initialize(boolean shouldConnect) {
        if (null == _instance) {
            _instance = new WorkerNode(shouldConnect);
        }
        return _instance;
    }

    /**
     * Use when retrieve a WorkerNode instance. Will connect to the master node.
     *
     * @return
     */
    public synchronized static WorkerNode initialize() {
        if (null == _instance) {
            _instance = new WorkerNode(false);
        }
        return _instance;
    }

    public void start() {

    }

    public void signalStart(Map<String, String> configs) {

    }

    public void signalStop() {

    }

    public void setShouldConnect(boolean shouldConnect) {
        this.shouldConnect = shouldConnect;
    }

    public boolean isShouldConnect() {
        return shouldConnect;
    }

    /**
     * Add an instance of @IMessageListener to listen to server's responses.
     * This instance will belong to @channel
     *
     * @param channel
     * @param listener
     */
    public void addListener(String channel, IMessageListener listener) {
        if (null == channel) {
            throw new NullPointerException("Channel cannot be null.");
        }
        if (null == listener) {
            throw new NullPointerException("Listener cannot be null.");
        }
        List<IMessageListener> listeners = channelListeners.get(channel);
        if (null == listeners) {
            listeners = new ArrayList<>();
            List<IMessageListener> checkList = channelListeners.putIfAbsent(channel, listeners);
            listeners = checkList == null ? listeners : checkList;
        }
        listeners.add(listener);
    }

    public void sendRequest(String channel, String request) throws InterruptedException, ExecutionException {
        if (shouldConnect) {
            client.sendRequest(channel, request);
        }
    }

    public void closeConnectionToServer() {
        client.close();
    }

    public SocketAddress getLocalAddress() throws IOException {
        return client.getLocalAddress();
    }
}
