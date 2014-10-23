package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;

public abstract class AbstractFSMFragment extends Fragment implements FSMActivityIntegration {

	protected FSMActivityIntegration fsmHelper = null;

	//abstract methods
	abstract public int getInitViewId();

	abstract public Uri getInitFSMUri();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		fsmHelper = new FSMActivityIntegrationHelper(activity, this.getInitViewId(), this.getInitFSMUri());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int viewId = this.getInitViewId();
		Uri fsmUri = this.getInitFSMUri();

		this.setCurrentViewId(viewId);
		this.setFSMUri(fsmUri);

		this.initFSMHandlers();

	}

	@Override
	public void onResume() {
		super.onResume();
		bindWithFSM();
	}

	@Override
	public void onPause() {
		unbindWithFSM();
		super.onPause();
	}

	@Override
	public void bindWithFSM() {
		fsmHelper.bindWithFSM();
	}

	@Override
	public void unbindWithFSM() {
		fsmHelper.unbindWithFSM();
	}

	@Override
	public void initFSMHandlers() {
		this.fsmHelper.initFSMHandlers();
	}

	public void registerFSMViewBindingHandler(FSMViewBindingHandler handler) {
		fsmHelper.registerFSMViewBindingHandler(handler);
	}

	public boolean unregisterFSMViewBindingHandler(FSMViewBindingHandler handler) {
		return fsmHelper.unregisterFSMViewBindingHandler(handler);
	}

	public void pushEventToFSM(String eventName) {
		fsmHelper.pushEventToFSM(eventName);
	}

	public void pushEventToFSM(String eventName, Serializable data) {
		fsmHelper.pushEventToFSM(eventName, data);
	}

	@Override
	public void pushEventToFSM(String eventName, Parcelable body) {
		this.fsmHelper.pushEventToFSM(eventName, body);
	}

	@Override
	public void setCurrentViewId(int viewId) {
		this.fsmHelper.setCurrentViewId(viewId);
	}

	@Override
	public void setFSMUri(Uri fsmUri) {
		this.fsmHelper.setFSMUri(fsmUri);
	}

	public Activity getAndroidContext() {
		return fsmHelper.getAndroidContext();
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

}
