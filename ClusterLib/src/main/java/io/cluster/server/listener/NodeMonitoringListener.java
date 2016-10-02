 /* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.model.MessageModel;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.server.node.NodeManager;
import io.cluster.util.Constants.Action;
import io.cluster.util.StringUtil;

/**
 *
 * @author thangpham
 */
public class NodeMonitoringListener extends IMessageListener<RequestNetBean> {

    @Override
    public String onMessage(RequestNetBean request) {
        RequestNetBean requestBean = (RequestNetBean) request;
        if (request.getMessage().length == 1) {
            NodeManager.ping(requestBean.getHost(), requestBean.getPort());
        } else {
            String messageStr = request.getMessageAsString();
            if (null == messageStr) {
                System.err.println("Cannot not process request with null message");
                return null;
            }
            MessageModel message = StringUtil.fromJson(messageStr, MessageModel.class);
            switch (message.getAction()) {
                case Action.REPORT_ACTION:
                    //Processload
                    //Ram
                    //Total Load Avg
                    //Disk
                    break;
                default:
                    System.err.println(String.format("Cannot perform request with action:", message.getAction()));
            }
        }

        return null;
    }

    @Override
    public String onChannel(RequestNetBean request) {
        return null;
    }

}
