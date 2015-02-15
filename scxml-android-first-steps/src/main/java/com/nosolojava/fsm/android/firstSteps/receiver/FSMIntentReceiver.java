package com.nosolojava.fsm.android.firstSteps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;

public class FSMIntentReceiver extends BroadcastReceiver {

	private final ViewStateLoaderHandler viewStateLoaderHandler;

	
	public FSMIntentReceiver(ViewStateLoaderHandler viewStateLoaderHandler) {
		super();
		this.viewStateLoaderHandler = viewStateLoaderHandler;
	}





	@Override
	public void onReceive(Context context, Intent intent) {

		//manage state-views
		viewStateLoaderHandler.onActionIntent(intent);

	}

}
