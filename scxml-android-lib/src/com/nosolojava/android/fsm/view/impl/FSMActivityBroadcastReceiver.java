package com.nosolojava.android.fsm.view.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.android.fsm.view.binding.FSMViewBindingHandler;

public class FSMActivityBroadcastReceiver extends BroadcastReceiver {

	private final CopyOnWriteArrayList<FSMViewBindingHandler> handlers= new CopyOnWriteArrayList<FSMViewBindingHandler>();
	private final String fsmSession;

	public FSMActivityBroadcastReceiver(List<FSMViewBindingHandler> handlers, String fsmSession) {
		super();
		this.handlers.addAll(handlers);
		this.fsmSession = fsmSession;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		// TODO this should be done with intent filters
		String sessionId= AndroidUtils.getFSMSessionFromFSMIntent(intent);
		if (fsmSession.equals(sessionId)) {
			//pass the intent to the handlers
			for (FSMViewBindingHandler handler : handlers) {
				handler.handleFSMIntent(intent);
			}
		}
	}

}
