package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.ServiceHelper;
import jade.core.ServiceException;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;

public class InterruptableUDPNodeMonitoringService extends UDPNodeMonitoringService {
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		return new InterruptableUDPNodeMonitoringHelper() {
			public void setPingDelay(int delay) {
				InterruptableUDPNodeMonitoringService.this.setClientsPingDelay(delay);
			}
			
			public void init(Agent a) {
			}
		};
	}
}
