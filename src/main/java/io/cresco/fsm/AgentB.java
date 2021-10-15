/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.31.1.5860.78bb27cc6 modeling language!*/

package io.cresco.fsm;

// line 111 "../../model.ump"
public class AgentB extends Agent
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //AgentB State Machines
  public enum Sm { s1, s2, s3, s4, s5 }
  private Sm sm;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public AgentB(int aAgentNumber, AgentStateManager aAgentStateManager)
  {
    super(aAgentNumber, aAgentStateManager);
    setSm(Sm.s1);
  }

  //------------------------
  // INTERFACE
  //------------------------

  public String getSmFullName()
  {
    String answer = sm.toString();
    return answer;
  }

  public Sm getSm()
  {
    return sm;
  }

  public boolean clock()
  {
    boolean wasEventProcessed = false;
    
    Sm aSm = sm;
    switch (aSm)
    {
      case s1:
        setSm(Sm.s2);
        wasEventProcessed = true;
        break;
      case s2:
        if (consume(Msga))
        {
          setSm(Sm.s3);
          wasEventProcessed = true;
          break;
        }
        break;
      case s3:
        if (consume(Msgb))
        {
          setSm(Sm.s4);
          wasEventProcessed = true;
          break;
        }
        break;
      case s4:
        setSm(Sm.s5);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  private void setSm(Sm aSm)
  {
    sm = aSm;

    // entry actions and do activities
    switch(sm)
    {
      case s1:
        // line 116 "../../model.ump"
        send(1, Msgc);
        break;
      case s4:
        // line 127 "../../model.ump"
        send(1, Msgd);
        break;
    }
  }

  public void delete()
  {
    super.delete();
  }

}