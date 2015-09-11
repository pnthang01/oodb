/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net.bean;

/**
 *
 * @author thangpham
 */
public class ResponseBean extends NetBean {

    private byte[] message;
    private long loggedtime;

    public ResponseBean(byte[] message) {
        this.message = message;
        this.loggedtime = System.currentTimeMillis();
    }
    
    @Override
    public String getMessageAsString() {
        return new String(message, 0 , message.length);
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
