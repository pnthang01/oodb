/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.listener.IMessageListener;
import io.cluster.net.bean.INetBean;
import io.cluster.net.bean.ResponseNetBean;
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

    public static class ClientTestChannel extends IMessageListener {

        @Override
        public String onChannel(INetBean bean) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String onMessage(INetBean bean) {
            if (null == bean || !(bean instanceof ResponseNetBean)) {
                return null;
            }
            ResponseNetBean response = (ResponseNetBean) bean;
            String message = response.getMessageAsString();
            System.out.println("Receive message from server: " + message);
            return null;
        }

    }

}
