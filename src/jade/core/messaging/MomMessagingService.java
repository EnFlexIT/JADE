package jade.core.messaging;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import jade.core.AID;
import jade.core.GenericCommand;
import jade.core.IMTPException;
import jade.core.MainContainer;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Timer;
import jade.core.TimerDispatcher;
import jade.core.TimerListener;
import jade.core.VerticalCommand;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.lang.acl.ACLMessage;
import jade.security.JADESecurityException;
import jade.util.Logger;

public abstract class MomMessagingService extends MessagingService {
	public static final String SEPARATOR = "#";
	
	private LocalJVMMessageRouter localRouter;
	
	protected String myLocation;
	// We need synchronized access --> Hashtable
	private Map<AID, String> locationsCache = new Hashtable<AID, String>();
	private long deliveryCnt = 0;
	protected boolean active;
	
	private Map<String, DeliveryContext> ongoingDeliveries = new HashMap<String, DeliveryContext>();
	
	private long deliveryCompletionTimeout = 60000;  // 1 min
	
	@Override
	public void boot(Profile myProfile) throws ServiceException {
		super.boot(myProfile);
		
		myLocation = myContainer.getID().getName();
		active = true;
		
		localRouter = LocalJVMMessageRouter.getRouter(myContainer.getPlatformID());
		localRouter.register(myLocation, this);
		
		try {
			deliveryCompletionTimeout = Long.parseLong(myProfile.getParameter("delivery-completion-timeout", null));
		}
		catch (Exception e) {
			// Ignore and keep default
		}
		
		initMom();
	}
	
	@Override
	public void shutdown() {
		active = false;
		shutdownMom();
				
		super.shutdown();
		localRouter.deregister(myLocation);
	}
	
	@Override
	public Sink getCommandSink(boolean side) {
		final Sink realSink = super.getCommandSink(side);
		if (side == Sink.COMMAND_SOURCE) {
			// Unless the use-message-manager property is explicitly set to true,
			// create an ad-hoc Sink that overrides the consumption of the SEND-MESSAGE VCommand
			// to skip the MessageManager
			if (!myProfile.getBooleanProperty("use-message-manager", false)) {
				myLogger.log(Logger.INFO, "MessagingService configured to skip MessageManager");
				return new Sink() {
					@Override
					public void consume(VerticalCommand cmd) {
						String name = cmd.getName();
						if(name.equals(MessagingSlice.SEND_MESSAGE)) {
							Object[] params = cmd.getParams();
							AID sender = (AID)params[0];
							GenericMessage msg = (GenericMessage)params[1];
							AID dest = (AID)params[2];
							// Since message delivery is asynchronous we use the GenericMessage
							// as a temporary holder for the sender principal and credentials
							msg.setSenderPrincipal(cmd.getPrincipal());
							msg.setSenderCredentials(cmd.getCredentials());
							msg.setSender(sender);
							checkTracing(msg);
							if (msg.getTraceID() != null) {
								myLogger.log(Logger.INFO, "MessagingService source sink handling message "+MessageManager.stringify(msg)+" for receiver "+dest.getName()+". TraceID = "+msg.getTraceID());
							}
							deliverNow(msg, dest);
						}
						else {
							realSink.consume(cmd);
						}
					}
				};
			}
			else {
				myLogger.log(Logger.INFO, "MessagingService configured to use MessageManager");
				return realSink;
			}
		}
		else {
			return realSink;
		}
	}

	protected void initMom() {
	}
	
	protected void shutdownMom() {
	}
		
	protected synchronized String generateDeliveryID(AID senderID, GenericMessage msg, AID receiverID) {
		return myLocation+SEPARATOR+String.valueOf(deliveryCnt++);
	}
	
	protected String extractSource(String deliveryID) {
		int k = deliveryID.indexOf(SEPARATOR);
		return deliveryID.substring(0, k);
	}
	
	@Override
	protected void deliverInLocalPlatfrom(GenericMessage msg, AID receiverID) throws IMTPException, ServiceException, NotFoundException, JADESecurityException {
		AID senderID = msg.getSender();
		String deliveryID = generateDeliveryID(senderID, msg, receiverID);
		MainContainer impl = myContainer.getMain();
		try {
			if (impl != null) {
				// Directly use the GADT on the main container
				GADTDeliveryContext context = new GADTDeliveryContext(deliveryID, senderID, msg, receiverID);
				registerContext(context);
				String receiverLocation = getAgentLocation(receiverID).getName();
				context.setReceiverLocation(receiverLocation);
				sendMessage(deliveryID, senderID, msg, receiverID, receiverLocation);
			}
			else {
				String receiverLocation = locationsCache.get(receiverID);
				if (receiverLocation != null) {
					// Cache hit :-)
					CacheDeliveryContext context = new CacheDeliveryContext(deliveryID, senderID, msg, receiverID);
					registerContext(context);
					context.setReceiverLocation(receiverLocation);
					sendMessage(deliveryID, senderID, msg, receiverID, receiverLocation);
				}
				else {
					// Cache miss :-(
					DeliveryContext context = new DeliveryContext(deliveryID, senderID, msg, receiverID);
					registerContext(context);
					defaultDeliver(deliveryID, senderID, msg, receiverID);
				}
			}
		}
		catch (AsynchDelivery ad) {
			// Actual delivery carried out asynchronously. 
			// Context will be managed as soon as we get back the receipt
		}
		catch (Exception e) {
			handleDeliveryResult(deliveryID, e);
		}
	}
	
	private void defaultDeliver(String deliveryID, AID senderID, GenericMessage msg, AID receiverID) throws IMTPException, NotFoundException, JADESecurityException, ServiceException {
		try {
			DeliveryContext context = getContext(deliveryID);
			String receiverLocation = null;
			MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
			try {
				receiverLocation = mainSlice.getAgentLocation(receiverID).getName();
			} 
			catch (IMTPException imtpe) {
				// Try to get a newer slice and repeat...
				mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
				receiverLocation = mainSlice.getAgentLocation(receiverID).getName();
			}
			context.setReceiverLocation(receiverLocation);
			sendMessage(deliveryID, senderID, msg, receiverID, receiverLocation);
		}
		catch (ServiceException se) {
			// This container is no longer able to access the Main --> before propagating the exception
			// try to see if the receiver lives locally
			if (myContainer.isLocalAgent(receiverID)) {
				MessagingSlice localSlice = (MessagingSlice)getIMTPManager().createSliceProxy(getName(), getHorizontalInterface(), getLocalNode());
				localSlice.dispatchLocally(msg.getSender(), msg, receiverID);
				handleDeliveryResult(deliveryID, null);
			}
			else {
				throw se;
			}
		}
	}
	
	protected void handleDeliveryResult(String deliveryID, Throwable t) {
		DeliveryContext context = deregisterContext(deliveryID);
		if (context != null) {
			if (t == null) {
				context .success();
			}
			else {
				if (active) {
					context.failure(t);
				}
			}
		}
		else {
			myLogger.log(Logger.WARNING, "Context for delivery "+deliveryID+" not found");
		}
	}
	
	private class AsynchDelivery extends RuntimeException {
		public AsynchDelivery() {
			super("");
		}
	    public Throwable fillInStackTrace() {
	        return this;
	    }
	}
	
	private synchronized void registerContext(final DeliveryContext context) {
		ongoingDeliveries.put(context.deliveryID,  context);
		if (deliveryCompletionTimeout > 0) {
			// Activate Timer whatchDog 
			Timer watchDog = TimerDispatcher.getTimerDispatcher().add(new Timer(System.currentTimeMillis()+deliveryCompletionTimeout, new TimerListener() {
				public void doTimeOut(Timer t) {
					timeout(context);
				}
			}));
			context.setWatchDog(watchDog);
		}
	}
	
	private synchronized DeliveryContext getContext(String deliveryID) {
		return ongoingDeliveries.get(deliveryID);
	}
	
	private synchronized DeliveryContext deregisterContext(String deliveryID) {
		DeliveryContext context = ongoingDeliveries.remove(deliveryID);
		if (context != null) {
			// Deactivate Timer whatchDog
			Timer watchDog = context.getWatchDog();
			if (watchDog != null) {
				TimerDispatcher.getTimerDispatcher().remove(watchDog);
			}
		}
		return context;
	}

	private synchronized void timeout(DeliveryContext context) {
		context.setWatchDog(null);
		deregisterContext(context.deliveryID);
		context.failure(new IMTPException("Delivery TIMEOUT expired"));
	}
	
	
	// Inner class DeliveryContext
	private class DeliveryContext {
		protected String deliveryID;
		protected GenericMessage msg;
		protected AID senderID;
		protected AID receiverID;
		protected String receiverLocation = null;
		protected int attemptsCnt = 0;
		protected Timer watchDog = null;
		
		DeliveryContext(String deliveryID, AID senderID, GenericMessage msg, AID receiverID) {
			this.deliveryID = deliveryID;
			this.senderID = senderID;			
			this.msg = msg;
			this.receiverID = receiverID;
		}
		
		void setReceiverLocation(String receiverLocation) {
			this.receiverLocation = receiverLocation;
		}
		
		void setWatchDog(Timer watchDog) {
			this.watchDog = watchDog;
		}
		
		Timer getWatchDog() {
			return watchDog;
		}
		
		protected void success() {
			// Store the receiverLocation in the cache
			locationsCache.put(receiverID, receiverLocation);
		}
		
		protected void failure(Throwable t) {
			String id = (msg.getTraceID() != null ? msg.getTraceID() : MessageManager.stringify(msg));
			if (t instanceof NotFoundException) {
				if (receiverLocation == null) {
					// Receiver not found in the GADT --> Abort
					if (msg.getTraceID() != null) {
						myLogger.log(Logger.WARNING, msg.getTraceID()+" - Receiver "+receiverID.getLocalName()+" does not exist.", t);
					}
					notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_NOT_FOUND + ": " + t.getMessage()));
				}
				else {
					// Receiver found in the GADT, but not in the destination container.
					// It may have moved elsewhere in the meanwhile --> Retry
					attemptsCnt++;
					if (attemptsCnt < maxDeliveryRetryAttempts) {
						retry();
					}
					else {
						myLogger.log(Logger.WARNING, id+" - Max attempts reached trying to deliver message to "+receiverID.getLocalName()+".");
						notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_SERVICE_ERROR + ": Max attempts reached"));
					}
				}
			}
			else if (t instanceof JADESecurityException) {
				// Delivery not authorized--> Abort
				if (msg.getTraceID() != null) {
					myLogger.log(Logger.WARNING, msg.getTraceID()+" - Not authorized.", t);
				}
				notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_NOT_FOUND + ": " + t.getMessage()));
			}
			else if (t instanceof ServiceException) {
				// Can't get a slice to deliver the message --> Abort
				myLogger.log(Logger.WARNING, id+" - Service error delivering message to "+receiverID.getLocalName()+".", t);
				notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_SERVICE_ERROR + ": " + t.getMessage()));
			}
			else if (t instanceof IMTPException) {
				// Main or remote container unreachable --> Abort
				myLogger.log(Logger.WARNING, id+" - Receiver "+receiverID.getLocalName()+" unreachable.", t);
				notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE + ": " + t.getMessage()));
			}
			else {
				// Unexpected error --> Abort
				myLogger.log(Logger.WARNING, id+" - Unexpected error delivering message to "+receiverID.getLocalName(), t);
				notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE + ": " + t.getMessage()));
			}
		}
		
		protected void retry() {
			receiverLocation = null;
			registerContext(this);
			try {
				defaultDeliver(deliveryID, senderID, msg, receiverID); 
			}
			catch (AsynchDelivery ad) {
				// Actual delivery carried out asynchronously. 
				// Context will be managed as soon as we get back the receipt
			}
			catch (Exception e) {
				handleDeliveryResult(deliveryID, e);
			}
		}
	}  // END of Inner class DeliveryContext
	
	
	// Inner class GADTDeliveryContext
	private class GADTDeliveryContext extends DeliveryContext {
		GADTDeliveryContext(String deliveryID, AID senderID, GenericMessage msg, AID receiverID) {
			super(deliveryID, senderID, msg, receiverID);
		}
		
		@Override
		protected void success() {
			// No need to cache anything in case we are on the Main
		}
		
		// Redefine retry to continue using the GADT
		@Override
		protected void retry() {
			receiverLocation = null;
			registerContext(this);
			try {
				String receiverLocation = getAgentLocation(receiverID).getName();
				setReceiverLocation(receiverLocation);
				sendMessage(deliveryID, senderID, msg, receiverID, receiverLocation);
			}
			catch (AsynchDelivery ad) {
				// Actual delivery carried out asynchronously. 
				// Context will be managed as soon as we get back the receipt
			}
			catch (Exception e) {
				handleDeliveryResult(deliveryID, e);
			}
		}
	}  // END of Inner class GADTDeliveryContext
	
	
	// Inner class CacheDeliveryContext
	private class CacheDeliveryContext extends DeliveryContext {
		CacheDeliveryContext(String deliveryID, AID senderID, GenericMessage msg, AID receiverID) {
			super(deliveryID, senderID, msg, receiverID);
		}
		
		@Override
		protected void failure(Throwable t) {
			locationsCache.remove(receiverID);
			DeliveryContext context = new DeliveryContext(deliveryID, senderID, msg, receiverID);
			registerContext(context);
			try {
				defaultDeliver(deliveryID, senderID, msg, receiverID); 
			}
			catch (AsynchDelivery ad) {
				// Actual delivery carried out asynchronously. 
				// Context will be managed as soon as we get back the receipt
			}
			catch (Exception e) {
				handleDeliveryResult(deliveryID, e);
			}
		}
	}  // END of Inner class CacheDeliveryContext
	
	private void sendMessage(String deliveryID, AID senderID, GenericMessage msg, AID receiverID, String receiverLocation) throws IMTPException {
		boolean sent = localRouter.localSendMessage(deliveryID, senderID, msg, receiverID, receiverLocation, this);
		if (!sent) {
			sendMessageViaMom(deliveryID, senderID, msg, receiverID, receiverLocation);
			throw new AsynchDelivery();
		}
	}
	
	Object processIncomingMessage(AID senderID, GenericMessage msg, AID receiverID) {
		// Do as if we received the incoming message via a normal HorizontalCommand
		GenericCommand cmd = new GenericCommand(MessagingSlice.H_DISPATCHLOCALLY, NAME, null);
		cmd.addParam(senderID);
		cmd.addParam(msg);
		cmd.addParam(receiverID);
		long timeStamp = msg.getTimeStamp();
		if (timeStamp > 0) {
			cmd.addParam(new Long(timeStamp));
		}			
		cmd.setPrincipal(msg.getSenderPrincipal());
		cmd.setCredentials(msg.getSenderCredentials());
		try {
			return getLocalNode().accept(cmd);
		}
		catch (IMTPException imtpe) {
			// Should never happen since this is the local node
			return imtpe;
		}
	}
	
	protected abstract void sendMessageViaMom(String deliveryID, AID senderID, GenericMessage msg, AID receiverID, String receiverLocation) throws IMTPException;
	
	protected abstract void sendReceiptViaMom(String deliveryID, String messageSenderLocation, Throwable failure);

	protected void handleMessageFromMom(String deliveryID, AID senderID, GenericMessage msg, AID receiverID, String senderLocation) {
		Object ret = processIncomingMessage(senderID, msg, receiverID);
		sendReceiptViaMom(deliveryID, senderLocation, (Throwable) ret);
	}
	
	protected void handleReceiptFromMom(String deliveryID, Throwable t) {
		handleDeliveryResult(deliveryID, t);
	}
}
