/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.controller;

import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestParam;
import io.cluster.http.core.AbstractController;
import io.cluster.http.core.ResponseModel;
import io.cluster.server.node.MasterNode;
import io.cluster.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
@RequestMapping(uri = "/node")
public class NodeController extends AbstractController {

    private static final Logger LOGGER = LogManager.getLogger(NodeController.class.getName());

    private final String API_KEY = "abc";
    private final MasterNode master = MasterNode.load();

    @RequestMapping(uri = "/all_status")
    public Object getAllStatus(@RequestParam(name = "apikey", required = true) String apiKey) {
        try {
//            if (!API_KEY.equals(apiKey)) {
//                return "ApiKey is wrong.Close connection";
//            }
            return master.checkAllNodeStatus();
        } catch (Exception ex) {
            LOGGER.error("Cannot get all node statues ", ex);
            return "Cannot get all node statues, error: " + ex.getMessage();
        }
    }

    @RequestMapping(uri = "/send_message")
    public Object sendMessageToNode(@RequestParam(name = "apikey", required = true) String apiKey,
            @RequestParam(name = "channel", required = true) String channel,
            @RequestParam(name = "host", required = true) String host,
            @RequestParam(name = "port", required = true) int port,
            @RequestParam(name = "message", required = true) String message) {
        try {
//            if (!API_KEY.equals(apiKey)) {
//                return "ApiKey is wrong.Close connection";
//            }
            String hashAddress = StringUtil.getHashAddress(host, port);
            master.sendMessageToSingleClient(channel, hashAddress, message);
            return new ResponseModel(1, "success", null);
        } catch (Exception ex) {
            LOGGER.error("Cannot send message to a node ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/send_message_group")
    public Object sendMessageToGroup(@RequestParam(name = "apikey", required = true) String apiKey,
            @RequestParam(name = "channel", required = true) String channel,
            @RequestParam(name = "group", required = true) String group,
            @RequestParam(name = "message", required = true) String message) {
        try {
//            if (!API_KEY.equals(apiKey)) {
//                return "ApiKey is wrong.Close connection";
//            }
            master.sendMessageToGroupClient(channel, group, message);
            return new ResponseModel(1, "success", null);
        } catch (Exception ex) {
            LOGGER.error("Cannot send message to coordinator ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }
}
