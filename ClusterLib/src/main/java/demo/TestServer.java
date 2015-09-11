/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.listener.ServerMessageListener;
import io.cluster.net.NIOAsyncServer;
import io.cluster.node.NodeManager;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestServer {

    public static void main(String[] args) {
        NIOAsyncServer server = new NIOAsyncServer();
        server.start();
        //
        ServerMessageListener listener = new ServerMessageListener();
        server.addListener(listener);
        //
        int choice = 0;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("1. Send message");
            System.out.println("2. Send message to single client");
            System.out.println("3. Monitor clients");
            choice = sc.nextInt();
            if (choice == 1) {
                server.sendMessage("Send message");
            }
            if (choice == 2) {
                System.out.println(NodeManager.checkNodeStatuses());
                int node = -1;
                node = sc.nextInt();
                NodeManager.getNodeByIndex(node);
            }
            if (choice == 3) {
                System.out.println(NodeManager.checkNodeStatuses());
            }
        } while (choice != 0);
    }
}
