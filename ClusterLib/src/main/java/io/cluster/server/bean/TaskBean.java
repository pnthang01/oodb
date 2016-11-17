/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.bean;

import java.util.List;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class TaskBean {

    private List<Map<String, String>> instructions;
    private boolean onNewProcess;
    private String host;
    private int port;
    private int parallelProcess = 1;
    private long delay;
    private long period;
    private long submitedTimed;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isOnNewProcess() {
        return onNewProcess;
    }

    public void setOnNewProcess(boolean onNewProcess) {
        this.onNewProcess = onNewProcess;
    }

    public List<Map<String, String>> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Map<String, String>> instructions) {
        this.instructions = instructions;
    }

    public int getParallelProcess() {
        return parallelProcess;
    }

    public void setParallelProcess(int parallelProcess) {
        this.parallelProcess = parallelProcess;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getSubmitedTimed() {
        return submitedTimed;
    }

    public void setSubmitedTimed(long submitedTimed) {
        this.submitedTimed = submitedTimed;
    }

}
