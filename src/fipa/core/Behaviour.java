/*
  $Id$
 */

package fipa.core;

/***************************************************************

  Name: Behaviour

  Responsibilities and Collaborations:

  + Provides an abstract interface for agent behaviours, allowing
    behaviour scheduling independently of the actual kind of the
    behaviours.
    (Agent)

******************************************************************/
public interface Behaviour {

  public void execute();

}
