/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.listener;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.INetBean;
import io.cluster.shared.bean.ResponseNetBean;
import io.cluster.shared.model.MessageModel;
import io.cluster.util.Constants.Action;
import io.cluster.util.StringUtil;

/**
 *
 * @author thangpham
 */
public class ClientMessageListener extends IMessageListener {

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
        String messageStr = response.getMessageAsString();
        if (null == messageStr) {
            System.err.println("Cannot not process request with null message");
            return null;
        }
        MessageModel message = StringUtil.fromJson(messageStr, MessageModel.class);
        switch (message.getAction()) {
            case Action.START_ACTION:
                break;
            case Action.STOP_ACTION:
                break;
            default:
                System.err.println(String.format("Cannot perform request with action:", message.getAction()));
        }
        System.out.println("Receive message from server: " + message);
        return null;
    }

}
