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

package jade.wrapper;

import java.util.EventObject;

//public class PlatformEvent extends EventObject {
public interface PlatformEvent {

    //String agentGUID, platformName;
    //int eventType;
    public static final int BORN_AGENT = 3;
    public static final int DEAD_AGENT = 4;
    public static final int STARTED_PLATFORM = 100;
    public static final int SUSPENDED_PLATFORM = 101;
    public static final int RESUMED_PLATFORM = 102;
    public static final int KILLED_PLATFORM = 103;

    /*PlatformEvent(Object anObject, int eventType, String agentGUID, String platformName) {
        super(anObject);
				this.eventType = eventType;
				this.agentGUID = agentGUID;
				this.platformName = platformName;
    }*/

    //public String getAgentGUID() {return agentGUID;}
    //public String getPlatformName() {return platformName;}
    //public int getEventType() {return eventType;}
    public String getAgentGUID();
    public String getPlatformName();
    public int getEventType();
}
