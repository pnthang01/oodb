/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.net.bean.NetBean;

/**
 *
 * @author thangpham
 */
public interface MessageListener {

    /**
     * When new channel joins to cluster to give message
     * @param message 
     */
    String onChannel(NetBean bean);
    
    /**
     * When one channel send message so listener will accept it
     * @param message 
     */
    String onMessage(NetBean bean);
}
