/*
  $Log$
  Revision 1.11  1999/08/27 15:43:50  rimassa
  Added transactional locking support for agent migration.

  Revision 1.10  1999/07/13 20:01:38  rimassa
  Removed useless code.

  Revision 1.9  1999/03/17 12:58:01  rimassa
  Changed AgentDescriptor responsibilities: now the class is not
  Serializable any more, so that AMS descriptors are not uselessly
  transferred across the network.
  RemoteProxy objects carry this duty now.

  Revision 1.8  1999/03/09 12:56:18  rimassa
  Added code to hold container name inside an AgentDescriptor object.

  Revision 1.7  1998/11/02 01:56:20  rimassa
  Removed every reference to MessageDispatcher class; now an
  AgentDescriptor uses AgentContainer directly.

  Revision 1.6  1998/10/04 18:00:58  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;


class AgentDescriptor {

  private RemoteProxy proxy;
  private String containerName;
  private boolean locked = false;

  public void setProxy(RemoteProxy rp) {
    proxy = rp;
  }

  public RemoteProxy getProxy() {
    return proxy;
  }

  public void setContainerName(String name) {
    containerName = name;
  }

  public String getContainerName() {
    return containerName;
  }

  public synchronized void lock() {
    while(locked) {
      try {
	wait();
      }
      catch(InterruptedException ie) {
	ie.printStackTrace();
      }
    }
    locked = true;
  }

  public synchronized void unlock() {
    locked = false;
    notifyAll();
  }

}
