package test.inProcess;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.AID;
import jade.wrapper.*;
import jade.util.leap.*;
import test.common.*;
import java.io.*;
import java.net.InetAddress;

/**
   @author Fabio Bellifemine - TILAB
 */
public class TesterAgent extends test.common.TesterAgent {

	protected TestGroup getTestGroup() {
			return (new TestGroup("test/inProcess/inProcessTestsList.xml"));
	}

	
}
