package com.nosolojava.fsm.android.firstSteps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;
import com.nosolojava.fsm.android.firstSteps.FSMSimpleActivity;

public class FSMIntentReceiver extends BroadcastReceiver {

	//view loader
	private final ViewStateLoaderHandler viewStateLoaderHandler;

	
	public FSMIntentReceiver(FSMSimpleActivity fsmSimpleActivity, ViewStateLoaderHandler viewStateLoaderHandler) {
		super();
		this.viewStateLoaderHandler = viewStateLoaderHandler;
	}





	@Override
	public void onReceive(Context context, Intent intent) {

		//manage state-views
		viewStateLoaderHandler.onActionIntent(intent);

	}

}
