package fipa.core;

public class StartContainer {

  public static void main(String args[]) {

    /* FIXME: handle command line options

       -platform URL
       -file     AgentList
       
    */

    // Default values for looking up the RMI registry
    String PlatformName = "FEPF";
    String PlatformHost = "localhost";
    String PlatformPort = ":1099"
    String PlatformURL = "rmi://" + PlatformHost + PlatformPort + "/" + PlatformName;

    AgentContainer theContainer = new AgentContainer(PlatformURL);

  }

}
