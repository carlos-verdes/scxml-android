package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;

public abstract class AbstractFSMActivity extends Activity implements FSMActivityIntegration {

	protected final FSMActivityIntegration fsmHelper;

	abstract public int getInitViewId();
	abstract public Uri getInitFSMUri();
	

	public AbstractFSMActivity() {
		super();

		fsmHelper = new FSMActivityIntegrationHelper(this,getInitViewId(),getInitFSMUri());
	}

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int viewId= this.getInitViewId();
		Uri fsmUri= this.getInitFSMUri();
		
		this.setContentView(viewId);
		
		this.setCurrentViewId(viewId);
		this.setFSMUri(fsmUri);
		
		this.initFSMHandlers();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindWithFSM();

	}

	@Override
	protected void onPause() {
		unbindWithFSM();
		super.onPause();
	}

	/* 
	 * Delegated methods
	 * */
	@Override
	public void bindWithFSM() {
		fsmHelper.bindWithFSM();
	}

	@Override
	public void unbindWithFSM() {
		fsmHelper.unbindWithFSM();
	}

	@Override
	public void pushEventToFSM(String eventName, Serializable data) {
		fsmHelper.pushEventToFSM(eventName, data);
	}

	@Override
	public void initFSMHandlers() {
		this.fsmHelper.initFSMHandlers();
	}

	@Override
	public void registerFSMViewBindingHandler(FSMViewBindingHandler handler) {
		this.fsmHelper.registerFSMViewBindingHandler(handler);
	}

	@Override
	public boolean unregisterFSMViewBindingHandler(FSMViewBindingHandler handler) {
		return this.fsmHelper.unregisterFSMViewBindingHandler(handler);
	}

	@Override
	public void pushEventToFSM(String eventName) {
		this.fsmHelper.pushEventToFSM(eventName);
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
