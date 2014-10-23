package com.nosolojava.android.fsm.view.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bugsense.trace.BugSenseHandler;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.ConnectionLostBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.OnNewStateBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.OnclickBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.ValueBindingHandler;

/**
 * Abstract class used in the different activity/fragments implementations to delegate FSM activity methods.
 * 
 * @author Carlos Verdes
 * 
 */
public class FSMActivityIntegrationHelper implements FSMActivityIntegration {

	private static final String LOG_TAG = "fsBind";
	public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	public static final String ID = "id";

	private final Activity currentActivity;
	private int currentView = -1;
	private String fsmSession = null;
	private Uri fsmUri = null;
	private final List<FSMViewBindingHandler> viewHandlers = new ArrayList<FSMViewBindingHandler>();
	private final Map<String, List<XPPFSMViewBindingHandler>> xppViewHandlers = new HashMap<String, List<XPPFSMViewBindingHandler>>();

	private final List<FSMViewBindingHandler> activeHandlers = new ArrayList<FSMViewBindingHandler>();

	private BroadcastReceiver receiver;
	private IntentFilter fsmIntentFilter = null;

	@Override
	public Activity getAndroidContext() {
		return this.currentActivity;
	}

	public FSMActivityIntegrationHelper(Activity activity, int viewId, Uri fsmUri) {
		super();

		this.currentActivity=activity;
		
		initActivityAndView(viewId, fsmUri);

		// on state change handler
		registerFSMViewBindingHandler(new OnNewStateBindingHandler());

		// value binding handler
		registerFSMViewBindingHandler(new ValueBindingHandler());

		// onclick handler
		registerFSMViewBindingHandler(new OnclickBindingHandler());

		//on session expired handler
		registerFSMViewBindingHandler(new ConnectionLostBindingHandler());

	}

	@Override
	public void initFSMHandlers() {
		

		Activity activity = this.getAndroidContext();
		//init bug sense
		BugSenseHandler.initAndStartSession(activity, "f29095fc");

		int viewId = this.getCurrentViewId();
		Uri fsmUri = this.getFSMUri();

		// init vars and check if there is any change in config
		initActivityAndView(viewId, fsmUri);

		//init handlers
		for (FSMViewBindingHandler activeHandler : this.activeHandlers) {
			activeHandler.reset();
		}

		//if there is any view based handler --> parse them
		if (!viewHandlers.isEmpty()) {
			View rootView = this.currentActivity.findViewById(this.currentView);
			parseViewBasedHandlers(rootView);

		}

		// if there is any xpp based handlers
		if (!xppViewHandlers.isEmpty()) {
			XmlResourceParser xrp = this.currentActivity.getResources().getXml(this.currentView);

			// parse layout
			parseXppViewBasedHandlers(xrp);

		}
		//update the active handlers, this will not be updated until next configuration change
		updateActiveHandlers();

		// create a broadcast receiver for FSM events
		this.receiver = new FSMActivityBroadcastReceiver(this.activeHandlers, this.fsmSession);


		//notify binding handlers
		for (FSMViewBindingHandler handler : activeHandlers) {
			handler.onInitActivity(currentActivity);
		}

		// intent to start the fsm
		Intent startFSMintent = new Intent(FSM_ACTIONS.INIT_FSM_SESSION.toString(), fsmUri);
		activity.startService(startFSMintent);

	}

	protected void updateActiveHandlers() {
		Set<FSMViewBindingHandler> allHandlers = getAllHandlers();
		this.activeHandlers.clear();
		this.activeHandlers.addAll(allHandlers);
	}

	private void parseXppViewBasedHandlers(XmlPullParser xpp) {
		try {
			xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

			//until the end of the document
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {

				if (xpp.getEventType() == XmlPullParser.START_TAG) {

					String namespace = xpp.getNamespace();

					// if namespace is Android
					if ("".equals(namespace) || ANDROID_NS.equals(namespace)) {
						//parse android element (att based)
						parseAndroidElement(xpp);

					} else
					//if there is any handler
					if (this.xppViewHandlers.containsKey(namespace)) {
						//parse the custom element (element based)
						parseCustomXmlElement(xpp, namespace);

					}

					//go to next xpp event
					xpp.next();

				} else {
					//on any other xpp event do next
					xpp.next();
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error parsing", e);
		}

	}

	protected void parseCustomXmlElement(XmlPullParser xpp, String namespace) {
		for (XPPFSMViewBindingHandler handler : this.xppViewHandlers.get(namespace)) {
			handler.registerXMLElementBinding(xpp);
		}
	}

	protected void parseAndroidElement(XmlPullParser xpp) {
		View view = null;

		int attCount = xpp.getAttributeCount();
		String attNs;
		// for each attribute
		for (int i = 0; i < attCount; i++) {
			attNs = xpp.getAttributeNamespace(i);
			// if some element has a handler
			if (!ANDROID_NS.equals(attNs) && this.xppViewHandlers.containsKey(attNs)) {
				registerXppAttBinding(xpp, view, attNs, i);
			}
		}
	}

	protected void registerXppAttBinding(XmlPullParser xpp, View view, String attNs, int i) {
		String attValue;
		String attName;
		if (view == null) {
			view = this.getViewFromXpp(xpp, ANDROID_NS);
		}

		if (view != null) {
			attName = xpp.getAttributeName(i);
			attValue = xpp.getAttributeValue(i);
			List<XPPFSMViewBindingHandler> handlers = this.xppViewHandlers.get(attNs);
			for (XPPFSMViewBindingHandler handler : handlers) {
				handler.registerXMLAttributeBinding(view, attName, attValue);
			}
		}
	}

	private <T extends View> void parseViewBasedHandlers(View view) {

		// register the binding for all the handlers
		for (FSMViewBindingHandler handler : this.viewHandlers) {
			registerBinding(view, handler);
		}

		// if the view is a group of views
		if (ViewGroup.class.isAssignableFrom(view.getClass())) {
			//parse each children
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
		//if this handler can manage this view
		if (viewClass.isAssignableFrom(view.getClass())) {
			handler.registerViewBinding(view, null);
		}
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
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_ACTIVE_STATES.toString());
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_ASSIGN.toString());
			fsmIntentFilter.addAction(FSM_ACTIONS.FSM_SESSION_EXPIRED.toString());

			AndroidUtils.addSessionToFilter(fsmIntentFilter, this.getSessionId());

			registerReceiver();
		}

		return hasChangedConfig;
	}

	@Override
	public void bindWithFSM() {
		if (this.currentActivity == null || this.getCurrentViewId() == -1) {
			Log.w(LOG_TAG, "To bind with FSM first call FSMActivityIntegration.initFSMHandlers(activity,viewId) method");
		} else {
			//register receiver
			registerReceiver();

			// send intent to fsm so it can send actual configuration (active states, assignments, etc).
			Intent intent = new Intent(FSM_ACTIONS.INIT_ACTIVITY.toString());
			intent.putExtra(FSM_EXTRAS.SESSION_ID.toString(), this.fsmSession);
			this.currentActivity.startService(intent);

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
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.currentActivity, this.fsmSession, eventName, data);
	}

	@Override
	public void pushEventToFSM(String eventName, Serializable data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this.currentActivity, this.fsmSession, eventName, data);
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
		List<XPPFSMViewBindingHandler> handlers;
		String namespace = handler.getNamespace();
		if (this.xppViewHandlers.containsKey(namespace)) {
			handlers = this.xppViewHandlers.get(namespace);
		} else {
			handlers = new ArrayList<XPPFSMViewBindingHandler>();
		}

		handlers.add(handler);
		this.xppViewHandlers.put(namespace, handlers);

		return true;
	}

	public Object unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.xppViewHandlers.remove(handler.getNamespace());
	}

	public Set<FSMViewBindingHandler> getAllHandlers() {

		Set<FSMViewBindingHandler> handlers = new HashSet<FSMViewBindingHandler>();
		handlers.addAll(this.viewHandlers);

		for (List<XPPFSMViewBindingHandler> xppHandlers : this.xppViewHandlers.values()) {
			handlers.addAll(xppHandlers);
		}

		return handlers;

	}

	private View getViewFromXpp(XmlPullParser xpp, String androidNs) {

		int id = getViewId(xpp, androidNs);

		View view = id != -1 ? this.currentActivity.findViewById(id) : null;

		return view;

	}

	private int getViewId(XmlPullParser xpp, String androidNs) {
		int id = -1;

		String idString = xpp.getAttributeValue(androidNs, ID);
		if (idString == null) {
			Log.w(LOG_TAG,
					"Error getting android id, review your layout elements (all must have an id to do a binding).");
		} else {
			idString = idString.replaceAll("\\D", "");
			id = Integer.parseInt(idString);
		}
		return id;
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
