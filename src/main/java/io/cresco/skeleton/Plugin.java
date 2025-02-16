package io.cresco.skeleton;


import io.cresco.fsm.AgentStateManager;
import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.Map;

@Component(
        service = { PluginService.class },
        scope=ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service=AgentService.class)
)

public class Plugin implements PluginService {

    public BundleContext context;
    public static  PluginBuilder pluginBuilder;
    private Executor executor;
    private CLogger logger;
    private Map<String, Object> map;
    public String myname;
    private RepoEngine repoEngine;
    private AgentStateManager agentEngine;


    @Override
    public boolean isActive() {
        return pluginBuilder.isActive();
    }

    @Override
    public void setIsActive(boolean isActive) {
        pluginBuilder.setIsActive(isActive);
    }


    @Activate
    void activate(BundleContext context, Map<String, Object> map) {

        this.context = context;
        this.map = map;
        myname = "this is my name";

    }

    @Modified
    void modified(BundleContext context, Map<String, Object> map) {
        System.out.println("Modified Config Map PluginID:" + (String) map.get("pluginID"));
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        pluginBuilder.msgIn(incoming);
        return true;
    }

    @Deactivate
    void deactivate(BundleContext context, Map<String,Object> map) {

        isStopped();
        this.context = null;
        this.map = null;

    }

    @Override
    public boolean isStarted() {

        System.out.println("Started PluginID:" + (String) map.get("pluginID"));

        try {
            pluginBuilder = new PluginBuilder(this.getClass().getName(), context, map);
            this.logger = pluginBuilder.getLogger(Plugin.class.getName(), CLogger.Level.Info);
            agentEngine = new AgentStateManager(pluginBuilder);
            this.executor = new ExecutorImpl(pluginBuilder, agentEngine);
            pluginBuilder.setExecutor(executor);
            // repoEngine = new RepoEngine(pluginBuilder, agentEngine);

            while (!pluginBuilder.getAgentService().getAgentState().isActive()) {
                logger.info("Plugin " + pluginBuilder.getPluginID() + " waiting on Agent Init");
                Thread.sleep(1000);
            }

            //send a bunch of messages
            MessageSender messageSender = new MessageSender(pluginBuilder);
            new Thread(messageSender).start();
            logger.info("Started Message Sender");

            //repoEngine.start(); // TODO: Down the line I want to try to handle all of this over the dataplane. Going to try direct messages for now.

            //set plugin active
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        pluginBuilder.setExecutor(null);
        pluginBuilder.setIsActive(false);
        return true;
    }
}