/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.listener.model.MessageModel;
import io.cluster.net.bean.RequestNetBean;
import io.cluster.node.NodeManager;
import io.cluster.node.bean.NodeBean;
import io.cluster.util.Constants.Action;
import io.cluster.util.Constants.Channel;
import io.cluster.util.StringUtil;

/**
 *
 * @author thangpham
 */
public class NodeMessageListener extends IMessageListener<RequestNetBean> {

    @Override
    public String onChannel(RequestNetBean bean) {
        String address = bean.getAddress().toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        NodeBean addedNode = NodeManager.addNode(Channel.NONE_GROUP, tmp[0], Integer.valueOf(tmp[1]));
        return addedNode.getId();
    }

    @Override
    public String onMessage(RequestNetBean bean) {
        String messageStr = bean.getMessageAsString();
        if (null == messageStr) {
            System.err.println("Cannot not process request with null message");
            return null;
        }
        MessageModel message = StringUtil.fromJson(messageStr, MessageModel.class);
        switch (message.getAction()) {
            case Action.REPORT_ACTION:
                break;
            default:
                System.err.println(String.format("Cannot perform request with action:", message.getAction()));
        }

        return null;
    }

}
