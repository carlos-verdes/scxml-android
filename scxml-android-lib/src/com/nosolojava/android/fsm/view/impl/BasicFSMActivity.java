package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;

import android.app.Activity;
import android.app.Service;
import android.net.Uri;
import android.os.Parcelable;

import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.fsm.runtime.ContextInstance;

public class BasicFSMActivity extends Activity implements FSMActivityIntegration {

	protected final FSMActivityIntegrationHelper fsmHelper;

	public BasicFSMActivity(Class<? extends Service> fsmServiceClazz, int viewId, Uri fsmUri) {
		super();

		fsmHelper = new FSMActivityIntegrationHelper(this, fsmServiceClazz, viewId, fsmUri);
	}

	@Override
	protected void onStart() {
		super.onResume();
		// bind with fsm
		fsmHelper.bindWithFSM();

	}

	@Override
	protected void onStop() {
		// unbind with fsm
		fsmHelper.unbindWithFSM();
		super.onPause();
	}

	/*
	 * Delegated methods
	 */

	@Override
	public void pushEventToFSM(String eventName, Serializable data) {
		fsmHelper.pushEventToFSM(eventName, data);
	}

	@Override
	public void registerFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		this.fsmHelper.registerFSMViewBindingHandler(handler);
	}

	@Override
	public boolean unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.fsmHelper.unregisterFSMViewBindingHandler(handler) != null;
	}

	@Override
	public void pushEventToFSM(String eventName) {
		this.fsmHelper.pushEventToFSM(eventName);
	}

	@Override
	public void pushEventToFSM(String eventName, Parcelable body) {
		this.fsmHelper.pushEventToFSM(eventName, body);
	}

	public int getCurrentViewId() {
		return fsmHelper.getCurrentViewId();
	}

	public Uri getFSMUri() {
		return fsmHelper.getFSMUri();
	}

	public String getSessionId() {
		return fsmHelper.getSessionId();
	}

	@Override
	public void associateStateView(Integer view, Runnable callback, String... states) {
		fsmHelper.associateStateView(view, callback, states);
	}

	@Override
	public void onNewStateConfig(ContextInstance contextInstance) {

	}

}
