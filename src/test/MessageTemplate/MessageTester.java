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

package test.MessageTemplate;

import java.util.Date;
import java.io.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;
/**
*
* This example test the MessageTemplate.
* For every fields of the ACLMessage we have two examples:
* for the first the ACLMessage matches the template, for the second 
* the ACLMessage doesn't match the template. 
* This example use the inprocess interface, to start use the following
* command line: java -cp <jade_path> test.MessageTemplate.MessageTester 
* @author Tiziana Trucco - Telecom Italia Lab S.p.A
* @version $Date$ $Revision$
**/

public class MessageTester {

  public static void main(String args[]) {

      try{
	  // Get a hold on JADE runtime
	  Runtime rt = Runtime.instance();
	  
	  // Exit the JVM when there are no more containers around
	  rt.setCloseVM(true);
	  
	  // Launch a complete platform on the 8888 port
	  // create a default Profile 
	  Profile pMain = new ProfileImpl(null, 8888, null);
	  
	  System.out.println("Launching a whole in-process platform..."+pMain);
	  MainContainer mc = rt.createMainContainer(pMain);

	  System.out.println( "START MessageTemplate TEST." );
	  waitInput();

	  //Sender.
	  //The ACLMessage has a sender with 2 resolvers.
	  System.out.println( "\n////// SENDER TESTS ///////" );
	  ACLMessage Msg = new ACLMessage(ACLMessage.REQUEST);
	  AID aid = new AID("pippo" ,false);
	  AID res1 = new AID( "res1", false);
	  aid.addResolvers(res1);
	  AID res2 = new AID( "res2",false);
	  aid.addResolvers(res2);
	  Msg.setSender(aid); 
	  //the MessageTemplate has no Resolvers.
	  //but the template matches the ACLMessage since the equals method of the AID matches only the AID name.
	  MessageTemplate senderTemplate = MessageTemplate.MatchSender(new AID("pippo",false ));
	  System.out.println( "\nACLMessage sender: " + Msg.getSender());
	  System.out.println( "Message Template: " +  senderTemplate.toString());
	  if(senderTemplate.match(Msg))
	      System.out.println( "1.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
       	      System.out.println( "1.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  
	  //The MessageTemplate has a different AID name.
	  MessageTemplate senderTemplate2 = MessageTemplate.MatchSender(new AID("pluto",false ));
	  System.out.println( "\nACLMessage sender: " + Msg.getSender());
	  System.out.println( "Message Template: " +  senderTemplate2.toString());
	  if(senderTemplate2.match(Msg))
	      System.out.println("2.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("2.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");
	  
	  waitInput();

	  //Perfomative
	  System.out.println( "\n////// PERFOMATIVE TEST //////" );
	  MessageTemplate performativeTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + performativeTemplate.toString());
	  if(performativeTemplate.match(Msg))
	      System.out.println("3.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("3.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setPerformative(ACLMessage.INFORM);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + performativeTemplate.toString());
	  if(performativeTemplate.match(Msg))
	      System.out.println("4.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("4.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();

	  //ConversationId
	  System.out.println( "\n////// CONVERSATION_ID TEST //////" );
	  MessageTemplate conversationIdTemplate = MessageTemplate.MatchConversationId( "Conversation_ID_100"); 
	  Msg.setConversationId( "Conversation_ID_100" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + conversationIdTemplate.toString());
	  if(conversationIdTemplate.match(Msg))
	      System.out.println("5.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("5.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setConversationId( "OtherConversationID" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + conversationIdTemplate.toString());
	  if(conversationIdTemplate.match(Msg))
	      System.out.println("6.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("6.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //Encoding
	  System.out.println( "\n////// ENCODING TEST //////" );
	  MessageTemplate encodingTemplate = MessageTemplate.MatchEncoding( "fipa.lang.BitEfficient"); 
	  Msg.setEncoding( "fipa.lang.BitEfficient" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + encodingTemplate.toString());
	  if(encodingTemplate.match(Msg))
	      System.out.println("7.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("7.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setEncoding("OtherEncoding");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + encodingTemplate.toString());
	  if(encodingTemplate.match(Msg))
	      System.out.println("8.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("8.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //InReplyTo
	  System.out.println( "\n////// IN_REPLY_TO TEST //////" );
	  MessageTemplate inReplyToTemplate = MessageTemplate.MatchInReplyTo( "InReplyTo_1000"); 
	  Msg.setInReplyTo( "inreplyto_1000" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + inReplyToTemplate.toString());
	  if(inReplyToTemplate.match(Msg))
	      System.out.println("9.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("9.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setInReplyTo("OtherInReplyTo");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + inReplyToTemplate.toString());
	  if(inReplyToTemplate.match(Msg))
	      System.out.println("10.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("10.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //Language
	  System.out.println( "\n////// LANGUAGE TEST //////" );
	  MessageTemplate languageTemplate = MessageTemplate.MatchLanguage( "FipaLanguage"); 
	  Msg.setLanguage( "FipaLanguage" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + languageTemplate.toString());
	  if(languageTemplate.match(Msg))
	      System.out.println("11.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("11.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setLanguage("OtherLanguage");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + languageTemplate.toString());
	  if(languageTemplate.match(Msg))
	      System.out.println("12.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("12.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //Ontology
	  System.out.println( "\n////// ONTOLOGY TEST //////" );
	  MessageTemplate ontologyTemplate = MessageTemplate.MatchOntology( "FipaOntology"); 
	  Msg.setOntology( "FipaOntology" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + ontologyTemplate.toString());
	  if(ontologyTemplate.match(Msg))
	      System.out.println("13.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("13.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setOntology("OtherOntology");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + ontologyTemplate.toString());
	  if(ontologyTemplate.match(Msg))
	      System.out.println("14.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("14.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //Protocol
	  System.out.println( "\n////// PROTOCOL TEST //////" );
	  MessageTemplate protocolTemplate = MessageTemplate.MatchProtocol( "FipaRequest"); 
	  Msg.setProtocol( "FipaRequest" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + protocolTemplate.toString());
	  if(protocolTemplate.match(Msg))
	      System.out.println("15.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("15.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setProtocol("OtherProtocol");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + protocolTemplate.toString());
	  if(protocolTemplate.match(Msg))
	      System.out.println("16.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("16.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //ReplyByDate
	  System.out.println( "\n////// REPLY_BY_DATE TEST //////" );
	  Date d1 = new Date();
	  MessageTemplate replyByDateTemplate = MessageTemplate.MatchReplyByDate((Date)d1.clone()); 
	  Msg.setReplyByDate((Date)d1.clone());
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyByDateTemplate.toString());
	  if(replyByDateTemplate.match(Msg))
	      System.out.println("17.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("17.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");
	  d1.setMinutes(d1.getMinutes()+ 10);
	  Msg.setReplyByDate(d1);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyByDateTemplate.toString());
	  if(replyByDateTemplate.match(Msg))
	      System.out.println("18.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("18.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //ReplyWith
	  System.out.println( "\n////// REPLY_WITH TEST //////" );
	  MessageTemplate replyWithTemplate = MessageTemplate.MatchReplyWith( "ReplyWith_1000"); 
	  Msg.setReplyWith( "ReplyWith_1000" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyWithTemplate.toString());
	  if(replyWithTemplate.match(Msg))
	      System.out.println("19.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("19.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  Msg.setReplyWith("OtherReplyWith");
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyWithTemplate.toString());
	  if(replyWithTemplate.match(Msg))
	      System.out.println("20.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
       	      System.out.println("20.2- THE ALCMESSAGE DOESN'T MATCH THE TEMPLATE");

	  waitInput();
	  //Receivers
	  //FIXME fare delle prove con rec[] vuoto
	  System.out.println( "\n//////// RECEIVERS TEST ////////" );
	  AID rec1 = new AID( "rec1",false );
	  AID rec2 = new AID( "rec2",false );
	  Msg.addReceiver(rec1);
	  Msg.addReceiver(rec2);
	  AID[] recs = new AID[2];
	  recs[0]= (AID)rec1.clone();
	  recs[1]= (AID)rec2.clone();
	  MessageTemplate receiverTemplate = MessageTemplate.MatchReceiver(recs);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + receiverTemplate.toString());
	  if(receiverTemplate.match(Msg))
	      System.out.println( "21.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println( "21.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  AID[] recs2 = new AID[3];
	  recs2[0]= (AID)rec1.clone();
	  recs2[1]=(AID)rec2.clone();
	  recs2[2]=new AID("rec3",false);
	  MessageTemplate receiverTemplate2 = MessageTemplate.MatchReceiver(recs2);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + receiverTemplate2.toString());
	  if(receiverTemplate2.match(Msg))
	      System.out.println("22.1- THE ACLMESSAGE MATCHES THE TEMPLATE ");
	  else 
	      System.out.println( "22.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  waitInput();
	  //ReplyTo
	  System.out.println( "\n//////// REPLY_TO TEST ////////" );
	  AID rep1 = new AID( "rep1",false );
	  AID rep2 = new AID( "rep2",false );
	  Msg.addReplyTo(rep1);
	  Msg.addReplyTo(rep2);
	  AID[] reps = new AID[2];
	  reps[0]= (AID)rep1.clone();
	  reps[1]= (AID)rep2.clone();
	  MessageTemplate replyToTemplate = MessageTemplate.MatchReplyTo(reps);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyToTemplate.toString());
	  if(replyToTemplate.match(Msg))
	      System.out.println( "23.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println( "23.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  AID[] reps2 = new AID[3];
	  reps2[0]= (AID)rep1.clone();
	  reps2[1]=(AID)rep2.clone();
	  reps2[2]=new AID("rep3",false);
	  MessageTemplate replyToTemplate2 = MessageTemplate.MatchReplyTo(reps2);
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + replyToTemplate2.toString());
	  if(replyToTemplate2.match(Msg))
	      System.out.println("24.1- THE ACLMESSAGE MATCHES THE TEMPLATE ");
	  else 
	      System.out.println( "24.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  waitInput();

	  //CONTENT: test with the MatchContent
	  System.out.println( "\n////// CONTENT TEST /////" );
	  Msg.setContent( "ContentOfTheACLMessage" );
	  MessageTemplate contentTemplate = MessageTemplate.MatchContent( "ContentOfTheACLMessage" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + contentTemplate.toString());
	  if(contentTemplate.match(Msg))
	      System.out.println("25.1- THE ACLMESSAGE MATCHES THE TEMPLATE ");
	  else 
	      System.out.println( "25.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  //the ACLMessage has another String content.
	  Msg.setContent( "AnotherContent" );
	  System.out.println( "\nACLMessage: " + Msg );
	  System.out.println( "MessageTemplate: " + contentTemplate.toString());
	  if(contentTemplate.match(Msg))
	      System.out.println("26.1- THE ACLMESSAGE MATCHES THE TEMPLATE ");
	  else 
	      System.out.println( "26.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  waitInput();

	  //AND
	  System.out.println( "\n////// AND TEST ///// " );
	  ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
	  message.setLanguage("FipaLanguage");
	  message.setReplyWith("Replywith_1000");
	  message.setConversationId( "AConversationID");
	  MessageTemplate AND_Template = MessageTemplate.and(languageTemplate,replyWithTemplate);
	  System.out.println( "\nACLMessage: " + message );
	  System.out.println( "MessageTemplate: " + AND_Template.toString());
	  if(AND_Template.match(message))
	      System.out.println( "27.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println("27.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  message.setLanguage( "AnotherLanguage");
	  System.out.println( "\nACLMessage: " + message);
	  System.out.println( "MessageTemplate: " + AND_Template.toString());
	  if(AND_Template.match(message))
	      System.out.println( "28.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println("28.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  
	  //OR
	  System.out.println( "\n////// OR TEST ///// " );
	  MessageTemplate OR_Template = MessageTemplate.or(languageTemplate,replyWithTemplate);
	  System.out.println( "\nACLMessage: " + message );
	  System.out.println( "MessageTemplate: " + OR_Template.toString());
	  if(OR_Template.match(message))
	      System.out.println( "29.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println("29.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  message.setReplyWith( "AnotherReplyWith");
	  System.out.println( "\nACLMessage: " + message);
	  System.out.println( "MessageTemplate: " + OR_Template.toString());
	  if(OR_Template.match(message))
	      System.out.println( "30.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println("30.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");

	  //NOT
	  System.out.println( "\n////// NOT TEST ///// " );
	  MessageTemplate NOT_Template = MessageTemplate.not(languageTemplate);
	  System.out.println( "\nACLMessage: " + message );
	  System.out.println( "MessageTemplate: " + NOT_Template.toString());
	  if(NOT_Template.match(message))
	      System.out.println( "31.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println( "31.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  message.setLanguage( "FipaLanguage");
	  System.out.println( "\nACLMessage: " + message);
	  System.out.println( "MessageTemplate: " + NOT_Template.toString());
	  if(NOT_Template.match(message))
	      System.out.println( "32.1- THE ACLMESSAGE MATCHES THE TEMPLATE" );
	  else
	      System.out.println("32.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE");
	  waitInput();

	  //CustomMsg
	  System.out.println( "\n/// CUSTOM Message TEST" );
	  ACLMessage customMsg = new ACLMessage(ACLMessage.REQUEST);
	  //template with a custom message empty. no check on performative.
	  MessageTemplate customTemplate = MessageTemplate.MatchCustom(customMsg,false);
	  ACLMessage Msg1 = new ACLMessage(ACLMessage.INFORM);
	  Msg1.setSender(new AID("pippo",false));
	  Msg1.setLanguage( "FipaLanguage" );
	  Msg1.setOntology( "FipaOntology" );
	  Msg1.setEncoding( "FipaEncoding");
	  Msg1.addReceiver(new AID( "rec1",false ));
	  Msg1.addReceiver(new AID( "rec2",false ));
	  Msg1.setConversationId("ConversationID_1000");
	  Msg1.setProtocol( "FipaProtocol" );
	  Msg1.setReplyWith( "ReplyWith_1000" );
	  Msg1.setReplyByDate(new Date());
	  Msg1.addReplyTo(new AID( "rep1",false ));
	  Msg1.addReplyTo(new AID( "rep1",false ));
	  Msg1.setInReplyTo( "InReplyTo_1000" );
	  Msg1.setContent("Content");
	  System.out.println( "\nACLMessage: " + Msg1 );
	  System.out.println( "MessageTemplate: " + customTemplate.toString());
	  if(customTemplate.match(Msg1))
	      System.out.println("33.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "33.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  //check on performartive
	  customTemplate = MessageTemplate.MatchCustom(customMsg,true);
	  System.out.println( "\nACLMessage: " + Msg1 );
	  System.out.println( "MessageTemplate: " + customTemplate.toString());
	  if(customTemplate.match(Msg1))
	      System.out.println("34.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "34.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  //all fields of the message setted. check on performative
	  customTemplate = MessageTemplate.MatchCustom(Msg1,true);
	  System.out.println( "\nACLMessage: " + Msg1 );
	  System.out.println( "MessageTemplate: " + customTemplate.toString());
	  if(customTemplate.match(Msg1))
	      System.out.println("35.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "35.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  //change a valued of the customMsg.
	  Msg1.setLanguage( "anotherLanguage" );
	  System.out.println( "\nACLMessage: " + Msg1 );
	  System.out.println( "MessageTemplate: " + customTemplate.toString());
	  if(customTemplate.match(Msg1))
	      System.out.println("36.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "36.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  waitInput();

	  //MatchAll
	  System.out.println( "\n///// MATCHALL TEST /////" );
	  MessageTemplate matchAllTemplate = MessageTemplate.MatchAll();
	  System.out.println( "\nACLMessage: " + Msg1 );
	  System.out.println( "MessageTemplate: " + matchAllTemplate.toString());
	  if(matchAllTemplate.match(Msg1))
	      System.out.println("37.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "37.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  waitInput();

	  
	  System.out.println( "\n//////TEST BYTE CONTENT with MatchCustom/////" );
	  ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
	  String content =  "content"; 
	  inform.setByteSequenceContent(content.getBytes());
	  MessageTemplate byteContent_Template = MessageTemplate.MatchCustom((ACLMessage)inform.clone(),false);
	  System.out.println( "\nACLMessage: " + inform );
	  System.out.println( "MessageTemplate: " + byteContent_Template.toString());
	  if(byteContent_Template.match(inform))
	      System.out.println("38.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "38.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );

	  waitInput();
	  
	  //the message has another byte content.
	  content =  "newContent"; 
	  inform.setByteSequenceContent(content.getBytes());
	  System.out.println( "\nACLMessage: " + inform );
	  System.out.println( "MessageTemplate: " + byteContent_Template.toString());
	  if(byteContent_Template.match(inform))
	      System.out.println("39.1- THE ACLMESSAGE MATCHES THE TEMPLATE");
	  else
	      System.out.println( "39.2- THE ACLMESSAGE DOESN'T MATCH THE TEMPLATE" );
	  
      }catch(Exception e) {
		  e.printStackTrace();
	      }
  }
    private static  void waitInput(){
	try{	
	    System.out.println("Press Enter to continue..." );
	    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
	    String input = buff.readLine();
	}catch(IOException e){}
    }
}//end class MessageTester.
