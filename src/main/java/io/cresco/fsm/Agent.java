/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.31.1.5860.78bb27cc6 modeling language!*/

package io.cresco.fsm;
import java.util.*;

// line 38 "../../model.ump"
public class Agent
{

  //------------------------
  // STATIC VARIABLES
  //------------------------

  public static final String Msga = "a";
  public static final String Msgb = "b";
  public static final String Msgc = "c";
  public static final String Msgd = "d";

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Agent Attributes
  private int agentNumber;

  //Agent Associations
  private List<Message> messages;
  private AgentStateManager agentStateManager;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Agent(int aAgentNumber, AgentStateManager aAgentStateManager)
  {
    agentNumber = aAgentNumber;
    messages = new ArrayList<Message>();
    boolean didAddAgentSystem = setAgentSystem(aAgentStateManager);
    if (!didAddAgentSystem)
    {
      throw new RuntimeException("Unable to create agent due to agentSystem. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setAgentNumber(int aAgentNumber)
  {
    boolean wasSet = false;
    agentNumber = aAgentNumber;
    wasSet = true;
    return wasSet;
  }

  public int getAgentNumber()
  {
    return agentNumber;
  }
  /* Code from template association_GetMany */
  public Message getMessage(int index)
  {
    Message aMessage = messages.get(index);
    return aMessage;
  }

  public List<Message> getMessages()
  {
    List<Message> newMessages = Collections.unmodifiableList(messages);
    return newMessages;
  }

  public int numberOfMessages()
  {
    int number = messages.size();
    return number;
  }

  public boolean hasMessages()
  {
    boolean has = messages.size() > 0;
    return has;
  }

  public int indexOfMessage(Message aMessage)
  {
    int index = messages.indexOf(aMessage);
    return index;
  }
  /* Code from template association_GetOne */
  public AgentStateManager getAgentSystem()
  {
    return agentStateManager;
  }
  /* Code from template association_MinimumNumberOfMethod */
  public static int minimumNumberOfMessages()
  {
    return 0;
  }
  /* Code from template association_AddUnidirectionalMany */
  public boolean addMessage(Message aMessage)
  {
    boolean wasAdded = false;
    if (messages.contains(aMessage)) { return false; }
    messages.add(aMessage);
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeMessage(Message aMessage)
  {
    boolean wasRemoved = false;
    if (messages.contains(aMessage))
    {
      messages.remove(aMessage);
      wasRemoved = true;
    }
    return wasRemoved;
  }
  /* Code from template association_AddIndexControlFunctions */
  public boolean addMessageAt(Message aMessage, int index)
  {  
    boolean wasAdded = false;
    if(addMessage(aMessage))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfMessages()) { index = numberOfMessages() - 1; }
      messages.remove(aMessage);
      messages.add(index, aMessage);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveMessageAt(Message aMessage, int index)
  {
    boolean wasAdded = false;
    if(messages.contains(aMessage))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfMessages()) { index = numberOfMessages() - 1; }
      messages.remove(aMessage);
      messages.add(index, aMessage);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addMessageAt(aMessage, index);
    }
    return wasAdded;
  }
  /* Code from template association_SetOneToMany */
  public boolean setAgentSystem(AgentStateManager aAgentStateManager)
  {
    boolean wasSet = false;
    if (aAgentStateManager == null)
    {
      return wasSet;
    }

    AgentStateManager existingAgentStateManager = agentStateManager;
    agentStateManager = aAgentStateManager;
    if (existingAgentStateManager != null && !existingAgentStateManager.equals(aAgentStateManager))
    {
      existingAgentStateManager.removeAgent(this);
    }
    agentStateManager.addAgent(this);
    wasSet = true;
    return wasSet;
  }

  public void delete()
  {
    messages.clear();
    AgentStateManager placeholderAgentStateManager = agentStateManager;
    this.agentStateManager = null;
    if(placeholderAgentStateManager != null)
    {
      placeholderAgentStateManager.removeAgent(this);
    }
  }

  // line 48 "../../model.ump"
  public boolean send(int agentNumber, String val){
    // Send to the other agent asynchronously
    System.out.println(agentNumber +  " Sent " + val);
    Agent recipient = getAgentSystem().findAgent(agentNumber);
    if(recipient != null) { 
      recipient.receive(val);
      return true;
    } 
    else {
      return false;
    }
  }

  // line 61 "../../model.ump"
  public boolean receive(String val){
    // Receive from the other agent and put into a queue
      addMessage(new Message(val));
      System.out.println(agentNumber +  " Received " + val);
      return true;
  }

  // line 68 "../../model.ump"
  public boolean consume(String val){
    // returns true if the first item on the queue matches val
     // deletes first item if it matches 
    if(numberOfMessages() >0)
    {
      if(getMessage(0).getVal().equals(val)) {
        removeMessage(getMessage(0));
         System.out.println(agentNumber +  " Consumed " + val);
        return(true);
      }
    }
    return(false);
  }

  // line 82 "../../model.ump"
   public boolean clock(){
    //abstract method -- does nothing; overridden by state machines
    return false;
  }


  public String toString()
  {
    return super.toString() + "["+
            "agentNumber" + ":" + getAgentNumber()+ "]" + System.getProperties().getProperty("line.separator") +
            "  " + "agentSystem = "+(getAgentSystem()!=null?Integer.toHexString(System.identityHashCode(getAgentSystem())):"null");
  }
}