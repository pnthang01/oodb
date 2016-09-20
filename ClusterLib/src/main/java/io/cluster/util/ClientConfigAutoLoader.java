/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author thangpham
 */
public class ClientConfigAutoLoader {

    private final Properties configMap;
    private static ClientConfigAutoLoader _clientConfig;

    public ClientConfigAutoLoader() throws IOException {
        configMap = new Properties();
        configMap.load(new FileInputStream(Constants.getClientConfigFile()));
    }

    private static void _init() throws IOException {
        _clientConfig = new ClientConfigAutoLoader();
    }

    public static String getConfigByName(String name) throws IOException {
        if (null == _clientConfig) {
            _init();
        }
        return _clientConfig.configMap.getProperty(name);
    }
}
