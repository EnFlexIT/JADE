/**
 * ***************************************************************
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
 * **************************************************************
 */
package jade.android;

import android.os.Handler;
import jade.util.Logger;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public abstract class RuntimeUICallback<Result> extends
		RuntimeCallback<Result> {
	protected Handler handler = new Handler();

	@Override
	public void notifySuccess(Logger logger, Result result) {
		final Result finalResult = result;

		final Logger finalLogger = logger;

		handler.post(new Runnable() {
			public void run() {
				try {
					onSuccess(finalResult);
				} catch (Throwable t) {
					finalLogger.log(Logger.SEVERE,
							"Exception in success notification with message: "
									+ t.getMessage());

					t.printStackTrace();
				}
			}
		});
	}

	@Override
	public void notifyFailure(Logger logger, Throwable throwable) {
		final Throwable finalThrowable = throwable;

		final Logger finalLogger = logger;

		handler.post(new Runnable() {
			public void run() {
				try {
					onFailure(finalThrowable);
				} catch (Throwable t) {
					finalLogger.log(Logger.SEVERE,
							"Exception in failure notification with message: "
									+ t.getMessage());

					t.printStackTrace();
				}
			}
		});
	}
}
