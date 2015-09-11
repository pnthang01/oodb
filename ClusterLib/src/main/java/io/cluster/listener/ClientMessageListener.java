/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.net.bean.NetBean;
import io.cluster.net.bean.ResponseBean;

/**
 *
 * @author thangpham
 */
public class ClientMessageListener implements MessageListener{

    @Override
    public String onChannel(NetBean bean) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String onMessage(NetBean bean) {
        if(null == bean || !(bean instanceof ResponseBean)) return null;
        ResponseBean response = (ResponseBean) bean;
        String message = response.getMessageAsString();
        System.out.println("Send message".equals(message));
        System.out.println("Receive message from server: " + message);
        return null;
    }
    
}
