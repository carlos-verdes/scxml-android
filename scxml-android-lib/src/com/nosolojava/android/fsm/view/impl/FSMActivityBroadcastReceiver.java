package com.nosolojava.android.fsm.view.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.io.FSM_EXTRAS;
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

		boolean passFilter = true;
		if (intent.hasExtra(FSM_EXTRAS.SESSION_ID.toString())) {
			passFilter = fsmSession.equals(intent.getStringExtra(FSM_EXTRAS.SESSION_ID.toString()));
		}

		if (passFilter) {
			//pass the intent to the handlers
			for (FSMViewBindingHandler handler : handlers) {
				handler.handleFSMIntent(intent);
			}
		}
	}

}
