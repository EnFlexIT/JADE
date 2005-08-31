package test.udpmonitor.tests;

import jade.core.ServiceHelper;

public interface InterruptableUDPNodeMonitoringHelper extends ServiceHelper {
	void setPingDelay(int delay);
}
