/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.31.1.5860.78bb27cc6 modeling language!*/

package io.cresco.fsm;
import java.util.*;

/**
 * This is the output for the distributed state-machine example from Umple with a few modifications.
 * Using this as a base to work with, but including this in the repo for reference.
 */

// line 6 "../../model.ump"
public class AgentStateManager
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //AgentSystem Associations
  private List<Agent> agents;
  private String name;
  private String incoming;
  private String outgoing;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public AgentStateManager(String agentName, String incomingPath, String outgoingPath)
  {
    agents = new ArrayList<Agent>();
    name = agentName;
    incoming = incomingPath;
    outgoing = outgoingPath;
  }

  //------------------------
  // INTERFACE
  //------------------------
  /* Code from template association_GetMany */
  public Agent getAgent(int index)
  {
    Agent aAgent = agents.get(index);
    return aAgent;
  }

  public List<Agent> getAgents()
  {
    List<Agent> newAgents = Collections.unmodifiableList(agents);
    return newAgents;
  }

  public int numberOfAgents()
  {
    int number = agents.size();
    return number;
  }

  public boolean hasAgents()
  {
    boolean has = agents.size() > 0;
    return has;
  }

  public int indexOfAgent(Agent aAgent)
  {
    int index = agents.indexOf(aAgent);
    return index;
  }
  /* Code from template association_MinimumNumberOfMethod */
  public static int minimumNumberOfAgents()
  {
    return 0;
  }
  /* Code from template association_AddManyToOne */
  public Agent addAgent(int aAgentNumber)
  {
    return new Agent(aAgentNumber, this);
  }

  public boolean addAgent(Agent aAgent)
  {
    boolean wasAdded = false;
    if (agents.contains(aAgent)) { return false; }
    AgentStateManager existingAgentStateManager = aAgent.getAgentSystem();
    boolean isNewAgentSystem = existingAgentStateManager != null && !this.equals(existingAgentStateManager);
    if (isNewAgentSystem)
    {
      aAgent.setAgentSystem(this);
    }
    else
    {
      agents.add(aAgent);
    }
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeAgent(Agent aAgent)
  {
    boolean wasRemoved = false;
    //Unable to remove aAgent, as it must always have a agentSystem
    if (!this.equals(aAgent.getAgentSystem()))
    {
      agents.remove(aAgent);
      wasRemoved = true;
    }
    return wasRemoved;
  }
  /* Code from template association_AddIndexControlFunctions */
  public boolean addAgentAt(Agent aAgent, int index)
  {  
    boolean wasAdded = false;
    if(addAgent(aAgent))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfAgents()) { index = numberOfAgents() - 1; }
      agents.remove(aAgent);
      agents.add(index, aAgent);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveAgentAt(Agent aAgent, int index)
  {
    boolean wasAdded = false;
    if(agents.contains(aAgent))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfAgents()) { index = numberOfAgents() - 1; }
      agents.remove(aAgent);
      agents.add(index, aAgent);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addAgentAt(aAgent, index);
    }
    return wasAdded;
  }

  public void delete()
  {
    for(int i=agents.size(); i > 0; i--)
    {
      Agent aAgent = agents.get(i - 1);
      aAgent.delete();
    }
  }

  // line 12 "../../model.ump"
  public Agent findAgent(int num){
    for(Agent agent : getAgents()) {
       if(agent.getAgentNumber() == num) {
          return(agent);
       }
     }
     return null;
  }

  // line 21 "../../model.ump"
   //public static  void main(String [] args){
   public void run(){
    Thread.currentThread().setUncaughtExceptionHandler(new UmpleExceptionHandler());
    Thread.setDefaultUncaughtExceptionHandler(new UmpleExceptionHandler());
    int randomIndex;
    this.addAgent(new AgentA(1, this));
    this.addAgent(new AgentB(2, this));

    // TODO: Separate this into two roles, have roles assigned by parameter during upload
     // One should randomly choose an agent to advance, and send the message over the dataplane to the other AgentStateManager
    Random r = new Random();
    
    for(int i=0; i<1000; i++) 
    {
      // clock either agent depending on parity of a random number generator
      int theNext =Math.abs(r.nextInt());
      randomIndex = theNext % 2;
      //System.out.println("Int was "+theNext+ "Next random index = " + randomIndex);
      this.getAgent(randomIndex).clock();
    }
  }

  public static class UmpleExceptionHandler implements Thread.UncaughtExceptionHandler
  {
    public void uncaughtException(Thread t, Throwable e)
    {
      translate(e);
      if(e.getCause()!=null)
      {
        translate(e.getCause());
      }
      e.printStackTrace();
    }
    public void translate(Throwable e)
    {
      List<StackTraceElement> result = new ArrayList<StackTraceElement>();
      StackTraceElement[] elements = e.getStackTrace();
      try
      {
        for(StackTraceElement element:elements)
        {
          String className = element.getClassName();
          String methodName = element.getMethodName();
          boolean methodFound = false;
          int index = className.lastIndexOf('.')+1;
          try {
            java.lang.reflect.Method query = this.getClass().getMethod(className.substring(index)+"_"+methodName,new Class[]{});
            UmpleSourceData sourceInformation = (UmpleSourceData)query.invoke(this,new Object[]{});
            for(int i=0;i<sourceInformation.size();++i)
            {
              // To compensate for any offsets caused by injected code we need to loop through the other references to this function
              //  and adjust the start / length of the function.
              int functionStart = sourceInformation.getJavaLine(i) + (("main".equals(methodName))?3:1);
              int functionEnd = functionStart + sourceInformation.getLength(i);
              int afterInjectionLines = 0;
              //  We can leverage the fact that all inject statements are added to the uncaught exception list 
              //   before the functions that they are within
              for (int j = 0; j < i; j++) {
                if (sourceInformation.getJavaLine(j) - 1 >= functionStart &&
                    sourceInformation.getJavaLine(j) - 1 <= functionEnd &&
                    sourceInformation.getJavaLine(j) - 1 <= element.getLineNumber()) {
                    // A before injection, +2 for the comments surrounding the injected code
                    if (sourceInformation.getJavaLine(j) - 1 == functionStart) {
                        functionStart += sourceInformation.getLength(j) + 2;
                        functionEnd += sourceInformation.getLength(j) + 2;
                    } else {
                        // An after injection
                        afterInjectionLines += sourceInformation.getLength(j) + 2;
                        functionEnd += sourceInformation.getLength(j) + 2;
                    }
                }
              }
              int distanceFromStart = element.getLineNumber() - functionStart - afterInjectionLines;
              if(distanceFromStart>=0&&distanceFromStart<=sourceInformation.getLength(i))
              {
                result.add(new StackTraceElement(element.getClassName(),element.getMethodName(),sourceInformation.getFileName(i),sourceInformation.getUmpleLine(i)+distanceFromStart));
                methodFound = true;
                break;
              }
            }
          }
          catch (Exception e2){}
          if(!methodFound)
          {
            result.add(element);
          }
        }
      }
      catch (Exception e1)
      {
        e1.printStackTrace();
      }
      e.setStackTrace(result.toArray(new StackTraceElement[0]));
    }
  //The following methods Map Java lines back to their original Umple file / line    
    public UmpleSourceData AgentSystem_findAgent(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(11).setJavaLines(143).setLengths(6);}
    public UmpleSourceData AgentSystem_main(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(20).setJavaLines(153).setLengths(14);}
    public UmpleSourceData Agent_receive(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(60).setJavaLines(199).setLengths(4);}
    public UmpleSourceData Agent_consume(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(67).setJavaLines(207).setLengths(11);}
    public UmpleSourceData Agent_clock(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(81).setJavaLines(222).setLengths(2);}
    public UmpleSourceData Agent_send(){ return new UmpleSourceData().setFileNames("model.ump").setUmpleLines(47).setJavaLines(185).setLengths(10);}
    public UmpleSourceData AgentA_setSm(){ return new UmpleSourceData().setFileNames("model.ump","model.ump").setUmpleLines(91, 97).setJavaLines(91, 95).setLengths(1, 1);}
    public UmpleSourceData AgentA_clock(){ return new UmpleSourceData().setFileNames("model.ump","model.ump").setUmpleLines(101, 104).setJavaLines(61, 70).setLengths(1, 1);}
    public UmpleSourceData AgentB_setSm(){ return new UmpleSourceData().setFileNames("model.ump","model.ump").setUmpleLines(115, 126).setJavaLines(91, 95).setLengths(1, 1);}
    public UmpleSourceData AgentB_clock(){ return new UmpleSourceData().setFileNames("model.ump","model.ump").setUmpleLines(119, 123).setJavaLines(57, 66).setLengths(1, 1);}

  }
  public static class UmpleSourceData
  {
    String[] umpleFileNames;
    Integer[] umpleLines;
    Integer[] umpleJavaLines;
    Integer[] umpleLengths;
    
    public UmpleSourceData(){
    }
    public String getFileName(int i){
      return umpleFileNames[i];
    }
    public Integer getUmpleLine(int i){
      return umpleLines[i];
    }
    public Integer getJavaLine(int i){
      return umpleJavaLines[i];
    }
    public Integer getLength(int i){
      return umpleLengths[i];
    }
    public UmpleSourceData setFileNames(String... filenames){
      umpleFileNames = filenames;
      return this;
    }
    public UmpleSourceData setUmpleLines(Integer... umplelines){
      umpleLines = umplelines;
      return this;
    }
    public UmpleSourceData setJavaLines(Integer... javalines){
      umpleJavaLines = javalines;
      return this;
    }
    public UmpleSourceData setLengths(Integer... lengths){
      umpleLengths = lengths;
      return this;
    }
    public int size(){
      return umpleFileNames.length;
    }
  }
}