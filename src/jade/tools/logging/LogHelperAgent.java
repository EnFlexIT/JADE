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
package jade.tools.logging;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.tools.logging.ontology.LogManagementOntology;
import jade.tools.logging.ontology.GetAllLoggers;
import jade.tools.logging.ontology.SetLevel;
import jade.tools.logging.ontology.SetFile;
import jade.util.Logger;
import jade.util.leap.List;

/**
 * @author 00917820
 * @version $Date:  $ $Revision: $
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LogHelperAgent extends Agent {

	private String logManagerClass = JavaLoggingLogManagerImpl.JAVA_LOGGING_LOG_MANAGER_CLASS; //default logManagerClass if not specified.
	private Logger logger;
	private Codec codec = new SLCodec();
	private LogManager logManager = null;

	protected void setup(){
		
		logger = Logger.getMyLogger(getLocalName());
	  // Register languages and ontologies
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);	
    getContentManager().registerOntology(LogManagementOntology.getInstance());
    
		MessageTemplate mt = MessageTemplate.MatchOntology(LogManagementOntology.NAME);
		addBehaviour(new LogHelperAgentBehaviour(this, mt));
		logger.log(Logger.INFO, getName() + " started using " + logManagerClass );
	}
	
	private class LogHelperAgentBehaviour extends SimpleAchieveREResponder{

		public LogHelperAgentBehaviour(Agent a, MessageTemplate mt) {
			super(a, mt);
		}

		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			try {
				ACLMessage reply = request.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				Action action =(Action)getContentManager().extractContent(request);
				Concept agentAction = action.getAction();
				if(agentAction instanceof GetAllLoggers){
					reply = handleGetAllLoggers((GetAllLoggers)agentAction, reply);
				}else if(agentAction instanceof SetFile){
					reply = handleSetFile((SetFile)agentAction, reply);
				}else if (agentAction instanceof SetLevel){
					reply = handleSetLevel((SetLevel)agentAction, reply);
				}else throw new NotUnderstoodException("Not supported action");		
				return reply;
			}catch(NotUnderstoodException nue){
				logger.log(Logger.WARNING, nue.getMessage());
				throw nue;
			}catch(RefuseException re){
				logger.log(Logger.WARNING, re.getMessage());
				throw re;
			}catch (Exception e) {
				logger.log(Logger.WARNING, e.getMessage());
				e.printStackTrace();
				throw new RefuseException(e.getMessage());	
			}
		}
		
		private ACLMessage handleGetAllLoggers(GetAllLoggers action, ACLMessage reply) throws RefuseException{
			try {
				String className = action.getType();
				if (className != null)
					logManagerClass = className;

				logger.log(Logger.CONFIG, "Log manager class defined: " + logManagerClass);
				logManager = (LogManager) Class.forName(logManagerClass).newInstance();

				List logInfo = logManager.getAllLogInfo();
				ContentElement ce = null;
				ce = new Result(action, logInfo);
				getContentManager().fillContent(reply, ce);
				reply.setPerformative(ACLMessage.INFORM);
			} catch (Exception any) {
				logger.log(Logger.WARNING, any.getMessage());
				any.printStackTrace();
				throw new RefuseException(any.getMessage());
			}
			return reply;
		}
		
		//FIXME: risolvere il problema del root logger quello senza nome !!!!!
		private ACLMessage handleSetFile(SetFile action, ACLMessage reply) throws RefuseException{
			if(logManager != null){
				logManager.addFile(action.getFile(), action.getLogger());
			}else{
				throw new RefuseException("missing initialization of log manager");
			}
			reply.setPerformative(ACLMessage.INFORM);
			//FIXME: no check if the operation is successfull ?
			return reply;
		}
		
		private ACLMessage handleSetLevel(SetLevel action, ACLMessage reply)throws RefuseException{
			if(logManager != null){
				logManager.setLogLevel(action.getLogger(), action.getLevel());
			}else{
				throw new RefuseException("missing initialization of log manager");
			}
			reply.setPerformative(ACLMessage.INFORM);
			return reply;
		}	
	}
}
