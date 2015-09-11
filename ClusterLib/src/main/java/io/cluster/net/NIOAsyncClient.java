/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net;

import io.cluster.listener.MessageListener;
import io.cluster.net.bean.ResponseBean;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thangpham
 */
public class NIOAsyncClient extends Thread {

    private List<MessageListener> listeners;
    AsynchronousSocketChannel soc;
    int pid = -1;

    public NIOAsyncClient() {
        try {
            listeners = new ArrayList();
            soc = AsynchronousSocketChannel.open();
            soc.connect(new InetSocketAddress("localhost", 14000)).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            pid = 1;
            Ping ping = new Ping(soc); ping.start();
            readResponse();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                soc.shutdownInput();
                soc.shutdownOutput();
                soc.close();
            } catch (Exception e) {
            }
        }
    }

    public void addListener(MessageListener listener) {
        this.listeners.add(listener);
    }

    public void sendRequest(String request) {
        soc.write(ByteBuffer.wrap(request.getBytes()));
    }

    public void close() {
        try {
            soc.shutdownInput();
            soc.shutdownOutput();
            soc.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readResponse() {
        ByteBuffer bbuf = ByteBuffer.allocateDirect(2048);
        int le = -1;
        byte[] bb = new byte[2048];
        while (true) {
            try {
                le = soc.read(bbuf).get();
                if (le != -1) {
                    bbuf.flip();
                    bbuf.get(bb, 0, le);
                    for (MessageListener listener : listeners) {
                        listener.onMessage(new ResponseBean(bb));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                bbuf.clear();
            }
        }
    }

    public class Ping extends Thread {

        private AsynchronousSocketChannel soc;
        byte[] ping = new byte[]{1};

        public Ping(AsynchronousSocketChannel soc) {
            this.soc = soc;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(3000);
                    soc.write(ByteBuffer.wrap(ping));
                } catch (InterruptedException ex) {
                }
            }
        }

    }
}
