package test.wrapper;

import jade.wrapper.*;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

class TestListener implements PlatformController.Listener {
        public void bornAgent(PlatformEvent anEvent) {
        	System.out.println(anEvent);
        }

        /**
         * Called when an agent dies. PlatformEvent source is AgentController.
         */
        public void deadAgent(PlatformEvent anEvent) {
        	System.out.println(anEvent);
        }
        
        /**
         * Called when the platform is started. PlatformEvent source is PlatformController.
         */
        public void startedPlatform(PlatformEvent anEvent) {
         	System.out.println(anEvent);
        }
       
        /**
         * Called when the platform is suspended. PlatformEvent source is PlatformController.
         */
        public void suspendedPlatform(PlatformEvent anEvent) {
        	System.out.println(anEvent);
        }
        
        /**
         * Called when the platform is activated. PlatformEvent source is PlatformController.
         */
        public void resumedPlatform(PlatformEvent anEvent) {
        	System.out.println(anEvent);
        }

        /**
         * Called when the platform is killed (destroyed). PlatformEvent source is PlatformController.
         */
        public void killedPlatform(PlatformEvent anEvent) {
        	System.out.println(anEvent);
        }
        
  public static void main(String args[]) {

    try {

      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      // Launch a complete platform on the 8888 port
      // create a default Profile 
      Profile pMain = new ProfileImpl(null, 8888, null);

      System.out.println("Launching a whole in-process platform..."+pMain);
      MainContainer mc = rt.createMainContainer(pMain);
      mc.addPlatformListener(new TestListener());

      // set now the default Profile to start a container
      ProfileImpl pContainer = new ProfileImpl(null, 8888, null);
      System.out.println("Launching the agent container ..."+pContainer);
      AgentContainer cont = rt.createAgentContainer(pContainer);

      System.out.println("Launching the rma agent on the main container ...");
      Agent rma = (Agent) mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

}