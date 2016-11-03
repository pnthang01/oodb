/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.server.bean.NodeBean;
import io.cluster.server.node.MasterNode;
import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.model.MessageModel;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.util.Constants.Action;
import io.cluster.util.StringUtil;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class ServerNodeMonitoringListener extends IMessageListener<RequestNetBean> {
    
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
                MessageModel message = StringUtil.fromJson(messageStr, MessageModel.class);
                switch (message.getAction()) {
                    case Action.REPORT_ACTION:
                        Map<String, String> values = message.getValues();
                        node.setState(values);
                        break;
                    default:
                        System.err.println(String.format("Cannot perform request with action:", message.getAction()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String onChannel(RequestNetBean request) {
        return null;
    }

}
