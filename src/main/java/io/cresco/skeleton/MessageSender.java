package io.cresco.skeleton;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.osgi.service.log.LogService;

public class MessageSender implements Runnable  {

    private PluginBuilder plugin;
    CLogger logger;

    public MessageSender(PluginBuilder plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger(this.getClass().getName(), CLogger.Level.Trace);

    }


    public void run() {

        while(true) {
            try {

                plugin.msgIn(String.valueOf(System.nanoTime()));
                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
