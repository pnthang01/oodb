/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net;

import io.cluster.listener.MessageListener;
import io.cluster.listener.ServerMessageListener;
import io.cluster.net.bean.RequestBean;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thangpham
 */
public class NIOAsyncServer extends Thread {

    private AsynchronousServerSocketChannel asynSvr;
    private List<MessageListener> listeners;
    private List<AsyncSocketBean> beanList;

    public NIOAsyncServer() {
        try {
            listeners = new ArrayList();
            beanList = new ArrayList<>();
            asynSvr = AsynchronousServerSocketChannel.open();
            asynSvr.bind(new InetSocketAddress(14000));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addListener(MessageListener listener) {
        this.listeners.add(listener);
    }

    public void sendMessage(String id, String message) {
        for(AsyncSocketBean bean : beanList) {
            if(bean.getClientId().equals(id)) {
                bean.sendResponse(message);
                break;
            }
        }
    }
    
    public void sendMessage(String message) {
        for (AsyncSocketBean bean : beanList) {
            bean.sendResponse(message);
        }
    }

    @Override
    public void run() {
        try {

            while (true) {
                AsyncSocketBean bean = new AsyncSocketBean(asynSvr.accept().get(), listeners);
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
        private final List<MessageListener> listeners;

        public AsyncSocketBean(AsynchronousSocketChannel soc, List<MessageListener> listeners) throws IOException {
            this.soc = soc;
            this.listeners = listeners;
            for (MessageListener listener : listeners) {
                String result = listener.onChannel(new RequestBean(soc.getRemoteAddress(), null));
                if (listener instanceof ServerMessageListener) {
                    this.clientId = result;
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
                soc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendResponse(String response) {
            soc.write(ByteBuffer.wrap(response.getBytes()));
        }

        private void readRequest() {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(4096);
            int le = -1;
            byte[] bb = new byte[4096];
            while (true) {
                try {
                    le = soc.read(bbuf).get();
                    if (le != -1) {
                        bbuf.flip();
                        bbuf.get(bb, 0, le);
                        for (MessageListener listener : listeners) {
                            listener.onMessage(new RequestBean(soc.getRemoteAddress(), bb));
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
