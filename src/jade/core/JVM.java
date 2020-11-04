package jade.core;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import jade.core.sam.CounterValueProvider;
import jade.core.sam.MeasureProvider;
import jade.core.sam.SAMHelper;
import jade.core.sam.SAMService;
import jade.imtp.leap.JICP.Connection;

//#J2ME_EXCLUDE_FILE

class JVM {
	public static final String JVM_NAME = "jvm-name";
	public static final String ENABLE_GLOBAL_MONITORING = "enable-global-monitoring";
	
	private static boolean initialized = false;
	
	static void started(AgentContainerImpl aci, Profile p) {
		if (p.getBooleanProperty(ENABLE_GLOBAL_MONITORING, false)) {
			synchronized (JVM.class) {
				if (!initialized) {
					try {
						SAMHelper helper = SAMService.getSingletonHelper();
						if (helper != null) {
							String jvmName = aci.getProperty(JVM_NAME, aci.getID().getName());
							String hostName = Profile.getDefaultNetworkName(p.getBooleanProperty(Profile.PRIVILEDGE_LOGICAL_NAME, false));
							String suffix = "#"+hostName+"#"+jvmName;
							// Number of open sockets
							helper.addCounterValueProvider("openSockets"+suffix, new CounterValueProvider() {
								@Override
								public long getValue() {
									return Connection.socketCnt;
								}

								@Override
								public boolean isDifferential() {
									return false;
								}
							});
							
							helper.addCounterValueProvider("threads"+suffix, new CounterValueProvider() {
								@Override
								public long getValue() {
									ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
									return threadMXBean.getThreadCount();
								}

								@Override
								public boolean isDifferential() {
									return false;
								}
							});
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
