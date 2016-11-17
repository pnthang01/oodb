/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.shared.core;

import io.cluster.server.bean.NodeBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public abstract class AbstractGroupManager {

    private static final Logger LOGGER = LogManager.getLogger(AbstractGroupManager.class.getName());

    protected final ConcurrentMap<String, NodeBean> nodeGroup;

    public AbstractGroupManager() {
        this.nodeGroup = new ConcurrentHashMap<>();
    }

    public void addNodeBean(NodeBean nodeBean) {
        nodeGroup.put(nodeBean.getId(), nodeBean);
    }
    
}
