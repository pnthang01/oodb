/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.controller;

import com.google.gson.reflect.TypeToken;
import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestParam;
import io.cluster.http.core.AbstractController;
import io.cluster.http.core.ResponseModel;
import io.cluster.server.bean.TaskBean;
import io.cluster.server.node.CoordinatorNodeManager;
import io.cluster.server.node.MasterNode;
import io.cluster.server.node.TaskControllingManager;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
    private final TaskControllingManager taskControl = TaskControllingManager.load();
    private final CoordinatorNodeManager nodeManager = CoordinatorNodeManager.load();
    private static final Type LIST_MAP_TASK = new TypeToken<List<Map<String, String>>>() {
    }.getType();

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
            @RequestParam(name = "address", required = true) String address,
            @RequestParam(name = "message", required = true) String message) {
        try {
//            if (!API_KEY.equals(apiKey)) {
//                return "ApiKey is wrong.Close connection";
//            }
            String[] split = address.split(":");
            String hashAddress = StringUtil.getHashAddress(split[0], Integer.parseInt(split[1]));
            master.sendMessageToSingleClient(channel, hashAddress, message);
            return new ResponseModel(1, "success", null);
        } catch (Exception ex) {
            LOGGER.error("Cannot send message to a node ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/assign_task")
    public Object assignTaskToNode(@RequestParam(name = "apikey", required = true) String apiKey,
            @RequestParam(name = "address", required = true) String address,
            @RequestParam(name = "task", required = true) String task,
            @RequestParam(name = "new_process", required = true) boolean newProcess,
            @RequestParam(name = "parallel", required = false, defaultValue = "1") int parallelProcess,
            @RequestParam(name = "delay", required = false, defaultValue = "3000") int delay,
            @RequestParam(name = "period", required = true, defaultValue = "0") int period) {
        ResponseModel response = null;
        try {
//            if (!API_KEY.equals(apiKey)) {
//                response= new ResponseModel(-1, "failed", "ApiKey is wrong.Close connection");
//            }
            List<Map<String, String>> inst = MethodUtil.fromJson(task, LIST_MAP_TASK);
            if (inst == null || inst.isEmpty()) {
                LOGGER.error("No task has definied. Assign task failed.");
                response = new ResponseModel(-1, "failed", "No task has definied. Assign task failed.");
            } else {
                String[] split = address.split(":");
                if (split.length != 2) {
                    response = new ResponseModel(-1, "failed", "You must specify either on a node or on a coordinator");
                } else {
                    TaskBean taskBean = new TaskBean();
                    taskBean.setHost(split[0]);
                    taskBean.setPort(StringUtil.safeParseInt(split[1]));
                    taskBean.setOnNewProcess(newProcess);
                    taskBean.setDelay(delay);
                    taskBean.setPeriod(period);
                    taskBean.setParallelProcess(parallelProcess);
                    taskBean.setInstructions(inst);
                    taskControl.addTask(taskBean);
                    response = new ResponseModel(1, "success", null);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot send task to a node ", ex);
            response = new ResponseModel(-1, "failed", ex.getMessage());
        } finally {
            return response;
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
            LOGGER.error("Cannot send message to group [" + group + "], at channel [" + channel + "] with message: " + message, ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/create_node_by_coordinator")
    public Object createNewNode(@RequestParam(name = "apikey", required = true) String apiKey,
            @RequestParam(name = "host", required = false) String host,
            @RequestParam(name = "port", required = false) int port,
            @RequestParam(name = "param", required = false) String param) {
        try {
            boolean res = Boolean.TRUE;
            if ((StringUtil.isNullOrEmpty(host) || port == 0) && StringUtil.isNullOrEmpty(param)) {
                res = nodeManager.createNewNode();
            } else if (!StringUtil.isNullOrEmpty(host) && port > 0) {
                res = nodeManager.createNewNode(host, port);
            } else if (!StringUtil.isNullOrEmpty(host) && port > 0 && !StringUtil.isNullOrEmpty(param)) {
                Map<String, String> sendingMessage = MethodUtil.fromJsonToMap(param);
                res = nodeManager.createNewNode(host, port, sendingMessage);
            }
            if (res) {
                return new ResponseModel(200, "success", null);
            } else {
                return new ResponseModel(-1, "failed", "Cannot create new node.");
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot create message to coordinator ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }
}
