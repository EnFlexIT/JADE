/**
 * @version $Id$
 *
 * Copyright (c) 1998 CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the agreement you entered into with CSELT.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 */

package examples.jess;

import jade.core.Agent;

/**
 * This is a simple sample JADE Agent that embeds a Jess engine.
 * It instantiates and adds only one behaviour. This behaviour is JessBehaviour
 * that only asserts messages when they arrive and use Jess as a reasoning tool
 * This agent executes the Jess code in the file examples/jess/JadeAgent.clp
 */
public class JessAgent extends Agent {

  /** 
   * adds the JessBehaviour and that's all.
   */
  protected void setup() {
    // add the behaviour
    // 1 is the number of steps that must be executed at each run of
    // the Jess engine before giving back the control to the Java code
    addBehaviour(new JessBehaviour(this,"examples/jess/JadeAgent.clp",1)); 
  }
}

