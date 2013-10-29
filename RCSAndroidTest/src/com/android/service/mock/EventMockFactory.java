package com.android.service.mock;

import com.android.service.event.BaseEvent;
import com.android.service.event.FactoryEvent;
import com.android.service.interfaces.AbstractFactory;

public class EventMockFactory implements AbstractFactory<BaseEvent,String>{

	@Override
	public BaseEvent create(String params, String subtype) {

		return new MockEvent();
	}

}
