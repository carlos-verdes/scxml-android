package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;

import android.app.Activity;
import android.app.Service;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.fsm.runtime.ContextInstance;

public abstract class BasicFSMFragment extends Fragment implements FSMActivityIntegration {

	protected FSMActivityIntegrationHelper fsmHelper;
	private Class<? extends Service> fsmServiceClazz;
	private int viewId;
	private Uri fsmUri;

	// abstract methods
	abstract public int getInitViewId();

	abstract public Uri getInitFSMUri();

	public BasicFSMFragment(Class<? extends Service> fsmServiceClazz, int viewId, Uri fsmUri) {
		super();

		this.fsmServiceClazz = fsmServiceClazz;
		this.viewId = viewId;
		this.fsmUri = fsmUri;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		fsmHelper = new FSMActivityIntegrationHelper(activity, fsmServiceClazz, viewId, fsmUri);

	}

	@Override
	public void onStart() {
		super.onStart();
		this.fsmHelper.bindWithFSM();
	}

	@Override
	public void onStop() {
		this.fsmHelper.unbindWithFSM();
		super.onStop();
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
