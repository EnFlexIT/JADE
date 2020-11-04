using System;
using jade.core;
using jade.core.behaviours;
using jade.lang.acl;

namespace examples.dummy
{
  public class DummySender : Agent
  {

    public override void setup()
    {
	  object[] args = getArguments();
      addBehaviour(new DummySendBehaviour(args));
      Console.WriteLine("DummySender "+getName()+" created!");
    }

    public override void takeDown()
    {
      Console.WriteLine("DummySender "+getLocalName()+" deleting...");
    }
  }

	public class DummySendBehaviour : OneShotBehaviour
	{
		private object[] args;

		public DummySendBehaviour(object[] args)
		{
			this.args = args;
		}

		public override void action()
		{
			ACLMessage aCLMessage = new ACLMessage(ACLMessage.INFORM);
			
			AID aID = null;

			if (args != null && args.Length == 1)
			{
				string name = args[0] as string;
				aID = new AID(name, AID.ISLOCALNAME);
			}
			else if (args != null && args.Length == 2)
			{
				string name = args[0] as string;
				string host = args[1] as string;
				aID = new AID(name+"@"+host+":1099/JADE", AID.ISGUID);
				aID.addAddresses("http://"+host+":7778/acc");
			}
			else
				aID = new AID("dummyReceiver", AID.ISLOCALNAME);
			
			aCLMessage.addReceiver(aID);
			aCLMessage.setSender(myAgent.getAID());
			aCLMessage.setContent("Prova contenuto");
			myAgent.send(aCLMessage);
			Console.WriteLine("AID is "+aID.getName());
			Console.WriteLine("Message sent");
			myAgent.doDelete();
		}
	}

}
