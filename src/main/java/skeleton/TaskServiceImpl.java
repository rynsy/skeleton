package skeleton;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.cody.task.Task;
import io.cody.task.TaskService;
import io.cresco.library.agent.AgentService;
import io.cresco.library.plugin.PluginBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogService;

/*
@Service(classes= TaskService.class,
        properties= {
                // Only necessary for Remote Services
                @ServiceProperty(name = "service.exported.interfaces", values = "*")
        })
@Singleton
*/

/*
@Component(
        name = "Task Service" ,
        configurationPid = { "io.cresco.configuration.factory" },
        service = { TaskService.class },
        scope=ServiceScope.PROTOTYPE
)
*/

//@Component(factory="io.cresco.configuration.factory")

/*
@Component(
        service = { AgentService.class} ,
        immediate = true,
        reference=@Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)

)
 */

@Component(
        name = "Task Service" ,
        configurationPid = { "io.cresco.configuration.factory" },
        service = { TaskService.class },
        scope=ServiceScope.PROTOTYPE,
        servicefactory = true,
        //immediate = true,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service=AgentService.class)
)

public class TaskServiceImpl implements TaskService  {
    Map<String, Task> taskMap;

    public BundleContext context;


    private AgentService agentService;

    /*
    public TaskServiceImpl() {
        taskMap = new HashMap<String, Task>();
        Task task = new Task();
        task.setId("1");
        task.setTitle("Buy some coffee");
        task.setDescription("Take the extra strong");
        addTask(task);

        task = new Task();
        task.setId("2");
        task.setTitle("Finish karaf tutorial");
        task.setDescription("Last check and wiki upload");
        addTask(task);

    }
    */

    /*
    @Reference(
            name = "io.cresco.library.agent.AgentService",
            service = AgentService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(AgentService agentService) {
        this.agentService = agentService;
    }

    protected void unsetHttpService(AgentService agentService) {
        this.agentService = null;
    }
    */

    @Activate
    void activate(BundleContext context, Map<String,Object> map) {

        this.context = context;

        /*
        ServiceReference sr = context.getServiceReference(AgentService.class.getName());

        boolean assign = sr.isAssignableTo(context.getBundle(), AgentService.class.getName());

        if(assign) {
            AgentService agentService = (AgentService)context.getService(sr);
            System.out.println("Agentid: " + agentService.getAgent().getId());
        }
        */


        //context.registerService(TaskService.class,this,null);

        taskMap = new HashMap<String, Task>();
        Task task = new Task();
        task.setId("1");
        task.setTitle("My Plugin Assignment is : " + (String)map.get("pluginID"));
        task.setDescription("Take the extra strong");
        addTask(task);

        /*
        ServiceReference sr = context.getServiceReference(AgentService.class.getName());

        if(sr != null) {
            boolean assign = sr.isAssignableTo(context.getBundle(), AgentService.class.getName());

            if (assign) {
                AgentService agentService = (AgentService) context.getService(sr);
                //System.out.println("Agentid: " + agentService.getAgent().getId());
                agentService.getAgent().sendMessage((String)map.get("pluginID"));
            }
        } else {
            System.out.println("Can't Find :" + AgentService.class.getName());
        }
        */

        /*
        ServiceReference ref = context.getServiceReference(LogService.class.getName());
        if (ref != null)
        {
            LogService log = (LogService) context.getService(ref);
            log.log(0,"!!!!Dfdfd!!!!");
            // Use the log...
                    }
        */

        /*
        ServiceReference ref = context.getServiceReference(LogService.class.getName());
        if (ref != null)
        {
            boolean assign = ref.isAssignableTo(context.getBundle(), LogService.class.getName());
            if(assign) {
                LogService logService = (LogService) context.getService(ref);
            }
        }
        */

        try {
            PluginBuilder plugin = new PluginBuilder(context, map);
            plugin.getAgentService().getAgentState().sendMessage("AGENT: " + (String) map.get("pluginID"));
            if(plugin.getLogService() == null) {
                System.out.println("Why is this service null?");
            } else {
                System.out.println("LOG NOT NULL!!!");
            }

            plugin.getLogService().log(0,"CODY !!!!");


            MessageSender messageSender = new MessageSender(plugin);
            new Thread(messageSender).start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }
    /*
    @Activate
    protected void Activate(Configuration config) {
        //boolean enabled = config.servletname_enabled();
        System.out.println("CONFIG : " + config.getBundleLocation());
    }
    */


    @Override
    public Task getTask(String id) {
        return taskMap.get(id);
    }

    @Override
    public void addTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    @Override
    public Collection<Task> getTasks() {
        // taskMap.values is not serializable
        return new ArrayList<Task>(taskMap.values());
    }

    @Override
    public void updateTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    @Override
    public void deleteTask(String id) {
        taskMap.remove(id);
    }

}