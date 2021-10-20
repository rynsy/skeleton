package io.cresco.skeleton;

import io.cresco.fsm.Agent;
import io.cresco.fsm.AgentStateManager;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class ExecutorImpl implements Executor {

    private PluginBuilder plugin;
    private AgentStateManager fsmManager;
    CLogger logger;


    public ExecutorImpl(PluginBuilder pluginBuilder, AgentStateManager agentStateManager) {
        this.plugin = pluginBuilder;
        this.fsmManager = agentStateManager;
        logger = plugin.getLogger(ExecutorImpl.class.getName(),CLogger.Level.Info);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeDISCOVER(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeERROR(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeINFO(MsgEvent incoming) {
        logger.info("INCOMING INFO MESSAGE : " + incoming.getParams());
        System.out.println("INCOMING INFO MESSAGE FOR PLUGIN");
        if (incoming.paramsContains("agent_manager_name") ) {
            if (incoming.paramsContains("ping_agent")
                    && incoming.paramsContains("fsm_agent_id")) {
                Agent fsm = fsmManager.getAgent(Integer.parseInt(incoming.getParam("fsm_agent_id")));
                fsm.clock();
            }
        }
        return null;
    }
    @Override
    public MsgEvent executeEXEC(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        return null;
    }


}