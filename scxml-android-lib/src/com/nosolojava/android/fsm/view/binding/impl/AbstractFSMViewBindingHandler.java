package com.nosolojava.android.fsm.view.binding.impl;

import android.app.Activity;
import android.app.Service;

import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;

public abstract class AbstractFSMViewBindingHandler implements XPPFSMViewBindingHandler {

	public static String BASIC_FSM_VIEW_HANDLER_NAMESPACE = "http://nosolojava.com/fsmViewBinding";

	protected String fsmSessionId = null;
	protected Activity currentActivity = null;

	protected Class<? extends Service> fsmServiceClazz;

	@Override
	public String getNamespace() {
		return BASIC_FSM_VIEW_HANDLER_NAMESPACE;
	}

	@Override
	public void onBind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {
		this.fsmSessionId = fsmSessionId;
		this.currentActivity = activity;
		this.fsmServiceClazz= fsmServiceClazz;
	}

	@Override
	public void onUnbind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {
		this.fsmSessionId = null;
		this.currentActivity = null;
		this.fsmServiceClazz= null;

	}
	
}
