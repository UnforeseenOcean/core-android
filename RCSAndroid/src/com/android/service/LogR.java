/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogR.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.util.ArrayList;

import com.android.service.util.DataBuffer;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class LogR.
 */
public class LogR {

	/** The type. */
	private final int type;

	/** The unique. */
	private final long unique;

	/** The disp. */
	private LogDispatcher disp;

	/** The Constant LOG_CREATE. */
	final public static int LOG_CREATE = 0x1;

	/** The Constant LOG_ATOMIC. */
	final public static int LOG_ATOMIC = 0x2;

	/** The Constant LOG_APPEND. */
	final public static int LOG_APPEND = 0x3;

	/** The Constant LOG_WRITE. */
	final public static int LOG_WRITE = 0x4;

	/** The Constant LOG_CLOSE. */
	final public static int LOG_CLOSE = 0x5;

	/** The Constant LOG_PRI_MAX. */
	final public static int LOG_PRI_MAX = 0x1;

	/** The Constant LOG_PRI_STD. */
	final public static int LOG_PRI_STD = 0x7f;

	/** The Constant LOG_PRI_MIN. */
	final public static int LOG_PRI_MIN = 0xff;

	/**
	 * Instantiates a new log, creates the evidence.
	 * 
	 * @param evidence
	 *            the log type
	 * @param priority
	 *            the priority
	 */
	public LogR(final int evidence, final int priority) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidence;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);

		send(p);
	}

	/**
	 * Instantiates a new log, creates the evidence with additional.
	 * 
	 * @param evidenceType
	 *            the log type
	 * @param priority
	 *            the priority
	 * @param additional
	 *            the additional
	 */
	public LogR(final int evidenceType, final int priority, final byte[] additional) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidenceType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);
		p.setAdditional(additional);

		send(p);
	}

	/**
	 * Instantiates a new log, creates atomically the evidence with additional
	 * and data.
	 * 
	 * @param evidenceType
	 *            the log type
	 * @param priority
	 *            the priority
	 * @param additional
	 *            the additional
	 * @param data
	 *            the data
	 */
	public LogR(final int evidenceType, final int priority, final byte[] additional, final byte[] data) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidenceType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_ATOMIC);
		p.setAdditional(additional);
		p.fill(data);

		send(p);
	}

	public LogR(final int evidenceType, final byte[] additional, final byte[] data) {
		this(evidenceType, LOG_PRI_STD, additional, data);
	}

	public LogR(int evidenceType) {
		this(evidenceType, LOG_PRI_STD);
	}

	// Send data to dispatcher
	/**
	 * Send.
	 * 
	 * @param p
	 *            the p
	 */
	private void send(final Packet p) {
		if (disp == null) {
			disp = LogDispatcher.self();

			if (disp == null) {
				return;
			}
		}

		disp.send(p);
	}

	/**
	 * Write or append data to the log.
	 * 
	 * @param data
	 *            the data
	 */
	public void write(final byte[] data) {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_WRITE);
		p.fill(data);

		send(p);
		return;
	}

	public void write(ArrayList<byte[]> bytelist) {
		int totalLen = 0;
		for (final byte[] token : bytelist) {
			totalLen += token.length;
		}

		final int offset = 0;
		final byte[] buffer = new byte[totalLen];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, totalLen);

		for (final byte[] token : bytelist) {
			databuffer.write(token);
		}

		write(buffer);
	}

	/**
	 * Close.
	 */
	public void close() {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_CLOSE);

		send(p);
		return;
	}
}
