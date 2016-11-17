/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.server.node;

import io.cluster.server.bean.NodeBean;
import io.cluster.server.listener.ServerCoordinatorMessageListener;
import io.cluster.shared.core.AbstractGroupManager;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class CoordinatorNodeManager extends AbstractGroupManager {

    private static final Logger LOGGER = LogManager.getLogger(CoordinatorNodeManager.class.getName());

    private final MasterNode masterNode;
    private static CoordinatorNodeManager _instance;

    static {
        _instance = new CoordinatorNodeManager();
    }

    public static CoordinatorNodeManager load() {
        return _instance;
    }

    public CoordinatorNodeManager() {
        super();
        this.masterNode = MasterNode.load();
        ServerCoordinatorMessageListener coorListener = new ServerCoordinatorMessageListener();
        masterNode.addListener(Constants.Channel.COORDINATOR_CHANNEL, coorListener);
    }

    /**
     * Create new node base on Coordinator node group. This will balance nodes
     * on servers by number of nodes on per server.
     *
     * @return
     */
    public boolean createNewNode() throws InterruptedException, ExecutionException {
        NodeBean minUsageNode = null;
        int min = 9999;
        for (Entry<String, NodeBean> entry : nodeGroup.entrySet()) {
            NodeBean value = entry.getValue();
            int usage = StringUtil.safeParseInt(value.getState().get("num_of_nodes"));
            if (usage == 0) {
                minUsageNode = value;
                break;
            }
            if (usage < min) {
                minUsageNode = value;
                min = usage;
            }
        }
        return createNewNode(minUsageNode.getHost(), minUsageNode.getPort());
    }

    /**
     * Create new node base on Coordinator node group. If not Coordinator group
     * does not exist or could not create any new node, result will be false,
     * otherwise true. Initialization parameters are default.
     *
     * @param host
     * @param port
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public boolean createNewNode(String host, int port) throws InterruptedException, ExecutionException {
        Map<String, String> sendingMessage = new HashMap();
        sendingMessage.put("action", "/build/v2.adx.carpenter-backend/start_one_node.sh");
        sendingMessage.put("param", "MaxMem 2GB");
        return createNewNode(host, port, sendingMessage);
    }

    /**
     * Create new node base on Coordinator node group. If not Coordinator group
     * does not exist or could not create any new node, result will be false,
     * otherwise true.
     *
     * @param host
     * @param port
     * @return
     */
    public boolean createNewNode(String host, int port, Map<String, String> sendingMessage) throws InterruptedException, ExecutionException {
        return masterNode.sendMessageToSingleClient(host, port, Constants.Channel.COORDINATOR_CHANNEL, MethodUtil.toJson(sendingMessage));
    }

}
