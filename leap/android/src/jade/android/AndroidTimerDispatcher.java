package jade.android;

import jade.core.TimerDispatcher;
import jade.util.Logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class AndroidTimerDispatcher extends TimerDispatcher {

	public static final String WAKE_TD_ACTION = "JADE.WAKE_TD";
	
	private Context context;
	private PendingIntent pendingIntent;

	
	AndroidTimerDispatcher(Context context) {
		super();
		this.context = context;

		// Prepare to receive Alarm notifications
		IntentFilter filter = new IntentFilter();
		filter.addAction(WAKE_TD_ACTION);
		context.registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context ctx, Intent i) {
				myLogger.log(Logger.FINE, "TD Alarm go off!!! Intent action = "+i.getAction());
				synchronized (AndroidTimerDispatcher.this) {
					wakeUp();
				}
			}
		}, filter);
	}

	protected void sleep(long sleepTime) throws InterruptedException {
		// Always wait forever, but add an Alarm if we have to sleep for a fixed time 
		if (sleepTime > 0) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			
			Intent i = new Intent();
			i.setAction(WAKE_TD_ACTION);
			pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+sleepTime, pendingIntent);
			myLogger.log(Logger.FINE, "TD Alarm activated");
		}
		
		wait(0);
	}
	
	protected void wakeUp() {
		myLogger.log(Logger.FINE, "Wakeing up TD");
		if (pendingIntent != null) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(pendingIntent);
			pendingIntent = null;
			myLogger.log(Logger.FINE, "TD Alarm cancelled");
		}
		notifyAll();
	}
}
