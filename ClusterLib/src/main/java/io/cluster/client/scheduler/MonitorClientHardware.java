/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.scheduler;

import com.google.common.util.concurrent.AtomicDouble;
import com.sun.management.OperatingSystemMXBean;
import io.cluster.client.node.WorkerNode;
import io.cluster.shared.model.MessageModel;
import io.cluster.util.Constants;
import io.cluster.util.StringUtil;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class MonitorClientHardware {

    private static final Logger LOGGER = LogManager.getLogger(MonitorClientHardware.class.getName());

    private final WorkerNode workerNode;

    private final AtomicDouble processLoad = new AtomicDouble();
    private final AtomicDouble loadAvg = new AtomicDouble();
    private final AtomicLong maxMem = new AtomicLong();
    private final AtomicLong usedMem = new AtomicLong();

    private Lock locker = null;
    private Condition outOfMemCondition = null;
    private AtomicBoolean isOutOfMem = null;

    private static MonitorClientHardware _instance;

    private MonitorClientHardware() {
        workerNode = WorkerNode.initialize();
        locker = new ReentrantLock(true);
        isOutOfMem = new AtomicBoolean();
        outOfMemCondition = locker.newCondition();
        this.run();
    }

    public synchronized static MonitorClientHardware load() {
        if (null == _instance) {
            _instance = new MonitorClientHardware();

        }
        return _instance;
    }

    public void run() {
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                locker.lock();
                try {
                    OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    Runtime runtime = Runtime.getRuntime();
                    processLoad.set(osMxBean.getProcessCpuLoad());
                    loadAvg.set(osMxBean.getSystemLoadAverage());
                    maxMem.set(runtime.maxMemory());
                    usedMem.set(runtime.totalMemory() - runtime.freeMemory());
                    double calMem = (double) usedMem.get() / (double) maxMem.get();
                    boolean checkMem = calMem <= 0.95f;
                    isOutOfMem.set(checkMem);
                    if (checkMem) {
                        outOfMemCondition.signalAll();
                    }
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
                    LOGGER.error("Error occured when monitor client hardware: ", ex);
                } finally {
                    locker.unlock();
                }
            }
        };
        timer.schedule(task, 5000, 5000);
    }

    /**
     * Check for mem is greater than specific value.
     *
     * @return
     */
    public boolean checkOutOfMem() {
        locker.lock();
        try {
            while (!isOutOfMem.get()) {
                outOfMemCondition.await();
            }
        } catch (Exception ex) {
            LOGGER.error("Error when check application's mem: ", ex);
        } finally {
            locker.unlock();
        }
        return isOutOfMem.get();
    }

    public double getProcessLoad() {
        return processLoad.get();
    }

    public double getLoadAvg() {
        return loadAvg.get();
    }

    public long getMaxMem() {
        return maxMem.get();
    }

    public long getUsedMem() {
        return usedMem.get();
    }

}
