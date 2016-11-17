/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.server.bean.NodeBean;
import io.cluster.server.node.MasterNode;
import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.util.Constants.Action;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerHardwareMonitoringListener extends IMessageListener<RequestNetBean> {
    
    private static final Logger LOGGER = LogManager.getLogger(MasterNode.class.getName());
    
    private MasterNode masterNode = MasterNode.load();
    
    @Override
    public String onMessage(RequestNetBean request) {
        try {
            RequestNetBean requestBean = (RequestNetBean) request;
            if (request.getMessage().length == 1) {
                masterNode.ping(requestBean.getHost(), requestBean.getPort());
            } else {
                String messageStr = request.getMessageAsString();
                if (null == messageStr) {
                    System.err.println("Cannot not process request with null message");
                    return null;
                }
                NodeBean node = masterNode.getNodeByHostPort(request.getHost(), request.getPort());
                Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
                String action = message.getOrDefault("action", "");
                switch (action) {
                    case Action.REPORT_ACTION:
                        node.setState(message);
                        break;
                    default:
                        LOGGER.error(String.format("Cannot perform request with message: %s", messageStr));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Strange error occured with error message: ", ex);
        }
        return null;
    }
    
    @Override
    public String onChannel(RequestNetBean request) {
        return null;
    }
    
}
