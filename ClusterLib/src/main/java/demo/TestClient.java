/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.listener.MessageListener;
import io.cluster.net.bean.NetBean;
import io.cluster.net.bean.ResponseBean;
import io.cluster.node.WorkerNode;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestClient {

    public static void main(String[] args) {
        String config = null;
        if (args.length > 0) {
            config = args[0];
        } else {
            config = "config/NIOClientConfig.txt";
        }
        WorkerNode.initialize(config);
        WorkerNode.addListener("testchannel", new ClientTestChannel());
        int choice = -1;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("1. Send request");
            choice = sc.nextInt();
            switch (choice) {
                case 1:
                    WorkerNode.sendRequest("testchannel", "Send request");
                    break;
                case 2:
                    break;
            }
        } while (choice != 0);
    }

    public static class ClientTestChannel implements MessageListener {

        @Override
        public String onChannel(NetBean bean) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String onMessage(NetBean bean) {
            if (null == bean || !(bean instanceof ResponseBean)) {
                return null;
            }
            ResponseBean response = (ResponseBean) bean;
            String message = response.getMessageAsString();
            System.out.println("Receive message from server: " + message);
            return null;
        }

    }

}
