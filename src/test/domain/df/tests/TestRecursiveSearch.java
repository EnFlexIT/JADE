/*****************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *****************************************************************/

package test.domain.df.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import test.common.*;
import test.domain.df.*;

import java.util.Vector;


/** This test create different DF registered one to each other. Different search
 * are executed.
 * The maxdepth of the search in the graph formed by the DFS, the number of result returned
 * by the search and other parameters are consider in this test.
 * See document
 *     title: Fipa Agent Management Specification
 * 	number:SC00023J
 * for more details.
 *
 * @author Alessandro Chiarotto - TILAB
 */
public class TestRecursiveSearch extends Test {
    // DF agent forming a federation
    // The graph is compose by the following vertex
    // v1=(df1,df2)   "df1 is registered on df2"
    // v2=(df2,df3)
    // v3=(df2,df4)
    // v4=(df4,df5)
    // v5=(df3,df1)
    
    
    private jade.wrapper.AgentContainer mc = null;
    private String[] arrayAIDdf = {"df1","df2","df3","df4","df5","df6"};
    //private AID df1,df2,df3,df4,df5,df6; // df AID
    private AID dfFederatorAID;
    public String getName() {
        return "Test RecursiveSearch";
    }
    
    public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
        final Agent finalA = a;
        final DataStore store = ds;
        final String key = resultKey;
        final Logger l = Logger.getLogger();
        
        
        // Test initialisation
        // create the DFs
        for(int i = 0; i < arrayAIDdf.length; i++) {
            TestUtility.createAgent(a, arrayAIDdf[i], "jade.domain.df", null, a.getAMS(), null);
        }
        
        String args[] = new String[1];
        // FIXME: lasciare solo il nome del file properties quando il DFFederator riesce a caricarlo...
        args[0] = "..\\..\\..\\jade\\src\\test\\domain\\df\\tests\\TestRecursiveSearch.properties";
        
        dfFederatorAID = TestUtility.createAgent(a, "DFFederator", "jade.misc.DFFederatorAgent", args, a.getAMS(), null);
 
        // Register a set of agent to each df
        // create different service type
        final ServiceDescription service1 = new ServiceDescription();
        service1.setName("SERVICENAME1");
        service1.setType("SERVICE1");
        final ServiceDescription serviceRing = new ServiceDescription();
        serviceRing.setName("SERVICENAMERING");
        serviceRing.setType("SERVICERING");
        
        
        for(int i=1; i<7; i++) {
            Integer iAgent = new Integer(i);
            String localAID = "df" + new String(iAgent.toString());
            // aid of the df-th
            AID dfAID = new AID(localAID,AID.ISLOCALNAME);
            
            try {
                switch (iAgent.intValue()) {
                    case 1:{
                        // Register agents to DF1
                        // 1 agent of service type SERVICERING
                        // agent a1 - service type SERVICERING
                        AID aid1 = new AID("a1",AID.ISLOCALNAME);
                        DFAgentDescription descA1 = new DFAgentDescription();
                        descA1.setName(aid1);
                        descA1.addServices(serviceRing);
                        DFService.register(a, dfAID, descA1);
                        break;
                    }
                    case 2:{
                        // Register agents to DF2
                        // 1 agents of service type SERVICE1
                        // 2 agents of service type SERVICERING
                        // agent a2 - service type SERVICE1
                        AID aid2 = new AID("a2",AID.ISLOCALNAME);
                        DFAgentDescription descA2 = new DFAgentDescription();
                        descA2.setName(aid2);
                        descA2.addServices(service1);
                        DFService.register(a, dfAID, descA2);
                        // agent a3 - service type SERVICERING
                        AID aid3 = new AID("a3",AID.ISLOCALNAME);
                        DFAgentDescription descA3 = new DFAgentDescription();
                        descA3.setName(aid3);
                        descA3.addServices(serviceRing);
                        DFService.register(a, dfAID, descA3);
                        // agent a4 - service type SERIVCERING
                        AID aid4 = new AID("a4",AID.ISLOCALNAME);
                        DFAgentDescription descA4 = new DFAgentDescription();
                        descA4.setName(aid4);
                        descA4.addServices(serviceRing);
                        DFService.register(a, dfAID, descA4);
                        break;
                    }
                    case 3:{
                        // Register agents to DF3
                        // 3 agents of service type SERVICE1
                        // 3 agents of service type SERVICERING
                        // agent a5 of SERVICE1
                        AID aid5 = new AID("a5",AID.ISLOCALNAME);
                        DFAgentDescription descA5 = new DFAgentDescription();
                        descA5.setName(aid5);
                        descA5.addServices(service1);
                        DFService.register(a, dfAID, descA5);
                        // agent a6 of SERVICE1
                        AID aid6 = new AID("a6",AID.ISLOCALNAME);
                        DFAgentDescription descA6 = new DFAgentDescription();
                        descA6.setName(aid6);
                        descA6.addServices(service1);
                        DFService.register(a, dfAID, descA6);
                        // agent a7 of SERVICE1
                        AID aid7 = new AID("a7",AID.ISLOCALNAME);
                        DFAgentDescription descA7 = new DFAgentDescription();
                        descA7.setName(aid7);
                        descA7.addServices(service1);
                        DFService.register(a, dfAID, descA7);
                        // agent a8 of SERVICE1
                        AID aid8 = new AID("a8",AID.ISLOCALNAME);
                        DFAgentDescription descA8 = new DFAgentDescription();
                        descA8.setName(aid8);
                        descA8.addServices(serviceRing);
                        DFService.register(a, dfAID, descA8);
                        // agent a9 of SERVICE1
                        AID aid9 = new AID("a9",AID.ISLOCALNAME);
                        DFAgentDescription descA9 = new DFAgentDescription();
                        descA9.setName(aid9);
                        descA9.addServices(serviceRing);
                        DFService.register(a, dfAID, descA9);
                        // agent a10 of SERVICE1
                        AID aid10 = new AID("a10",AID.ISLOCALNAME);
                        DFAgentDescription descA10 = new DFAgentDescription();
                        descA10.setName(aid10);
                        descA10.addServices(serviceRing);
                        DFService.register(a, dfAID, descA10);
                        break;
                    }
                    case 4:{
                        // Register agents to DF3
                        // 2 agents of service type SERVICE1
                        // agent a11 of SERVICE1
                        AID aid11 = new AID("a11",AID.ISLOCALNAME);
                        DFAgentDescription descA11 = new DFAgentDescription();
                        descA11.setName(aid11);
                        descA11.addServices(service1);
                        DFService.register(a, dfAID, descA11);
                        // agent a12 of SERVICE1
                        AID aid12 = new AID("a12",AID.ISLOCALNAME);
                        DFAgentDescription descA12 = new DFAgentDescription();
                        descA12.setName(aid12);
                        descA12.addServices(service1);
                        DFService.register(a, dfAID, descA12);
                        break;
                    }
                    case 5:{
                        // agent a13 of SERVICE1
                        AID aid13 = new AID("a13",AID.ISLOCALNAME);
                        DFAgentDescription descA13 = new DFAgentDescription();
                        descA13.setName(aid13);
                        descA13.addServices(service1);
                        DFService.register(a, dfAID, descA13);
                        break;
                    }
                    case 6:{
                        // agent a14 of SERVICE1
                        AID aid14 = new AID("a14",AID.ISLOCALNAME);
                        DFAgentDescription descA14 = new DFAgentDescription();
                        descA14.setName(aid14);
                        descA14.addServices(service1);
                        DFService.register(finalA, dfAID, descA14);
                        break;
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            
            
        }
        
        //TestUtility.createAgent(a,"a" + iAgent.toString() , "test.domain.df.tests.TestRecursiveSearchAgent", agentArgs, a.getAMS(), null);
        
        
        Behaviour b = new OneShotBehaviour() {
            public void action() {
                try {
                    boolean testPassed = true;
                    
                    // Send queries to df6
                    AID aidDF6 = new AID("df6",AID.ISLOCALNAME);
                    // set constraints on the search
                    SearchConstraints srcCstr = new SearchConstraints();
                    DFAgentDescription[] dfs = null;
                    DFAgentDescription dfAgent = new DFAgentDescription();
                    // TESTS: request of agent that offer service name: SERVICE1
                    dfAgent.addServices(service1);
                    
                    // test 1
                    srcCstr.setMaxResults(new Long(1));
                    srcCstr.setMaxDepth(new Long(0));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 1",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,1);
                    // test 2
                    srcCstr.setMaxResults(new Long(-1));
                    srcCstr.setMaxDepth(new Long(0));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 2",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,1);
                    // test 3
                    srcCstr.setMaxResults(new Long(-1));
                    srcCstr.setMaxDepth(new Long(1));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 3",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,2);
                    // test 4
                    srcCstr.setMaxResults(new Long(-1));
                    srcCstr.setMaxDepth(new Long(2));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 4",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,4);
                    // test 5
                    srcCstr.setMaxResults(new Long(-1));
                    srcCstr.setMaxDepth(new Long(4));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 5",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,5);
                    // test 6
                    srcCstr.setMaxResults(new Long(2));
                    srcCstr.setMaxDepth(new Long(20));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 6",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,2);
                    // test 7
                    srcCstr.setMaxResults(new Long(3));
                    srcCstr.setMaxDepth(new Long(20));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 7",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,3);
                    // test 8
                    srcCstr.setMaxResults(new Long(-1));
                    srcCstr.setMaxDepth(new Long(20));
                    dfs = DFService.search(finalA,aidDF6,dfAgent,srcCstr);
                    printResults(dfs,"Query 8",srcCstr,aidDF6);
                    testPassed = testPassed & checkResults(dfs,8);
                    if(testPassed) {
                        store.put(key, new Integer(Test.TEST_PASSED));
                    } else
                    {
                        store.put(key, new Integer(Test.TEST_FAILED));
                    }
                    
                    
                }catch(Exception e) {
                    e.printStackTrace();
                }
                
            }
            public int onEnd() {
                return 0;
            }
            
        };
        return b;
    }
    
    
    
    public void clean(Agent a) {
        try {
                Logger l = Logger.getLogger();
                l.log("Killing dfs...");
                for(int i=0; i<arrayAIDdf.length; i++)
                    TestUtility.killAgent(a,new AID(arrayAIDdf[i],AID.ISLOCALNAME));
                l.log("done.");
                l.log("Killing DFFederator...");
                TestUtility.killAgent(a, new AID("DFFederator",AID.ISLOCALNAME));
                l.log("done.");
            }catch(Exception e) {
                e.printStackTrace();
            }
    }
    
     
    /*
     * Print information on the query search and on the results
     * @param dfs results
     * @param queryName name of the query
     * @param srcConst search constraints
     * @param agentDF agent that receive the query
     */
    
    private void printResults(DFAgentDescription[] dfs,String queryName, SearchConstraints srcConst,AID agentDF) {
        Logger l = Logger.getLogger();
        l.log("---- " + queryName + " ----");
        l.log("df aid:" + agentDF);
        l.log("max-depth:" + srcConst.getMaxDepth());
        l.log("max-result:" + srcConst.getMaxResults());
        if (dfs!=null) {
            l.log("Number of results:"+dfs.length);
            for(int i=0;i<dfs.length;i++) {
                l.log("Agent name:"+dfs[i].getName());
            }
        }
        else
            l.log("No result.");
    }
    
    private boolean checkResults(DFAgentDescription[] dfs, int expectedNumResult) {
        if (dfs.length == expectedNumResult) {
            System.out.println("Query Result:CORRECT");
            return true;
        }
        else {
            System.out.println("Query Result:NOT CORRECT");
            return false;
        }
    }
    
}



