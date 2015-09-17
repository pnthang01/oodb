/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net;

import java.io.FileInputStream;
import java.util.Properties;
import io.cluster.listener.MessageListener;
import io.cluster.net.bean.RequestBean;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class NIOAsyncServer extends Thread {

    private AsynchronousServerSocketChannel asynSvr;
    private final Map<String, List<MessageListener>> channelListeners;
    private List<AsyncSocketBean> beanList;
    private int SIZE;

    public NIOAsyncServer(File configFile) {
        System.out.println("The server is about to start...");
        channelListeners = new HashMap<>();
        Properties prop = new Properties();
        beanList = new ArrayList<AsyncSocketBean>();
        try {
            prop.load(new FileInputStream(configFile));
            SIZE = Integer.parseInt(prop.getProperty("SIZE"));
            asynSvr = AsynchronousServerSocketChannel.open();
            asynSvr.bind(new InetSocketAddress(Integer.parseInt(prop.getProperty("PORT"))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("The server started successfully.");
    }

    public void addListener(String channel, MessageListener listener) {
        List<MessageListener> listeners = channelListeners.get(channel);
        if (null == listeners) {
            listeners = new ArrayList<MessageListener>();
            channelListeners.put(channel, listeners);
        }
        listeners.add(listener);
    }

    public void sendMessageToSingleBean(String id, String channel, String message) {
        for (AsyncSocketBean bean : beanList) {
            if (bean.getClientId().equals(id)) {
                bean.sendResponse(channel, message);
                break;
            }
        }
    }

    public void sendMessageToAllBean(String channel, String message) {
        for (AsyncSocketBean bean : beanList) {
            bean.sendResponse(channel, message);
        }
    }

    @Override
    public void run() {
        try {

            while (true) {
                AsyncSocketBean bean = new AsyncSocketBean(asynSvr.accept().get(), channelListeners);
                beanList.add(bean);
                bean.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AsyncSocketBean extends Thread {

        private String clientId;
        private final AsynchronousSocketChannel soc;
        private final Map<String, List<MessageListener>> channelListeners;

        public AsyncSocketBean(AsynchronousSocketChannel soc, Map<String, List<MessageListener>> channelListeners) throws IOException {
            this.soc = soc;
            this.channelListeners = channelListeners;
            List<MessageListener> listenners = channelListeners.get("system");
            for (MessageListener listener : listenners) {
                String result = listener.onChannel(new RequestBean(soc.getRemoteAddress(), null));
                this.clientId = result;
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
            byte[] finalBytes = new byte[32 + SIZE];
            System.arraycopy(channel.getBytes(), 0, finalBytes, 0, channel.length());
            System.arraycopy(response.getBytes(), 0, finalBytes, 32, response.length());
            soc.write(ByteBuffer.wrap(finalBytes));
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
                            List<MessageListener> listeners = channelListeners.get("system");
                            for (MessageListener listener : listeners) {
                                listener.onMessage(new RequestBean(soc.getRemoteAddress(), bbuf.get()));
                            }
                        } else {
                            bbuf.flip();
                            bbuf.get(bb, 0, le);
                            String channel = new String(bb, 0, 32).trim();
                            //
                            List<MessageListener> listeners = channelListeners.get(channel);
                            if (null != listeners) {
                                byte[] message = new byte[le - 32];
                                System.arraycopy(bb, 32, message, 0, message.length);
                                for (MessageListener listener : listeners) {
                                    listener.onMessage(new RequestBean(soc.getRemoteAddress(), message));
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    bbuf.clear();
                }
            }
        }
    }
}
