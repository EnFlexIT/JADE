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

public class ResourceManager {
	public static final int USER_AGENTS = 0;
	public static final int SYSTEM_AGENTS = 1;
	public static final int CRITICAL = 2;
	
  private static ThreadGroup agentThreads = new ThreadGroup("JADE Agents");
  private static ThreadGroup systemAgentThreads = new ThreadGroup("JADE Management Agents");
  private static ThreadGroup criticalThreads = new ThreadGroup("JADE time-critical threads");
      
      
  static {
      agentThreads.setMaxPriority(Thread.NORM_PRIORITY);
      systemAgentThreads.setMaxPriority(Thread.NORM_PRIORITY);
      criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);
  }
  
  public static Thread getThread(int type, Runnable r) {
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
  	case CRITICAL:
  		t = new Thread(criticalThreads, r);
      t.setPriority(criticalThreads.getMaxPriority());
  		break;
  	}
  	
  	return t;
  }
}
  	
  		
  		
  
  
      