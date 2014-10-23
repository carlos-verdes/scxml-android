package com.nosolojava.android.fsm.view.binding.impl;

import java.io.Serializable;

import android.app.Activity;
import android.view.View;

import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;

public abstract class AbstractFSMViewBindingHandler implements XPPFSMViewBindingHandler {

	public static String BASIC_FSM_VIEW_HANDLER_NAMESPACE = "http://nosolojava.com/fsmViewBinding";

	protected String fsmSessionId = null;
	protected Activity currentActivity = null;

	@Override
	public Class<? extends View> getViewClass() {
		return View.class;
	}

	@Override
	public String getNamespace() {
		return BASIC_FSM_VIEW_HANDLER_NAMESPACE;
	}

	@Override
	public void onBind(Activity activity, String fsmSessionId) {
		this.fsmSessionId = fsmSessionId;
		this.currentActivity = activity;
	}

	@Override
	public void onUnbind(Activity activity, String fsmSessionId) {
		this.fsmSessionId = null;
		this.currentActivity = null;

	}

	@Override
	public <T extends View> void registerViewBinding(T view, Serializable dataBinding) {
		throw new UnsupportedOperationException(
				"For xml based handlers use registerXMLAttributeBinding method instead.");
	}

	
}
