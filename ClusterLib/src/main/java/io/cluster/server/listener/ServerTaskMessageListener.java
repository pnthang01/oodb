/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.listener;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.server.bean.NodeBean;
import io.cluster.server.node.MasterNode;
import io.cluster.util.Constants.Action;
import io.cluster.util.Constants.Group;
import io.cluster.util.StringUtil;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class ServerTaskMessageListener extends IMessageListener<RequestNetBean> {

    private final MasterNode masterNode = MasterNode.load();

    @Override
    public String onChannel(RequestNetBean bean) {
        String address = bean.getAddress().toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        NodeBean addedNode = masterNode.addNode(Group.NONE_GROUP, tmp[0], Integer.valueOf(tmp[1]));
        return addedNode.getId();
    }

    @Override
    public String onMessage(RequestNetBean bean) {
        String messageStr = bean.getMessageAsString();
        if (null == messageStr) {
            System.err.println("Cannot not process request with null message");
            return null;
        }
        Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
        String action = message.getOrDefault("action", "");
        switch (action) {
            case Action.REPORT_ACTION:
                break;
            default:
                System.err.println(String.format("Cannot perform request with action:", messageStr));
        }

        return null;
    }

}
