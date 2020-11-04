using System;
using jade.core;
using jade.core.behaviours;
using jade.lang.acl;

namespace examples.dummy
{
  public class DummyReceiver : Agent
  {

    public override void setup()
    {
      addBehaviour(new DummyReceiveBehaviour());
      Console.WriteLine("DummyReceiver "+ getName() + " created!");
    }

    public override void takeDown()
    {
      Console.WriteLine("ReceiverDummyAgent " + getLocalName()+" deleting...");
    }
  }

	public class DummyReceiveBehaviour : SimpleBehaviour
	{
		private bool completed = false;


		public override void action()
		{
			MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage aCLMessage = myAgent.receive(messageTemplate);
			if (aCLMessage == null)
			{
				block(500);
			}
			else
			{
				Console.WriteLine("New message received from "+aCLMessage.getSender().getName());
				Console.WriteLine("Message content: "+aCLMessage.getContent());
			}
		}

		public override bool done()
		{
			return completed;
		}
	}

}
