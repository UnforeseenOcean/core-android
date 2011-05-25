/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventBattery.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.Battery;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerBattery;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventBattery extends EventBase implements Observer<Battery> {
	/** The Constant TAG. */
	private static final String TAG = "EventBattery";

	private int actionOnExit, actionOnEnter, minLevel, maxLevel;
	private boolean inRange = false;
	
	@Override
	public void begin() {
		ListenerBattery.self().attach(this);
	}

	@Override
	public void end() {
		ListenerBattery.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		
		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();
			minLevel = databuffer.readInt();
			maxLevel = databuffer.readInt();
			
			if(Cfg.DEBUG) Check.log( TAG + " exitAction: " + actionOnExit + " minLevel:" + minLevel + " maxLevel:" + maxLevel);
		} catch (final IOException e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: params FAILED");
			return false;
		}
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
	}

	public int notification(Battery b) {
		if(Cfg.DEBUG) Check.log( TAG + " Got battery notification: " + b.getBatteryLevel() + "%");
		
		if (minLevel > maxLevel)
			return 0;
		
		// Nel range
		if ((b.getBatteryLevel() >= minLevel && b.getBatteryLevel() <= maxLevel) && inRange == false) {
			inRange = true;
			if(Cfg.DEBUG) Check.log( TAG + " Battery IN");
			onEnter();
		}
     
		// Fuori dal range
		if ((b.getBatteryLevel() < minLevel || b.getBatteryLevel() > maxLevel) && inRange == true) {
			inRange = false;
			if(Cfg.DEBUG) Check.log( TAG + " Battery OUT");
			onExit();
		}
		
		return 0;
	}
	
	public void onEnter() {
		trigger(actionOnEnter);
	}

	public void onExit() {
		trigger(actionOnExit);
	}
}
