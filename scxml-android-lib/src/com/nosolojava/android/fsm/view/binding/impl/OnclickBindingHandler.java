package com.nosolojava.android.fsm.view.binding.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * Send an event to FSM when a View has a binding attribute "onclick". The event name is the value of the attribute.
 * 
 * @author Carlos Verdes
 * 
 */
public class OnclickBindingHandler extends AbstractFSMViewBindingHandler {

	public static String ONCLICK_ATTRIBUTE = "onclick";

	private Map<View, OnClickListener> onclickBindingMap = new HashMap<View, OnClickListener>();

	@Override
	public void reset() {
		this.onclickBindingMap.clear();
	}

	public <T extends View> void registerViewBinding(final T view, final String eventName) {

		OnClickListener onclick = new FSMOnClickListener(eventName);
		view.setOnClickListener(onclick);
		this.onclickBindingMap.put(view, onclick);
	}

	class FSMOnClickListener implements OnClickListener {

		private final String eventName;

		public FSMOnClickListener(String eventName) {
			super();
			this.eventName = eventName;
		}

		@Override
		public void onClick(View v) {
			if (currentActivity != null) {
				AndroidBroadcastIOProcessor.sendMessageToFSM(fsmSessionId, currentActivity, fsmServiceClazz, eventName);
			}
		}
	}

	@Override
	public void registerXMLAttributeBinding(View view, String bindingAttribute, String bindingValue) {
		if (ONCLICK_ATTRIBUTE.equals(bindingAttribute)) {
			registerViewBinding(view, bindingValue);
		}
	}

	@Override
	public void onBind(final Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {
		super.onBind(activity, fsmServiceClazz, fsmSessionId);

		for (Entry<View, OnClickListener> entry : this.onclickBindingMap.entrySet()) {
			entry.getKey().setOnClickListener(entry.getValue());
		}
	}

	@Override
	public void onUnbind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {

		for (Entry<View, OnClickListener> entry : this.onclickBindingMap.entrySet()) {
			entry.getKey().setOnClickListener(null);
		}
		super.onUnbind(activity, fsmServiceClazz, fsmSessionId);

	}

	@Override
	public void registerXMLElementBinding(XmlPullParser xpp) {
	}

	@Override
	public boolean handleFSMIntent(Intent intent) {
		return false;
	}

	@Override
	public void updateView(ContextInstance newContextInstance) {
	}

}
