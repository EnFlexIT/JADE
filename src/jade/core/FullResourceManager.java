/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core;

//#J2ME_EXCLUDE_FILE

class FullResourceManager implements ResourceManager {
	private static final String USER_AGENTS_GROUP_NAME = "JADE User Agents";
	private static final String SYSTEM_AGENTS_GROUP_NAME = "JADE System Agents";
	private static final String CRITICAL_THREADS_GROUP_NAME = "JADE Time-critical Threads";
  
	private ThreadGroup parent;
	private ThreadGroup agentThreads;
  private ThreadGroup systemAgentThreads;
  private ThreadGroup criticalThreads;
      
    
  public FullResourceManager() {
  	parent = new ThreadGroup("FIXME: Dummy name");
  	agentThreads = new ThreadGroup(parent, USER_AGENTS_GROUP_NAME);
    agentThreads.setMaxPriority(Thread.NORM_PRIORITY);
  	
  	systemAgentThreads = new ThreadGroup(parent, SYSTEM_AGENTS_GROUP_NAME);
    systemAgentThreads.setMaxPriority(Thread.NORM_PRIORITY);
    
  	criticalThreads = new ThreadGroup(parent, CRITICAL_THREADS_GROUP_NAME);
    criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);
  }
  
  public Thread getThread(int type, String name, Runnable r) {
  	Thread t = null;
  	switch (type) {
  	case USER_AGENTS:
  		t = new Thread(agentThreads, r);
      t.setPriority(agentThreads.getMaxPriority());
  		break;
  	case SYSTEM_AGENTS:
  		t = new Thread(systemAgentThreads, r);
      t.setPriority(systemAgentThreads.getMaxPriority());
  		break;
  	case TIME_CRITICAL:
  		t = new Thread(criticalThreads, r);
      t.setPriority(criticalThreads.getMaxPriority());
  		break;
  	}
  	if (t != null) {
  		t.setName(name);
  	}
  	
  	return t;
  }
  
  public void releaseResources() {
    parent.interrupt();
    
    agentThreads = null;
    systemAgentThreads = null;
		criticalThreads = null;
		parent = null;
  }
  	
}
  	
  		
  		
  
  
      