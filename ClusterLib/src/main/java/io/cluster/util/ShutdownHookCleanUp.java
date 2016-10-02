/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author thangpham
 */
public class ShutdownHookCleanUp {

    private static final Deque<ExecutorService> cleanUpExecutor = new ArrayDeque<>();
    
    public static void addExecutor(ExecutorService executor) {
        cleanUpExecutor.add(executor);
    } 
    
    public static void initialize() {
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutdownHook());
    }
    
    public static class ShutdownHook extends Thread {

        @Override
        public void run() {
            while(!cleanUpExecutor.isEmpty()) {
                ExecutorService executor = cleanUpExecutor.poll();
                executor.shutdownNow();
            }
        }

    }
}
