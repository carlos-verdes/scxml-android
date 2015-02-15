package com.nosolojava.android.fsm.view.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;
import com.nosolojava.fsm.runtime.ContextInstance;

public class FSMIntentReceiver extends BroadcastReceiver {

	private final ViewStateLoaderHandler viewStateLoaderHandler;
	private final FSMActivityIntegration fsmActivity;

	public FSMIntentReceiver(FSMActivityIntegration fsmActivity, ViewStateLoaderHandler viewStateLoaderHandler) {
		super();
		this.fsmActivity = fsmActivity;
		this.viewStateLoaderHandler = viewStateLoaderHandler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		ContextInstance newConfig = (ContextInstance) intent.getSerializableExtra(FSM_EXTRAS.CONTENT.toString());
		if(newConfig!=null){
			this.fsmActivity.onNewStateConfig(newConfig);
		}

		this.viewStateLoaderHandler.onActionIntent(intent);

	}

}
