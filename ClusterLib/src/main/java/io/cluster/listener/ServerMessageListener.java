/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.node.bean.NodeBean;
import io.cluster.net.bean.NetBean;
import io.cluster.net.bean.RequestBean;
import io.cluster.node.NodeManager;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author thangpham
 */
public class ServerMessageListener implements MessageListener {

    private ServerMessageInterceptor interceptor;

    public ServerMessageListener() {
        interceptor = new ServerMessageInterceptor();
        interceptor.start();
    }

    @Override
    public String onMessage(NetBean request) {
        if (request == null || !(request instanceof RequestBean)) {
            System.err.println("The request is in wrong format or be null on message, cannot process.");
        }
        RequestBean requestBean = (RequestBean) request;
        if (requestBean.getMessage() != null && requestBean.getMessage().length <= 0) {
            System.err.println("Request from client is empty or null, cannot process.");
            return null;
        }
        interceptor.addRequest(requestBean);
        return null;
    }

    @Override
    public String onChannel(NetBean request) {
        if (request == null || !(request instanceof RequestBean)) {
            System.err.println("Request from client is empty or null on channel, cannot process.");
            return null;
        }
        RequestBean requestBean = (RequestBean) request;
        String address = requestBean.getAddress().toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        NodeBean addedNode = NodeManager.addNode(tmp[0], Integer.valueOf(tmp[1]));
        return addedNode.getId();
    }

    public class ServerMessageInterceptor extends Thread {

        private ConcurrentLinkedQueue<RequestBean> requestQueue;
        private Lock lock;
        private Condition newElement;

        public ServerMessageInterceptor() {
            lock = new ReentrantLock();
            newElement = lock.newCondition();
            requestQueue = new ConcurrentLinkedQueue();
        }

        public void run() {
            while (true) {
                lock.lock();
                try {
                    while (requestQueue.isEmpty()) {
                        newElement.await();
                    }
                    RequestBean request = requestQueue.poll();
                    //************************** Process your request here
                    if (request.getMessage().length == 1) {
                        NodeManager.ping(request.getHost(), request.getPort());
                    }
                    String message = request.getMessageAsString();
                    //
                    //////////////CAST message to task to do
                    //
                    System.out.println("Interceptor process request: " + request.getHost());
                    //**************************
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void addRequest(RequestBean request) {
            lock.lock();
            try {
                requestQueue.add(request);
                newElement.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
