/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.client.listener;

import io.cluster.shared.bean.ResponseNetBean;
import io.cluster.shared.core.IMessageListener;
import io.cluster.util.StringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ClientTaskMessageListener extends IMessageListener<ResponseNetBean> {

    private static final Logger LOGGER = LogManager.getLogger(ClientTaskMessageListener.class.getName());

    private final ConcurrentMap<String, String> instructionValues = new ConcurrentHashMap();
    private final Lock locker = new ReentrantLock(true);
    private final Condition instructionCondition = locker.newCondition();
    private final AtomicBoolean hasInstruction = new AtomicBoolean(false);

    @Override
    public String onChannel(ResponseNetBean bean) {
        LOGGER.info("This listner does not need to listen on channel.");
        return null;
    }

    @Override
    public String onMessage(ResponseNetBean bean) {
        locker.lock();
        String result = null;
        try {
            if (null == bean || !(bean instanceof ResponseNetBean) || StringUtil.isNullOrEmpty(bean.getMessage())) {
                LOGGER.error("Client Request is empty or a null or wrong net bean, cannot process.");
                result = null;
            }
            ResponseNetBean response = (ResponseNetBean) bean;
            String messageStr = response.getMessageAsString();
            if (null == messageStr) {
                LOGGER.error("Cannot not process request with null message.");
                result = null;
            }
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
            instructionValues.clear();
            instructionValues.putAll(message);
            hasInstruction.set(Boolean.TRUE);
            instructionCondition.signalAll();
            LOGGER.info("Receive message from server: " + message);
            result = null;
        } catch (Exception ex) {
            LOGGER.info("Problem occured when receiving message, error: ", ex);
        } finally {
            locker.unlock();
            return result;
        }
    }

    public Map<String, String> mustWaitForInstruction() {
        locker.lock();
        try {
            while (!hasInstruction.get()) {
                instructionCondition.await();
            }
            hasInstruction.set(Boolean.FALSE);
        } catch (Exception ex) {
            LOGGER.error("Problem occured when call mustWaitForInstuction: ", ex);
        } finally {
            locker.unlock();
        }
        Map<String, String> result = new HashMap(instructionValues);
        return result;
    }

    public Map<String, String> shouldWaitForInstruction(int wait) {
        locker.lock();
        try {
            int i = 0;
            while (!hasInstruction.get() || i < wait) {
                instructionCondition.await(3, TimeUnit.SECONDS);
                ++i;
            }
            hasInstruction.set(Boolean.FALSE);
        } catch (Exception ex) {
            LOGGER.error("Problem occured when call shouldWaitForInstruction: ", ex);
        } finally {
            locker.unlock();
        }
        Map<String, String> result = new HashMap(instructionValues);
        return result;
    }

}
