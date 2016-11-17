/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.server.bean.NodeBean;
import io.cluster.server.node.MasterNode;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.shared.core.IMessageListener;
import io.cluster.util.Constants;
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
    private MasterNode masterNode = MasterNode.load();

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
            NodeBean node = masterNode.getNodeByHostPort(bean.getHost(), bean.getPort());
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
            String action = message.getOrDefault("action", "");
            switch (action) {
                case Constants.Action.REPORT_ACTION:
                    node.setState(message);
                    break;
                default:
                    LOGGER.error(String.format("Cannot perform request with action: %s", action));
            }
        } catch (Exception ex) {
            LOGGER.error("Receive wrong message: " + MethodUtil.toJson(bean), ex);
        }
        return null;
    }

}
