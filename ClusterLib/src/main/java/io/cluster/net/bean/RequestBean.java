/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net.bean;

import java.net.SocketAddress;

/**
 *
 * @author thangpham
 */
public class RequestBean extends NetBean {

    private SocketAddress address;
    private byte[] message;
    private long loggedtime;
    private String host;
    private int port = -1;

    public RequestBean(SocketAddress address, byte... message) {
        this.address = address;
        this.message = message;
        this.loggedtime = System.currentTimeMillis();
    }

    @Override
    public String getMessageAsString() {
        return new String(message, 0, message.length).trim();
    }

    public String getHost() {
        if (null != host) {
            return host;
        }
        String address = this.address.toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        host = tmp[0];
        port = Integer.valueOf(tmp[1]);
        return host;
    }

    public int getPort() {
        if (port != -1) {
            return port;
        }
        String address = this.address.toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        host = tmp[0];
        port = Integer.valueOf(tmp[1]);
        return port;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public long getLoggedtime() {
        return loggedtime;
    }

    public void setLoggedtime(long loggedtime) {
        this.loggedtime = loggedtime;
    }

}
