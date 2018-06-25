package io.cresco.skeleton;

import io.cresco.library.plugin.PluginBuilder;
import org.osgi.service.log.LogService;

public class MessageSender implements Runnable  {

    private PluginBuilder plugin;

    public MessageSender(PluginBuilder plugin) {
        this.plugin = plugin;
    }


    public void run() {

        while(true) {
            try {

                plugin.msgIn(String.valueOf(System.nanoTime()));
                LogService ls = plugin.getLogService();

                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
