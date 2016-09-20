/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.listener.IMessageListener;
import io.cluster.net.bean.INetBean;
import io.cluster.net.bean.RequestNetBean;
import io.cluster.node.MasterNode;
import io.cluster.node.NodeManager;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestServer {

    public static void main(String[] args) {
        String config = null;
        if (args.length > 0) {
            config = args[0];
        } 
        MasterNode.initialize(config);
        NodeManager.addListener("testchannel", new TestChannel());
        //
        int choice = 0;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("1. Send message");
            System.out.println("2. Send message to single client");
            System.out.println("3. Monitor clients");
            choice = sc.nextInt();
            if (choice == 1) {
                NodeManager.sendMessageToAllClient("testchannel", "Send message");
            }
            if (choice == 2) {
                System.out.println(NodeManager.checkAllNodeStatus());
                int node = -1;
                node = sc.nextInt() - 1;
                String id = NodeManager.getNodeByIndex(node).getId();
                NodeManager.sendMessageToSingleClient(id, "testchannel", "Send message");
            }
            if (choice == 3) {
                System.out.println(NodeManager.checkAllNodeStatus());
            }
        } while (choice != 0);
    }

    public static class TestChannel extends IMessageListener {

        @Override
        public String onChannel(INetBean bean) {
            return null;
        }

        @Override
        public String onMessage(INetBean bean) {
            RequestNetBean requestBean = (RequestNetBean) bean;
            String message = requestBean.getMessageAsString();
            System.out.println("Receive message: " + message);
            return null;
        }

    }
}
