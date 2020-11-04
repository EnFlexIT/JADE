package jade.core.messaging;

import java.util.HashMap;

//#J2ME_EXCLUDE_FILE

public class DeliveryTracing extends HashMap<String, Object> {

	private static final ThreadLocal<DeliveryTracing> tracing = new ThreadLocal<DeliveryTracing>();
	
	public static void beginTracing() {
		tracing.set(new DeliveryTracing());
	}
	
	public static void endTracing() {
		tracing.set(null);
	}
	
	public static void setTracingInfo(String key, Object value) {
		DeliveryTracing dt = tracing.get();
		if (dt != null) {
			dt.put(key, value);
		}
	}
	
	public static String report() {
		DeliveryTracing dt = tracing.get();
		if (dt != null) {
			return "Delivery details: "+dt.toString();
		}
		else {
			return "Delivery tracing off";
		}
	}
}
