
package jade.core;

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
