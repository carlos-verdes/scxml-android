package com.nosolojava.android.fsm.view.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;

public class ActivityFSMIntentReceiver extends BroadcastReceiver {

	private final ViewStateLoaderHandler viewStateLoaderHandler;

	
	public ActivityFSMIntentReceiver(ViewStateLoaderHandler viewStateLoaderHandler) {
		super();
		this.viewStateLoaderHandler = viewStateLoaderHandler;
	}





	@Override
	public void onReceive(Context context, Intent intent) {

		//manage state-views
		viewStateLoaderHandler.onActionIntent(intent);

	}

}
