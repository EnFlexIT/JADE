/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.domain;

//#MIDP_EXCLUDE_FILE

import java.util.Enumeration;

import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Iterator;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.KBManagement.*;
import jade.lang.acl.ACLMessage;

import jade.proto.SubscriptionResponder;

import jade.content.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.abs.*;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
class KBSubscriptionManager implements SubscriptionResponder.SubscriptionManager{

  KB kBase;
  ContentManager cm;

  public KBSubscriptionManager(KB k){
    super();
    kBase = k;
  }
  
  public void setContentManager(ContentManager c){
    cm = c;
  }
        
    public boolean register(SubscriptionResponder.Subscription sub) throws RefuseException, NotUnderstoodException{
        
    DFAgentDescription dfdTemplate = null;
    SearchConstraints constraints = null;
    AbsIRE absIota = null;
    
      try{
      // Get DFD template and search constraints from the subscription message 
      ACLMessage subMessage = sub.getMessage();
      
      absIota = (AbsIRE) cm.extractAbsContent(subMessage);
      AbsPredicate absResult = absIota.getProposition();
      AbsAgentAction absAction = (AbsAgentAction) absResult.getAbsObject(BasicOntology.RESULT_ACTION);
      AbsAgentAction absSearch = (AbsAgentAction) absAction.getAbsObject(BasicOntology.ACTION_ACTION);
      Search search = (Search) FIPAManagementOntology.getInstance().toObject(absSearch);
      
      dfdTemplate = (DFAgentDescription) search.getDescription();
      constraints = search.getConstraints();
    
      // Register the Subscription
      kBase.subscribe(dfdTemplate, sub);
  
    }catch(Exception e){
      throw new NotUnderstoodException(e.getMessage());
    }
    // Search for DFDs that already match the specified template
    List results = kBase.search(dfdTemplate); 
    
    // If some DFD matches the template, notify the subscribed agent 
    if(results.size() > 0){
      // If there are more matching DFD than MAX_RESULT, then remove the DFD in eccess
      Long maxResult = constraints.getMaxResults();           
      if(maxResult != null) {           
        if(results.size() >= maxResult.intValue()){
          // More results than required have been found, put in list the first MAX_RESULT results
          ArrayList list = new ArrayList();
          int j = 0;
          for(Iterator i = results.iterator();i.hasNext()&& j < maxResult.intValue();j++){
            list.add(i.next()); 
          }
          results=list;
        }
      }
      notify(sub, results, absIota);
      return true;
    }
    return false;
    }


  //OK
    // degeregister the subscritpion from hashtable
    public boolean deregister( SubscriptionResponder.Subscription sub ) throws FailureException {
    kBase.unsubscribe(sub);
    return false;
    }
    
    
    private DFAgentDescription getDFAgentDescriptionFromACL(ACLMessage aclM){
      DFAgentDescription dfd = null;
      try{
      AbsIRE absIota = (AbsIRE) cm.extractAbsContent(aclM);
      AbsPredicate absResult = absIota.getProposition();
      AbsAgentAction absAction = (AbsAgentAction) absResult.getAbsObject(BasicOntology.RESULT_ACTION);
      AbsAgentAction absSearch = (AbsAgentAction) absAction.getAbsObject(BasicOntology.ACTION_ACTION);
      Search search = (Search) FIPAManagementOntology.getInstance().toObject(absSearch);
      
      dfd = (DFAgentDescription) search.getDescription();
      }catch(Exception e){
        e.printStackTrace();
      } 
      return dfd;
    }
    
  /**
     Handle registrations/deregistrations/modifications by notifying 
     subscribed agents if necessary
   */
  void handleChange(DFAgentDescription dfd) {
    // Create a temporary MemKB just to re-use the match() method. 
    DFMemKB memTemp = new DFMemKB(0);
    
    Enumeration e = kBase.getSubscriptions();
    while (e.hasMoreElements()) {
      SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription) e.nextElement();
      DFAgentDescription template = getDFAgentDescriptionFromACL(sub.getMessage());
      if ( memTemp.match(template, dfd) ) {
        // This subscriber must be notified
        List results = new ArrayList();
        results.add(dfd);
        ACLMessage aclSub = sub.getMessage();
        AbsIRE absIota=null;
          try{
            absIota = (AbsIRE) cm.extractAbsContent(aclSub);
          }catch(Exception ex){
            ex.printStackTrace();
          }
        notify(sub, results, absIota);
      }
    }
  }
    
  private void notify(SubscriptionResponder.Subscription sub, List results, AbsIRE absIota) {
    try {
      ACLMessage notification = sub.getMessage().createReply();
      notification.setPerformative(ACLMessage.INFORM);
      AbsPredicate absEquals = new AbsPredicate(SLVocabulary.EQUALS);
      absEquals.set(SLVocabulary.EQUALS_LEFT, absIota);
      absEquals.set(SLVocabulary.EQUALS_RIGHT, FIPAManagementOntology.getInstance().fromObject(results));
    
      cm.fillContent(notification, absEquals);
      //pass to Subscription the message to send
      sub.notify(notification);
    }
    catch (Exception e) {
      e.printStackTrace();
      //FIXME: Check whether a FAILURE message should be sent back.       
    }
  }
}
