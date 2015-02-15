package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;
import java.util.UUID;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.ViewBindingParser;

/**
 * Abstract class used in the different activity/fragments implementations to delegate FSM activity methods.
 * 
 * @author Carlos Verdes
 * 
 */
public class FSMActivityIntegrationHelper {

	private static final String LOG_TAG = "fsBind";

	// activity bound to the fsm service
	private final Activity currentActivity;
	private int currentView;
	private final FSMActivityIntegration fsmActivity;

	// fsm session
	private String fsmSession = null;

	// fsm uri
	private Uri fsmUri;

	// android fsm service class (to send intents from activity to fsm)
	private final Class<? extends Service> fsmServiceClazz;

	// parser that implements relations between fsm configurations (current state + model data) and the view
	private ViewBindingParser viewBindingParser;

	private ViewStateLoaderHandler viewStateLoaderHandler;

	private BroadcastReceiver receiver;
	private IntentFilter fsmIntentFilter = null;

	public FSMActivityIntegrationHelper(Activity activity, Class<? extends Service> fsmServiceClazz, int viewId,
			Uri fsmUri) {
		super();

		this.fsmServiceClazz = fsmServiceClazz;
		this.viewBindingParser = new ViewBindingParser(this.fsmServiceClazz);

		this.currentActivity = activity;
		this.fsmActivity = (FSMActivityIntegration) activity;
		this.currentView = viewId;

		this.viewStateLoaderHandler = new ViewStateLoaderHandler(activity, fsmServiceClazz);

		initFsmUriAndSession(fsmUri);

		// init intent filter
		this.fsmIntentFilter = new IntentFilter();

		this.fsmIntentFilter.addAction(FSM_ACTIONS.FSM_SESSION_INITIATED.toString());
		this.fsmIntentFilter.addAction(FSM_ACTIONS.FSM_SESSION_ENDED.toString());
		this.fsmIntentFilter.addAction(FSM_ACTIONS.FSM_NEW_SESSION_CONFIG.toString());

		this.fsmIntentFilter.addDataScheme("fsm");
		this.fsmIntentFilter.addDataAuthority(this.fsmSession, null);

		// init receiver
		this.receiver = new FSMIntentReceiver(this.fsmActivity, this.viewStateLoaderHandler);

	}

	protected void initFsmUriAndSession(Uri fsmUri) {
		String uriFragment = fsmUri.getFragment();
		if (uriFragment == null || "".equals(uriFragment)) {
			uriFragment = UUID.randomUUID().toString();

			fsmUri = Uri.parse(fsmUri.toString() + "#" + uriFragment);
		}

		this.fsmUri = fsmUri;
		this.fsmSession = this.fsmUri.getFragment();
	}

	public void sendInitFSMIntent() {
		Uri fsmUri = this.getFSMUri();
		Intent startFSMintent = new Intent(this.getAndroidContext(), this.fsmServiceClazz);
		startFSMintent.setAction(FSM_ACTIONS.INIT_FSM_SESSION.toString());
		startFSMintent.setData(fsmUri);
		this.getAndroidContext().startService(startFSMintent);
	}

	public void bindWithFSM() {
		if (this.currentActivity == null || this.getCurrentViewId() == -1) {
			Log.w(LOG_TAG, "To bind with FSM you should provide a valid activity and viewId");
		} else {
			// register receiver
			registerReceiver();

			// init parsers
			this.viewBindingParser.parseXppViewBasedHandlers(this.currentActivity, this.getCurrentViewId());

			// and bind
			this.viewBindingParser.bindHandlers(this.currentActivity, this.fsmServiceClazz, this.fsmSession);

			// send intent to fsm so it can send actual configuration (active states, assignments, etc).
			sendInitFSMIntent();

		}

	}

	public void unbindWithFSM() {
		if (this.currentActivity == null || this.getCurrentViewId() == -1) {
			Log.w(LOG_TAG, "To bind with FSM first call FSMActivityIntegration.initFSMHandlers(activity,viewId) method");
		} else {

			unregisterReceiver();

			// and unbind
			this.viewBindingParser.unbindHandlers(this.currentActivity, this.fsmServiceClazz, this.fsmSession);

		}

	}

	protected void registerReceiver() {
		try {
			if (this.receiver != null) {
				this.currentActivity.registerReceiver(this.receiver, fsmIntentFilter);
			}
		} catch (Exception e) {
			Log.w(LOG_TAG, "Error registering receiver", e);

		}
	}

	protected void unregisterReceiver() {
		try {
			if (this.receiver != null) {
				this.currentActivity.unregisterReceiver(receiver);
			}
		} catch (Exception e) {
			Log.w(LOG_TAG, "Error unregistering receiver", e);
		}
	}

	public void pushEventToFSM(String eventName) {
		Parcelable data = null;
		pushEventToFSM(eventName, data);

	}

	public void pushEventToFSM(String eventName, Parcelable data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.fsmSession, this.currentActivity, this.fsmServiceClazz,
				eventName, data);
	}

	public void pushEventToFSM(String eventName, Serializable data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.fsmSession, this.currentActivity, this.fsmServiceClazz,
				eventName, data);
	}

	public boolean registerFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.viewBindingParser.registerFSMViewBindingHandler(handler);
	}

	public Object unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.viewBindingParser.unregisterFSMViewBindingHandler(handler);
	}

	public void associateStateView(Integer view, Runnable callback, String... states) {
		this.viewStateLoaderHandler.registerStateView(view, callback, states);
	}

	public int getCurrentViewId() {
		return this.currentView;
	}

	public void setCurrentViewId(int viewId) {
		this.currentView = viewId;
	}

	public void setFSMUri(Uri fsmUri) {
		this.fsmUri = fsmUri;
	}

	public Uri getFSMUri() {
		return this.fsmUri;
	}

	public String getSessionId() {
		return this.fsmSession;
	}

	public Activity getAndroidContext() {
		return this.currentActivity;
	}

}
