package io.cluster.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thangpham
 */
public class Constants {

    public static class Channel {

        public static final String ALL_CHANNEL = "c_all";
        public static final String SYSTEM_CHANNEL = "c_system";
        public static final String NODE_CHANNEL = "c_node";
        public static final String COORDINATOR_CHANNEL = "c_coor";
    }

    public static class Group {

        public static final String NONE_GROUP = "g_none";
        public static final String COORDINATOR_GROUP = "g_coor";
    }

    public static class Action {

        public static final String REPORT_ACTION = "_report";
        public static final String STOP_ACTION = "_stop";
        public static final String START_ACTION = "_start";
    }

    private static final String SERVER_CONFIGURATION_FILE = "NIOServerConfig.txt";
    private static final String CLIENT_CONFIGURATION_FILE = "NIOClientConfig.txt";

    private static String BASE_CONFIGURATION_FOLDER = "config/";

    public static void setBaseConfigFolder(String folder) {
        BASE_CONFIGURATION_FOLDER = folder;
    }

    public static String getBaseConfigFolder() {
        return BASE_CONFIGURATION_FOLDER;
    }

    public static String getServerConfigFile() {
        return StringUtil.toString(BASE_CONFIGURATION_FOLDER, SERVER_CONFIGURATION_FILE);
    }

    public static String getClientConfigFile() {
        return StringUtil.toString(BASE_CONFIGURATION_FOLDER, CLIENT_CONFIGURATION_FILE);
    }
}
