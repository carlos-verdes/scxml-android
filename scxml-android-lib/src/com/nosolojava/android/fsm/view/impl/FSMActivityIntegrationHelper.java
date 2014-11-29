package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.ViewBindingParser;
import com.splunk.mint.Mint;

/**
 * Abstract class used in the different activity/fragments implementations to delegate FSM activity methods.
 * 
 * @author Carlos Verdes
 * 
 */
public class FSMActivityIntegrationHelper implements FSMActivityIntegration {

	private static final String LOG_TAG = "fsBind";

	private final Activity currentActivity;
	private int currentView = -1;
	private String fsmSession = null;
	private Uri fsmUri = null;
	private final List<FSMViewBindingHandler> viewHandlers = new ArrayList<FSMViewBindingHandler>();
	private ViewBindingParser viewBindingParser = new ViewBindingParser();

	private final List<FSMViewBindingHandler> activeHandlers = new ArrayList<FSMViewBindingHandler>();

	private BroadcastReceiver receiver;
	private IntentFilter fsmIntentFilter = null;

	@Override
	public Activity getAndroidContext() {
		return this.currentActivity;
	}

	public FSMActivityIntegrationHelper(Activity activity, int viewId, Uri fsmUri) {
		super();

		this.currentActivity = activity;

		initActivityAndView(viewId, fsmUri);

	}

	protected boolean initActivityAndView(int viewId, Uri fsmUri) {
		boolean hasChangedConfig = false;

		if (this.getCurrentViewId() != viewId) {
			this.setCurrentViewId(viewId);
			hasChangedConfig = true;
		}

		// intent action from fsm
		String sessionId = fsmUri.getFragment();
		if (this.fsmSession == null || !this.fsmSession.equals(sessionId)) {
			hasChangedConfig = true;
			this.fsmSession = sessionId;

		}

		if (hasChangedConfig) {

			unregisterReceiver();

			fsmIntentFilter = new IntentFilter();
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_NEW_SESSION_CONFIG.toString());
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_SESSION_INITIATED.toString());
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_SESSION_ENDED.toString());

			AndroidUtils.addSessionToFilter(fsmIntentFilter, this.getSessionId());

			registerReceiver();
		}

		return hasChangedConfig;
	}

	@Override
	public void initFSMHandlers() {

		Activity activity = this.getAndroidContext();
		// init bug sense
		Mint.initAndStartSession(activity, "f29095fc");

		int viewId = this.getCurrentViewId();
		Uri fsmUri = this.getFSMUri();

		// init vars and check if there is any change in config
		initActivityAndView(viewId, fsmUri);

		// init handlers
		for (FSMViewBindingHandler activeHandler : this.activeHandlers) {
			activeHandler.reset();
		}

		// if there is any view based handler --> parse them
		if (!viewHandlers.isEmpty()) {
			View rootView = this.currentActivity.findViewById(this.currentView);
			parseViewBasedHandlers(rootView);

		}

		// parse view
		this.viewBindingParser.parseXppViewBasedHandlers(this.currentActivity, this.currentView);

		// update the active handlers, this will not be updated until next configuration change
		updateActiveHandlers();

		// create a broadcast receiver for FSM events
		this.receiver = new FSMActivityBroadcastReceiver(this.activeHandlers, this.fsmSession);

		// notify binding handlers
		for (FSMViewBindingHandler handler : activeHandlers) {
			handler.onInitActivity(currentActivity);
		}

		// intent to start the fsm
		sendInitFSMIntent();

	}

	protected void sendInitFSMIntent() {
		Activity activity = this.getAndroidContext();
		Uri fsmUri = this.getFSMUri();
		Intent startFSMintent = new Intent(this.getAndroidContext(), FSMServiceImpl.class);
		startFSMintent.setAction(FSM_ACTIONS.INIT_FSM_SESSION.toString());
		startFSMintent.setData(fsmUri);
		activity.startService(startFSMintent);
	}

	protected void updateActiveHandlers() {
		Set<FSMViewBindingHandler> allHandlers = getAllHandlers();
		this.activeHandlers.clear();
		this.activeHandlers.addAll(allHandlers);
	}

	private <T extends View> void parseViewBasedHandlers(View view) {

		// register the binding for all the handlers
		for (FSMViewBindingHandler handler : this.viewHandlers) {
			registerBinding(view, handler);
		}

		// if the view is a group of views
		if (ViewGroup.class.isAssignableFrom(view.getClass())) {
			// parse each children
			ViewGroup aux = (ViewGroup) view;
			int childrenCount = aux.getChildCount();
			for (int i = 0; i < childrenCount; i++) {
				View children = aux.getChildAt(i);
				parseViewBasedHandlers(children);

			}

		}
	}

	protected void registerBinding(View view, FSMViewBindingHandler handler) {
		Class<? extends View> viewClass = handler.getViewClass();
		// if this handler can manage this view
		if (viewClass.isAssignableFrom(view.getClass())) {
			handler.registerViewBinding(view, null);
		}
	}

	@Override
	public void bindWithFSM() {
		if (this.currentActivity == null || this.getCurrentViewId() == -1) {
			Log.w(LOG_TAG, "To bind with FSM first call FSMActivityIntegration.initFSMHandlers(activity,viewId) method");
		} else {
			// register receiver
			registerReceiver();

			// send intent to fsm so it can send actual configuration (active states, assignments, etc).
			sendInitFSMIntent();

			for (FSMViewBindingHandler activeHandler : this.activeHandlers) {
				activeHandler.onBind(this.currentActivity, this.fsmSession);
			}

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

	@Override
	public void unbindWithFSM() {
		if (this.currentActivity == null || this.getCurrentViewId() == -1) {
			Log.w(LOG_TAG, "To bind with FSM first call FSMActivityIntegration.initFSMHandlers(activity,viewId) method");
		} else {

			unregisterReceiver();
			for (FSMViewBindingHandler activeHandler : this.activeHandlers) {
				activeHandler.onUnbind(this.currentActivity, this.fsmSession);
			}

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

	@Override
	public void pushEventToFSM(String eventName) {
		Parcelable data = null;
		pushEventToFSM(eventName, data);

	}

	@Override
	public void pushEventToFSM(String eventName, Parcelable data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.fsmSession, this.currentActivity, eventName, data);
	}

	@Override
	public void pushEventToFSM(String eventName, Serializable data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.fsmSession, this.currentActivity, eventName, data);
	}

	@Override
	public void registerFSMViewBindingHandler(FSMViewBindingHandler handler) {
		this.viewHandlers.add(handler);
	}

	@Override
	public boolean unregisterFSMViewBindingHandler(FSMViewBindingHandler handler) {
		return this.viewHandlers.remove(handler);
	}

	public boolean registerFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.viewBindingParser.registerFSMViewBindingHandler(handler);
	}

	public Object unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.viewBindingParser.unregisterFSMViewBindingHandler(handler);
	}

	public Set<FSMViewBindingHandler> getAllHandlers() {

		Set<FSMViewBindingHandler> handlers = new HashSet<FSMViewBindingHandler>();
		handlers.addAll(this.viewHandlers);

		for (List<XPPFSMViewBindingHandler> xppHandlers : this.viewBindingParser.getHandlerList()) {
			handlers.addAll(xppHandlers);
		}

		return handlers;

	}

	@Override
	public int getCurrentViewId() {
		return this.currentView;
	}

	@Override
	public void setCurrentViewId(int viewId) {
		this.currentView = viewId;
	}

	@Override
	public void setFSMUri(Uri fsmUri) {
		this.fsmUri = fsmUri;
	}

	@Override
	public Uri getFSMUri() {
		return this.fsmUri;
	}

	@Override
	public String getSessionId() {
		return this.fsmSession;
	}

}
