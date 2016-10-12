/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.net;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.util.Constants.Channel;
import io.cluster.util.ServerConfigAutoLoader;
import io.cluster.util.StringUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author thangpham
 */
public class NIOAsyncServer extends Thread {

    private AsynchronousServerSocketChannel asynSvr;
    private final ConcurrentMap<String, List<IMessageListener>> channelListeners;
    private final ConcurrentMap<String, AsyncSocketBean> beanList;
    private int SIZE;
    private boolean isRunning = true;

    public NIOAsyncServer() {
        System.out.println("The server is about to start...");
        channelListeners = new ConcurrentHashMap<>();
        beanList = new ConcurrentHashMap<>();
        try {
            SIZE = Integer.parseInt(ServerConfigAutoLoader.getConfigByName("SIZE"));
            String host = ServerConfigAutoLoader.getConfigByName("HOST");
            int port = Integer.parseInt(ServerConfigAutoLoader.getConfigByName("PORT"));
            asynSvr = AsynchronousServerSocketChannel.open();
            asynSvr.bind(new InetSocketAddress(host, port));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("The server started successfully.");
    }

    public void shutdown() {
        try {
            isRunning = false;
            asynSvr.close();
            System.out.println("Server is shutdown...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(String channel, IMessageListener listener) {
        List<IMessageListener> listeners = channelListeners.get(channel);
        if (null == listeners) {
            listeners = new ArrayList<>();
            List<IMessageListener> checkList = channelListeners.putIfAbsent(channel, listeners);
            listeners = checkList == null ? listeners : checkList;
        }
        listeners.add(listener);
    }

    public void sendMessageToSingleBean(String channel, String id, String message) throws InterruptedException, ExecutionException {
        for (Entry<String, AsyncSocketBean> entry : beanList.entrySet()) {
            if (entry.getKey().equals(id)) {
                entry.getValue().sendResponse(channel, message);
                break;
            }
        }
    }

    public void sendMessageToAllBean(String channel, String message) throws InterruptedException, ExecutionException {
        for (AsyncSocketBean bean : beanList.values()) {
            try {
                bean.sendResponse(channel, message);
            } catch (Exception ex) {
                try {
                    ex.printStackTrace();
                    System.err.println("Cannot send message to client: " + bean.soc.getRemoteAddress().toString());
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                try {
                    AsyncSocketBean bean = new AsyncSocketBean(asynSvr.accept().get(), channelListeners);
                    beanList.put(bean.getClientId(), bean);
                    bean.start();
                } catch (Exception ex) {
                    System.err.println("Cannot accept new client, error " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AsyncSocketBean extends Thread {

        private String clientId;
        private final AsynchronousSocketChannel soc;
        private final ConcurrentMap<String, List<IMessageListener>> channelListeners;

        public AsyncSocketBean(AsynchronousSocketChannel soc, ConcurrentMap<String, List<IMessageListener>> channelListeners) throws IOException {
            this.soc = soc;
            this.channelListeners = channelListeners;
            String[] parseAddress = StringUtil.parseAddress(soc.getRemoteAddress());
            this.clientId = StringUtil.getHashAddress(parseAddress[0], Integer.parseInt(parseAddress[1]));
            for (Entry<String, List<IMessageListener>> entry : channelListeners.entrySet()) {
                for (IMessageListener listener : entry.getValue()) {
                    try {
                        listener.onChannel(new RequestNetBean(soc.getRemoteAddress(), null));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                readRequest();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    soc.shutdownInput();
                    soc.shutdownOutput();
                    soc.close();
                } catch (IOException ex) {
                }
            }
        }

        /**
         * Send a message to client by channel
         *
         * @param channel
         * @param response
         */
        public void sendResponse(String channel, String response) {
            if (null == channel || null == response) {
                throw new IllegalArgumentException("Either channel or response message is null.");
            }
            try {
                byte[] finalBytes = new byte[32 + SIZE];
                System.arraycopy(channel.getBytes(), 0, finalBytes, 0, channel.length());
                System.arraycopy(response.getBytes(), 0, finalBytes, 32, response.length());
                soc.write(ByteBuffer.wrap(finalBytes)).get();
            } catch (InterruptedException | ExecutionException ex) {
                System.err.println("Error when send response message to client, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void readRequest() {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(SIZE);
            int le = -1;
            byte[] bb = new byte[32 + SIZE];
            while (true) {
                try {
                    le = soc.read(bbuf).get();
                    if (le != -1) {
                        if (le == 1) {
                            List<IMessageListener> listeners = channelListeners.get(Channel.SYSTEM_CHANNEL);
                            for (IMessageListener listener : listeners) {
                                listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), bbuf.get()));
                            }
                        } else {
                            bbuf.flip();
                            bbuf.get(bb, 0, le);
                            String channel = new String(bb, 0, 32).trim();
                            byte[] message = new byte[le - 32];
                            System.arraycopy(bb, 32, message, 0, message.length);
                            if (Channel.ALL_CHANNEL.equals(channel)) {
                                for (List<IMessageListener> listeners : channelListeners.values()) {
                                    for (IMessageListener listener : listeners) {
                                        listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), message));
                                    }
                                }
                            } else {
                                List<IMessageListener> listeners = channelListeners.get(channel);
                                if (null != listeners && !listeners.isEmpty()) {
                                    for (IMessageListener listener : listeners) {
                                        listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), message));
                                    }
                                }
                            }
                            bbuf.clear();
                        }
                    }
                } catch (IOException ex) {
                    System.err.println(String.format("Client %s exited or forced to quit, stop listenning.", this.clientId));
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    bbuf.clear();
                }
            }
        }
    }
}
