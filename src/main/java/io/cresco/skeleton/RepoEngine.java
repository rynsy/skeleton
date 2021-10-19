package io.cresco.skeleton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cresco.fsm.Agent;
import io.cresco.library.data.FileObject;
import io.cresco.library.data.TopicType;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import io.cresco.fsm.AgentStateManager;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RepoEngine {

    private PluginBuilder plugin;
    private CLogger logger;
    private Gson gson;

    private Type repoListType;

    private AtomicBoolean inScan = new AtomicBoolean(false);

    private AtomicBoolean lockFileMap = new AtomicBoolean();
    private Map<String, Map<String,FileObject>> fileMap;

    private AtomicBoolean lockPeerVersionMap = new AtomicBoolean();
    private Map<String, String> peerVersionMap;

    private AtomicBoolean lockPeerUpdateStateMap = new AtomicBoolean();
    private Map<String, Boolean> peerUpdateStateMap;

    private AtomicBoolean lockPeerUpdateQueueMap = new AtomicBoolean();
    private Map<String, Queue<Map<String,String>>> peerUpdateQueueMap;

    private Type mapType;

    private Timer agentScanTimer;

    private String scanDirString;

    private AtomicBoolean lockSubscriberMap = new AtomicBoolean();
    private Map<String,Map<String,String>> subscriberMap;

    private int transferId = -1;

    private List<String> listenerList;

    private String agentManagerName;
    
    private AgentStateManager agentManager;

    public RepoEngine(PluginBuilder pluginBuilder, AgentStateManager agentStateManager) {

        this.plugin = pluginBuilder;
        logger = plugin.getLogger(RepoEngine.class.getName(), CLogger.Level.Info);
        this.agentManager = agentStateManager;
        gson = new Gson();


        this.mapType = new TypeToken<Map<String,String>>() {
        }.getType();


        subscriberMap = Collections.synchronizedMap(new HashMap<>());

        listenerList = new ArrayList<>();

        peerVersionMap = Collections.synchronizedMap(new HashMap<>());
        peerUpdateStateMap = Collections.synchronizedMap(new HashMap<>());
        peerUpdateQueueMap = Collections.synchronizedMap(new HashMap<>());

        scanDirString =  plugin.getConfig().getStringParam("scan_dir");             //TODO: You'll need a similar flag.
        agentManagerName =  plugin.getConfig().getStringParam("agentmanager_name");

    }

    public void start() {
        long delay  = 5000L;
        //long period = 15000L;

        long period =  plugin.getConfig().getLongParam("scan_period", 15000L);


        // TODO: At this level you're going to need to define sender/receiver
        if((scanDirString != null) && (agentManagerName != null)) {
            logger.info("Starting file scan : " + scanDirString + " agentmanager: " + agentManagerName);
            startScan(delay, period);
            // In Filerepo it scans the local directory and looks for the listener to pass the data along to.
            /** For your project, it will look for subscribers and decide at random which agent to clock.
             *  You should have it choose a random number between 1 and AgentManager.getAgentNum(), clock it, and log the interaction.
             *      I suppose for now you should continue to send random numbers until some AgentManager.finished() flag is set
             *
             *      Todo: May need to rename these to something easier to track
             */
        } else if((scanDirString == null) && (agentManagerName != null)) {
            logger.info("Start listening for agentmanager: " + agentManagerName);
            createSubListener(agentManagerName);
            /**
             *  For your project, you should look for subscribers and see if they are sending you any data
             *  requests (clock agent in slot 2 on your fsm) and then clock the right agents. Maybe report a status.
             */

        }


    }

    // TODO: Change this into send method
    public void startScan(long delay, long period) {

        //stop scan if started
        stopScan();

        //start listening
        createRepoSubListener(agentManagerName);

        //create timer task
        TimerTask fileScanTask = new TimerTask() {
            public void run() {
                try {

                    if(plugin.isActive()) {

                            if (!inScan.get()) {

                                //logger.error("\t\t ***STARTING SCAN " + inScan.get() + " tid:" + transferId);
                                inScan.set(true);

                                //let everyone know scan is starting
                                repoBroadcast(agentManagerName,"discover");

                                logger.debug("PING Agents");
                                pingExternalAgents();

                                inScan.set(false);

                            } else {
                                logger.error("\t\t ***ALREADY IN SCAN");
                            }

                    } else {
                        logger.error("NO ACTIVE");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        agentScanTimer = new Timer("Timer");
        agentScanTimer.scheduleAtFixedRate(fileScanTask, delay, period);
        logger.debug("agentscantimer : set : " + period);
    }

    public void shutdown() {

        stopScan();
        //if(agentManagerName != null) {
        //    repoBroadcast(agentManagerName,"shutdown");
        //}
        for(String listenerid : listenerList) {
            plugin.getAgentService().getDataPlaneService().removeMessageListener(listenerid);
        }

    }

    public void stopScan() {
        if(agentScanTimer != null) {
            agentScanTimer.cancel();
            agentScanTimer = null;
        }
    }


    //TODO: This will be your sender method. Need to keep all of the subscriber management code
    private void pingExternalAgents() {
        String returnString = null;
        try {
            int subscriberCount = 0;

            List<Map<String,String>> currentSubscriberList = null;

            synchronized (lockSubscriberMap) {
                subscriberCount = subscriberMap.size();
                if (subscriberCount >0) {
                    currentSubscriberList = new ArrayList<>();
                    for (Map<String,String> subscriber : subscriberMap.values()) {
                        currentSubscriberList.add(subscriber);
                    }
                }
            }

            if(subscriberCount > 0) {
                for (Map<String, String> subscriberMap : currentSubscriberList) {

                    //This is another agentmanager in my region, I need to send it data
                    String region = subscriberMap.get("sub_region_id");
                    String agent = subscriberMap.get("sub_agent_id");
                    String pluginID = subscriberMap.get("sub_plugin_id");

                    logger.debug("SEND :" + region + " " + agent + " " + pluginID + " data");

                    MsgEvent agentManagerRequest = plugin.getGlobalPluginMsgEvent(MsgEvent.Type.EXEC, region, agent, pluginID);
                    agentManagerRequest.setParam("action", "pingagent");
                    agentManagerRequest.setParam("fsm_agent_id", "1"); //TODO: Change this to target specific agents

                    MsgEvent agentManagerResponse = plugin.sendRPC(agentManagerRequest);

                    if (agentManagerResponse != null) {

                        logger.debug("Host Region: " + region + " Agent: " + agent + " pluginId:" + pluginID + " responded");

                        if (agentManagerResponse.paramsContains("status_code") && agentManagerResponse.paramsContains("status_desc")) {
                            int status_code = Integer.parseInt(agentManagerResponse.getParam("status_code"));
                            String status_desc = agentManagerResponse.getParam("status_code");
                            if (status_code != 10) {
                                logger.error("Region: " + region + " Agent: " + agent + " pluginId:" + pluginID + " agentmanager update failed status_code: " + status_code + " status_desc:" + status_desc);
                            } else {
                                // TODO: I think here you'll need to change the code above and then do the send/ping here.
                                boolean fsm_status_desc = Boolean.getBoolean(agentManagerResponse.getParam("fsm_agent_status"));
                                logger.info("Pinged FsmAgent # 1 on Plugin " + pluginID);
                                if (!fsm_status_desc) {
                                   // FSM Agent 1 is exhausted, need to remove it from the sub list
                                    removeSubscribe(subscriberMap);
                                }
                            }
                        }


                    } else {
                        logger.error("Host Region: " + region + " Agent: " + agent + " pluginId:" + pluginID + " failed to respond!");
                        logger.error("Removing Host Region: " + region + " Agent: " + agent + " pluginId:" + pluginID);
                        removeSubscribe(subscriberMap);
                    }

                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //sub functions
    private void updateSubscribe(Map<String, String> incomingMap) {

        try {
            if ((incomingMap.containsKey("repo_region_id")) && (incomingMap.containsKey("repo_agent_id")) && (incomingMap.containsKey("repo_plugin_id"))) {

                //don't include self
                if (!((plugin.getRegion().equals(incomingMap.get("repo_region_id"))) && (plugin.getAgent().equals(incomingMap.get("repo_agent_id"))) && (plugin.getPluginID().equals(incomingMap.get("repo_plugin_id"))))) {
                    subMessage(agentManagerName, incomingMap.get("repo_region_id"), incomingMap.get("repo_agent_id"), incomingMap.get("repo_plugin_id"), "subscribe");
                }

            } else {
                logger.error("not agent identification provided");
            }
        } catch (Exception ex) {
            logger.error("Failed to subscribe");
            logger.error(ex.getMessage());
        }

    }

    //sub functions
    private void pingAgentAndUpdate(Map<String, String> incomingMap) {

        try {
            if ((incomingMap.containsKey("repo_region_id")) && (incomingMap.containsKey("repo_agent_id")) && (incomingMap.containsKey("repo_plugin_id"))) {

                //don't include self
                if (!((plugin.getRegion().equals(incomingMap.get("repo_region_id"))) && (plugin.getAgent().equals(incomingMap.get("repo_agent_id"))) && (plugin.getPluginID().equals(incomingMap.get("repo_plugin_id"))))) {
                    // TODO: ping the agent
                    fsmMessage(agentManagerName, incomingMap.get("fsm_agent_id"), incomingMap.get("repo_region_id"), incomingMap.get("repo_agent_id"), incomingMap.get("repo_plugin_id"), "pingagent");
                }

            } else {
                logger.error("not agent identification provided");
            }
        } catch (Exception ex) {
            logger.error("Failed to subscribe");
            logger.error(ex.getMessage());
        }

    }

    // TODO: I believe this will be the Receiver method
    private void createSubListener(String agentmanagerName) {


        javax.jms.MessageListener ml = new javax.jms.MessageListener() {
            public void onMessage(Message msg) {
                try {

                    if (msg instanceof TextMessage) {
                        logger.debug(" SUB REC MESSAGE:" + ((TextMessage) msg).getText());
                        Map<String, String> incomingMap = gson.fromJson(((TextMessage) msg).getText(), mapType);
                        if(incomingMap.containsKey("action")) {
                            if(incomingMap.containsKey("agentmanager_name")) {

                                String actionType = incomingMap.get("action");
                                switch (actionType) {
                                    case "discover":
                                        updateSubscribe(incomingMap);
                                        break;
// TODO add action to switch
                                    case "pingagent":
                                        //TODO: add handler
                                        pingAgentAndUpdate(incomingMap);
                                        break;

                                    default:
                                        logger.error("unknown actionType: " + actionType);
                                        break;
                                }
                            } else {
                                logger.error("action called without agentmanager_name");
                            }
                        } else {
                            logger.error("createSubListener no action in message");
                            logger.error(incomingMap.toString());
                        }

                    }
                } catch(Exception ex) {

                    ex.printStackTrace();
                }
            }
        };

        String queryString = "agentmanager_name='" + agentmanagerName + "' AND broadcast";
        String node_from_listner_id = plugin.getAgentService().getDataPlaneService().addMessageListener(TopicType.AGENT,ml,queryString);

        listenerList.add(node_from_listner_id);

    }

    public void fsmMessage(String agentmanagerName, String fsmAgentId, String regionId, String agentId, String pluginId, String action) {

        try {

            Agent current = agentManager.findAgent(Integer.parseInt(fsmAgentId));
            boolean status = current.clock();

            Map<String,String> update = new HashMap<>();
            update.put("action",action);
            update.put("agentmanager_name",agentmanagerName);
            update.put("sub_region_id", plugin.getRegion());
            update.put("sub_agent_id",plugin.getAgent());
            update.put("sub_plugin_id",plugin.getPluginID());

            TextMessage updateMessage = plugin.getAgentService().getDataPlaneService().createTextMessage();
            updateMessage.setText(gson.toJson(update));
            updateMessage.setStringProperty("agentmanager_name",agentmanagerName);
            updateMessage.setStringProperty("region_id",regionId);
            updateMessage.setStringProperty("agent_id",agentId);
            updateMessage.setStringProperty("plugin_id",pluginId);
            updateMessage.setStringProperty("fsm_agent_id",fsmAgentId);
            updateMessage.setStringProperty("fsm_agent_status", String.valueOf(status));

            plugin.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT,updateMessage);


        } catch (Exception ex) {
            logger.error("failed to update subscribers");
            logger.error(ex.getMessage());
        }

    }


    public void subMessage(String agentmanagerName, String regionId, String agentId, String pluginId, String action) {

        try {

            Map<String,String> update = new HashMap<>();
            update.put("action",action);
            update.put("agentmanager_name",agentmanagerName);
            update.put("sub_region_id", plugin.getRegion());
            update.put("sub_agent_id",plugin.getAgent());
            update.put("sub_plugin_id",plugin.getPluginID());

            TextMessage updateMessage = plugin.getAgentService().getDataPlaneService().createTextMessage();
            updateMessage.setText(gson.toJson(update));
            updateMessage.setStringProperty("agentmanager_name",agentmanagerName);
            updateMessage.setStringProperty("region_id",regionId);
            updateMessage.setStringProperty("agent_id",agentId);
            updateMessage.setStringProperty("plugin_id",pluginId);

            plugin.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT,updateMessage);


        } catch (Exception ex) {
            logger.error("failed to update subscribers");
            logger.error(ex.getMessage());
        }

    }


    //repo functions
    public void repoBroadcast(String agentmanagerName, String action) {

        try {

            Map<String,String> update = new HashMap<>();
            update.put("action",action);
            update.put("agentmanager_name",agentmanagerName);
            update.put("repo_region_id", plugin.getRegion());
            update.put("repo_agent_id",plugin.getAgent());
            update.put("repo_plugin_id",plugin.getPluginID());

            TextMessage updateMessage = plugin.getAgentService().getDataPlaneService().createTextMessage();
            updateMessage.setText(gson.toJson(update));
            updateMessage.setStringProperty("agentmanager_name",agentmanagerName);
            updateMessage.setBooleanProperty("broadcast",Boolean.TRUE);

            plugin.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT,updateMessage);
            logger.debug("SENDING MESSAGE: " + update.toString());

        } catch (Exception ex) {
            logger.error("failed to update subscribers");
            logger.error(ex.getMessage());
        }

    }

    private void createRepoSubListener(String agentmanagerName) {
        javax.jms.MessageListener ml = new javax.jms.MessageListener() {
            public void onMessage(Message msg) {
                try {

                    if (msg instanceof TextMessage) {
                        logger.debug(" REPO REC MESSAGE:" + ((TextMessage) msg).getText());
                        Map<String, String> incomingMap = gson.fromJson(((TextMessage) msg).getText(), mapType);
                        if(incomingMap.containsKey("action")) {
                            if(incomingMap.containsKey("agentmanager_name")) {

                                String actionType = incomingMap.get("action");
                                switch (actionType) {
                                    case "subscribe":
                                        addSubscribe(incomingMap);
                                        break;
                                    case "unsubscribe":
                                        removeSubscribe(incomingMap);
                                        break;

                                    default:
                                        logger.error("unknown actionType: " + actionType);
                                        break;
                                }


                            } else {
                                logger.error("action called without agentmanager_name");
                            }
                        } else {
                            logger.error("createRepoSubListener no action in message");
                        }

                    }
                } catch(Exception ex) {

                    ex.printStackTrace();
                }
            }
        };

        String queryString = "agentmanager_name='" + agentmanagerName + "' AND region_id='" + plugin.getRegion() + "' AND agent_id='" + plugin.getAgent() + "' AND plugin_id='" + plugin.getPluginID() + "'";
        String node_from_listner_id = plugin.getAgentService().getDataPlaneService().addMessageListener(TopicType.AGENT,ml,queryString);

        listenerList.add(node_from_listner_id);

    }

    private String generateSubKey(Map<String, String> incomingMap) {
        String subKey = null;
        try {
                if ((incomingMap.containsKey("sub_region_id")) && (incomingMap.containsKey("sub_agent_id")) && (incomingMap.containsKey("sub_plugin_id"))) {

                    subKey = incomingMap.get("sub_region_id") + "_" + incomingMap.get("sub_agent_id") + "_" + incomingMap.get("sub_plugin_id");

                }

            } catch (Exception ex) {
            logger.error("could not generate sub key");
            logger.error(ex.getMessage());
        }

        return subKey;
    }

    private void addSubscribe(Map<String, String> incomingMap) {

        try {
            if ((incomingMap.containsKey("sub_region_id")) && (incomingMap.containsKey("sub_agent_id")) && (incomingMap.containsKey("sub_plugin_id"))) {

                    //don't include self
                    if (!((plugin.getRegion().equals(incomingMap.get("sub_region_id"))) && (plugin.getAgent().equals(incomingMap.get("sub_agent_id"))) && (plugin.getPluginID().equals(incomingMap.get("sub_plugin_id"))))) {
                        String subKey = generateSubKey(incomingMap);
                        if(subKey != null) {
                            synchronized (lockSubscriberMap) {
                                if (subscriberMap.containsKey(subKey)) {
                                    subscriberMap.get(subKey).put("ts", String.valueOf(System.currentTimeMillis()));
                                } else {
                                    incomingMap.put("ts", String.valueOf(System.currentTimeMillis()));
                                    subscriberMap.put(subKey, incomingMap);
                                }
                            }
                        }
                    }

            } else {
                logger.error("not agent identification provided");
            }
        } catch (Exception ex) {
            logger.error("Failed to subscribe");
            logger.error(ex.getMessage());
        }


    }

    private void removeSubscribe(Map<String, String> incomingMap) {

        try {

            if ((incomingMap.containsKey("sub_region_id")) && (incomingMap.containsKey("sub_agent_id")) && (incomingMap.containsKey("sub_plugin_id"))) {

                //don't include self
                if (!((plugin.getRegion().equals(incomingMap.get("sub_region_id"))) && (plugin.getAgent().equals(incomingMap.get("sub_agent_id"))) && (plugin.getPluginID().equals(incomingMap.get("sub_plugin_id"))))) {
                    String subKey = generateSubKey(incomingMap);
                    if(subKey != null) {
                        synchronized (lockSubscriberMap) {

                            subscriberMap.remove(subKey);

                        }
                    }
                }

            } else {
                logger.error("not agent identification provided");
            }

        } catch (Exception ex) {
            logger.error("Failed to unsubscribe");
            logger.error(ex.getMessage());
        }

    }
}
