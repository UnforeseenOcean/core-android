/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.conf.RunningConf;

// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public class EventConf implements RunningConf {

	

	/** Events status. */
	final public static int EVENT_STOPPED = AgentConf.AGENT_STOPPED;

	/** The Constant EVENT_RUNNING. */
	final public static int EVENT_RUNNING = AgentConf.AGENT_RUNNING;

	/** Event type. */
	private final EventType eventType;

	/** Event unique ID. */
	private final int eventId;

	/** Event status: enabled, disabled, running, stopped. */
	private final int eventAction;

	/** Parameters. */
	private final byte[] eventParams;

	/**
	 * Instantiates a new event.
	 * 
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @param action
	 *            the action
	 * @param params
	 *            the params
	 */
	public EventConf(final EventType type, final int id, final int action,
			final byte[] params) {
		this.eventType = type;
		this.eventId = id;
		this.eventAction = action;
		this.eventParams = params;
	}

	/**
	 * Gets the type of the event.
	 * 
	 * @return the type
	 */
	public EventType getType() {
		return this.eventType;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.eventId;
	}

	/**
	 * Gets the action id to trigger.
	 * 
	 * @return the action
	 */
	public int getAction() {
		return this.eventAction;
	}

	/**
	 * Gets the parameters to parse.
	 * 
	 * @return the params
	 */
	public byte[] getParams() {
		return this.eventParams;
	}
}
