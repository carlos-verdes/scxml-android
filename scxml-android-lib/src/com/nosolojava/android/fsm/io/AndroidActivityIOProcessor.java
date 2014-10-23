package com.nosolojava.android.fsm.io;

import android.app.Service;
import android.content.Intent;

import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class AndroidActivityIOProcessor extends AndroidBroadcastIOProcessor {

	public static final String NAME = "activity";

	public AndroidActivityIOProcessor(Service androidService) {
		super(androidService);
	}

	public AndroidActivityIOProcessor(Service androidService, StateMachineEngine engine) {
		super(androidService, engine);
	}

	@Override
	public void sendMessage(Message message) {
 
		if (message != null) {
			Intent intent = createIntentFromMessage(message);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.androidService.startActivity(intent);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

}
