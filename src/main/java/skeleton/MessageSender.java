package skeleton;

import io.cresco.library.agent.AgentService;
import io.cresco.library.plugin.PluginBuilder;
import org.osgi.service.log.LogService;

public class MessageSender implements Runnable  {

    private PluginBuilder plugin;

    public MessageSender(PluginBuilder plugin) {
        this.plugin = plugin;
    }


    public void run() {

        System.out.println("PLUGIN RUN");

        while(true) {
            try {


                plugin.msgIn(String.valueOf(System.nanoTime()));
                LogService ls = plugin.getLogService();
                ls.log(0,"ITS IN HERE!!!");
                /*
                LogService ls = plugin.getLogService();

                if(ls != null) {
                    System.out.println("Log Service != null");
                } else {
                    System.out.println("Log Service = null");
                }
                */
                /*
                plugin.getLogService().log(0,"LOG 0");
                plugin.getLogService().log(1,"LOG 1");
                plugin.getLogService().log(2,"LOG 2");
                plugin.getLogService().log(3,"LOG 3");
                plugin.getLogService().log(4,"LOG 4");
                */

                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
