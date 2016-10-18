/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.shared.core.IMessageListener;
import io.cluster.shared.bean.INetBean;
import io.cluster.shared.bean.ResponseNetBean;
import io.cluster.client.node.WorkerNode;
import io.cluster.util.Constants;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author thangpham
 */
public class TestClient {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length > 0) {
            Constants.setBaseConfigFolder(args[0]);
        }
        WorkerNode workerNode = WorkerNode.initialize(true);
        workerNode.addListener("testchannel", new ClientTestChannel());
        int choice = -1;
        Scanner sc = new Scanner(System.in);
        do {
            try {
                System.out.println("1. Send request");
                System.out.println("2. Print remote address");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        workerNode.sendRequest("testchannel", "Send request");
                        break;
                    case 2:
                        SocketAddress remoteAddress = workerNode.getLocalAddress();
                        System.out.println("Address: " + remoteAddress.toString());
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
