/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
public class ShutdownHookCleanUp {

    private static final Deque<ExecutorCleanUpUnit> cleanUpExecutor = new ArrayDeque<>();

    public static void addExecutor(ExecutorCleanUpUnit executor) {
        cleanUpExecutor.add(executor);
    }

    public static void initialize() {
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutdownHook());
        System.out.println("Initialize shutdown hook cleanup");
    }

    public static class ShutdownHook extends Thread {

        @Override
        public void run() {
            while (!cleanUpExecutor.isEmpty()) {
                try {
                    ExecutorCleanUpUnit unit = cleanUpExecutor.poll();
                    System.out.println("Start to shuwdown executor: " + unit.name);
                    unit.executor.shutdown();
                    while (!unit.executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    }
                    System.out.println("Shutdown the executor successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to shutdown executor.");
                }
            }
        }
    }

    public static class ExecutorCleanUpUnit {

        private String name;
        private ExecutorService executor;

        public ExecutorCleanUpUnit(String name, ExecutorService executor) {
            this.name = name;
            this.executor = executor;
        }

    }

}
