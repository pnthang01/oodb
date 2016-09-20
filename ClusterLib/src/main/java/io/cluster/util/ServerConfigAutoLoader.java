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
public class ServerConfigAutoLoader {

    private Properties configMap;
    private static ServerConfigAutoLoader _serverConfig;

    public ServerConfigAutoLoader() throws IOException {
        configMap = new Properties();
        configMap.load(new FileInputStream(Constants.getServerConfigFile()));
    }

    private static void _init() throws IOException {
        _serverConfig = new ServerConfigAutoLoader();
    }

    public static String getConfigByName(String name) throws IOException {
        if (null == _serverConfig) {
            _init();
        }
        return _serverConfig.configMap.getProperty(name);
    }
}
