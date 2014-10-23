package com.nosolojava.android.fsm.service;

import java.io.Serializable;
import java.util.Map;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.ContextFactory;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class AndroidContextFactory implements ContextFactory {

	private final android.content.Context androidContext;

	public AndroidContextFactory(android.content.Context androidContext) {
		super();
		this.androidContext = androidContext;
	}

	@Override
	public Context createContext(String sessionId, String parentSessionId, StateMachineModel model,
			StateMachineEngine engine, Map<String, Serializable> initValues) throws ConfigurationException {
		return new AndroidFSMContext(this.androidContext, sessionId, parentSessionId, model, engine, initValues);
	}

}
