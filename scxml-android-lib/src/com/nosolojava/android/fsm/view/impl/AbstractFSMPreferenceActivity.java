package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;

import android.app.Activity;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.nosolojava.android.fsm.listener.FSMPreferenceChangeListener;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;

public abstract class AbstractFSMPreferenceActivity extends PreferenceActivity implements FSMActivityIntegration {
	protected final FSMActivityIntegration fsmHelper;

	abstract public int getInitViewId();

	abstract public Uri getInitFSMUri();

	private OnSharedPreferenceChangeListener onPrefChangeListener = null;

	public AbstractFSMPreferenceActivity() {
		super();

		fsmHelper = new FSMActivityIntegrationHelper(this, this.getInitViewId(), this.getInitFSMUri());

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int viewId = this.getInitViewId();
		Uri fsmUri = this.getInitFSMUri();

		this.setContentView(viewId);

		this.setCurrentViewId(viewId);
		this.setFSMUri(fsmUri);

		this.initFSMHandlers();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindWithFSM();

		this.onPrefChangeListener = new FSMPreferenceChangeListener(this.getSessionId(), this);
	}

	@Override
	protected void onPause() {
		if (this.onPrefChangeListener != null) {
			PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(
					onPrefChangeListener);
			this.onPrefChangeListener = null;
		}

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

	public void initFSMHandlers() {
		fsmHelper.initFSMHandlers();
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
