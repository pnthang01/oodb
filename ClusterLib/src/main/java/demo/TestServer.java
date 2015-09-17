/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.listener.MessageListener;
import io.cluster.net.bean.NetBean;
import io.cluster.net.bean.RequestBean;
import io.cluster.node.MasterNode;
import io.cluster.node.NodeManager;
import java.io.File;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestServer {

    public static void main(String[] args) {
        MasterNode master = null;
        if (args.length > 0) {
            File file = new File(args[0]);
            master = new MasterNode(file);
        } else {
            master = new MasterNode();
        }
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
                System.out.println(NodeManager.checkNodeStatuses());
                int node = -1;
                node = sc.nextInt();
                String id = NodeManager.getNodeByIndex(node).getId();
                NodeManager.sendMessageToSingleClient(id, "testchannel", "Send message");
            }
            if (choice == 3) {
                System.out.println(NodeManager.checkNodeStatuses());
            }
        } while (choice != 0);
    }

    public static class TestChannel implements MessageListener {

        @Override
        public String onChannel(NetBean bean) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String onMessage(NetBean bean) {
            RequestBean requestBean = (RequestBean) bean;
            String message = requestBean.getMessageAsString();
            System.out.println("Receive message: " + message);
            return null;
        }

    }
}
