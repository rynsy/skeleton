package io.cresco.skeleton;


import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.Map;

@Component(
        service = { PluginService.class },
        scope=ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        servicefactory = true,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service=AgentService.class)
)

public class Plugin implements PluginService {

    public BundleContext context;


    @Activate
    void activate(BundleContext context, Map<String,Object> map) {

        this.context = context;

        System.out.println("Config Map PluginID:" + (String) map.get("pluginID"));

        try {
            PluginBuilder plugin = new PluginBuilder(context, map);
            plugin.getAgentService().getAgentState().sendMessage("AGENT: " + (String) map.get("pluginID"));

            MessageSender messageSender = new MessageSender(plugin);
            new Thread(messageSender).start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    @Modified
    void modified(BundleContext context, Map<String,Object> map) {
        System.out.println("Modified Config Map PluginID:" + (String) map.get("pluginID"));
    }


    @Override
    public boolean msgIn(String msg) {
        return true;
    }

    @Override
    public boolean msgIn(MsgEvent msg) {
        return true;
    }


}