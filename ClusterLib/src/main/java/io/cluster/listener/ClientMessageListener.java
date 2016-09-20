/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.net.bean.INetBean;
import io.cluster.net.bean.ResponseNetBean;
import io.cluster.util.StringUtil;

/**
 *
 * @author thangpham
 */
public class ClientMessageListener extends IMessageListener{

    @Override
    public String onChannel(INetBean bean) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String onMessage(INetBean bean) {
        if (null == bean || !(bean instanceof ResponseNetBean) || StringUtil.isNullOrEmpty(bean.getMessage())) {
            System.err.println("Client Request is empty or a null or wrong net bean, cannot process.");
            return null;
        }
        ResponseNetBean response = (ResponseNetBean) bean;
        String message = response.getMessageAsString();
        System.out.println("Send message".equals(message));
        System.out.println("Receive message from server: " + message);
        return null;
    }
    
}
