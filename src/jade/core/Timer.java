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

import jade.util.leap.Comparable;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
class Timer implements Comparable {

  private long expireTimeMillis;
  private boolean expired;
  private Agent owner;

  public Timer(long when, Agent a) {
    expireTimeMillis = when;
    owner = a;
    expired = false;
  }

  public int compareTo(Object o) {
    if(equals(o))
      return 0;
    else {
      Timer t = (Timer)o;
      return (expireTimeMillis <= t.expireTimeMillis) ? -1 : 1;
    }
  }

  public boolean equals(Object o) {
    Timer t = (Timer)o;
    return (expireTimeMillis == t.expireTimeMillis);
  }


  // Called by the TimerDispatcher

  boolean isExpired() {

    boolean oldExpired = expired;
    expired |= (expireTimeMillis < System.currentTimeMillis());
    // Edge triggered action
    if(!oldExpired && expired)
      owner.doTimeOut(this);

    return expired;
  }

  long expirationTime() {
    return expireTimeMillis;
  }

}
