/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net.bean;

import java.net.SocketAddress;

/**
 *
 * @author thangpham
 */
public class SystemNetBean extends INetBean {

    public SystemNetBean(SocketAddress address, byte... message) {
        super(address, message);
    }

}