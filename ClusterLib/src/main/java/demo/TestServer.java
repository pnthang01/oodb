/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.INetBean;
import io.cluster.shared.bean.RequestNetBean;
import io.cluster.server.node.MasterNode;
import io.cluster.util.Constants;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestServer {

    public static void main(String[] args) {
        if (args.length > 0) {
            Constants.setBaseConfigFolder(args[0]);
        }
        
        MasterNode masterNode = MasterNode.load();
//        masterNode.addListenner("testchannel", new TestChannel());
        //
        int choice = 0;
        Scanner sc = new Scanner(System.in);
        do {
            try {
                System.out.println("1. Send message");
                System.out.println("2. Send message to single client");
                System.out.println("3. Monitor clients");
                choice = sc.nextInt();
                if (choice == 1) {
                    masterNode.sendMessageToAllClient("testchannel", "Send message");
                }
                if (choice == 2) {
                    System.out.println(masterNode.checkAllNodeStatus());
                    int node = -1;
                    node = sc.nextInt() - 1;
                    String id = masterNode.getNodeByIndex(node).getId();
                    masterNode.sendMessageToSingleClient(id, "testchannel", "Send message");
                }
                if (choice == 3) {
                    System.out.println(masterNode.checkAllNodeStatus());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
