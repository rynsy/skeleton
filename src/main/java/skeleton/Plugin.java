package skeleton;

import io.cresco.library.agent.AgentService;

public class Plugin implements Runnable  {

    private AgentService as;
    public Plugin(AgentService as) {
        this.as = as;
    }

    public void run() {

        System.out.println("PLUGIN RUN");

        while(true) {
            try {
                as.msgIn(String.valueOf(System.nanoTime()));

                //Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
