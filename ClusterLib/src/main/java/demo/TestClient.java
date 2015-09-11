/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.node.WorkerNode;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestClient {

    public static void main(String[] args) {
        WorkerNode.initialize();
        int choice = -1;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("1. Send request");
            choice = sc.nextInt();
            switch (choice) {
                case 1:
                    WorkerNode.sendRequest("Send request");
                    break;
                case 2:
                    break;
            }
        } while (choice != 0);
    }

}
