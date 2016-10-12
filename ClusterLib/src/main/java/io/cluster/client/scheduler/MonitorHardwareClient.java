/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.scheduler;

import com.sun.management.OperatingSystemMXBean;
import io.cluster.client.node.WorkerNode;
import io.cluster.shared.model.MessageModel;
import io.cluster.util.Constants;
import io.cluster.util.StringUtil;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class MonitorHardwareClient implements Runnable {
    
    private final WorkerNode workerNode = WorkerNode.load();

    @Override
    public void run() {
        try {
            OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            Map<String, String> nodeInfo = new HashMap();
            nodeInfo.put("process_load", osMxBean.getProcessCpuLoad() + "");
            nodeInfo.put("load_avg", osMxBean.getSystemLoadAverage() + "");
            nodeInfo.put("max_mem", runtime.maxMemory() + "");
            nodeInfo.put("used_mem", (runtime.totalMemory() - runtime.freeMemory()) + "");
            MessageModel model = new MessageModel();
            model.setAction(Constants.Action.REPORT_ACTION);
            model.setValues(nodeInfo);
            workerNode.sendRequest(Constants.Channel.SYSTEM_CHANNEL, StringUtil.toJson(model));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
