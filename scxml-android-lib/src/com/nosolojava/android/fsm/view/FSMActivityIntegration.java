package com.nosolojava.android.fsm.view;

import java.io.Serializable;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcelable;

import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;

public interface FSMActivityIntegration {

	Activity getAndroidContext();
	int getCurrentViewId();
	void setCurrentViewId(int viewId);
	Uri getFSMUri();
	void setFSMUri(Uri fsmUri);
	String getSessionId();
	
	void initFSMHandlers();

	void bindWithFSM();

	void unbindWithFSM();

	void registerFSMViewBindingHandler(FSMViewBindingHandler handler);

	boolean unregisterFSMViewBindingHandler(FSMViewBindingHandler handler);

	void pushEventToFSM(String eventName);

	void pushEventToFSM(String eventName, Serializable data);

	void pushEventToFSM(String eventName, Parcelable data);

//	void handleFsmMessage(Message fsmMessage);
//
//	void onNewStateConfig(Set<String> activeStates);

}