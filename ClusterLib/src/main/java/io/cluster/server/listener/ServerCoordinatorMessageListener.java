/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.shared.bean.RequestNetBean;
import io.cluster.shared.core.IMessageListener;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerCoordinatorMessageListener extends IMessageListener<RequestNetBean> {

    private static final Logger LOGGER = LogManager.getLogger(ServerCoordinatorMessageListener.class.getName());

    @Override
    public String onChannel(RequestNetBean bean) {
        return null;
    }

    @Override
    public String onMessage(RequestNetBean bean) {
        try {
            String messageStr = bean.getMessageAsString();
            if (null == messageStr) {
                System.err.println("Cannot not process request with null message");
                return null;
            }
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
        } catch (Exception ex) {
            LOGGER.error("Receive wrong message: " + MethodUtil.toJson(bean), ex);
        }
        return null;
    }

}
