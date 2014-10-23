package com.nosolojava.android.fsm.service;

import java.io.Serializable;
import java.util.Map;

import android.content.Context;

import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class AndroidFSMContext extends JexlFSMContext {

	private final Context androidContext;

	public AndroidFSMContext(Context androidContext, String sessionId, String parentSessionId, StateMachineModel model,
			StateMachineEngine engine, Map<String, Serializable> initValues) throws ConfigurationException {
		super(sessionId, parentSessionId, model, engine, initValues);

		this.androidContext = androidContext;
	}

	public Context getAndroidContext() {
		return androidContext;
	}

	public static AndroidFSMContext getIntance(com.nosolojava.fsm.runtime.Context context) {
		return (AndroidFSMContext) context;
	}


}
