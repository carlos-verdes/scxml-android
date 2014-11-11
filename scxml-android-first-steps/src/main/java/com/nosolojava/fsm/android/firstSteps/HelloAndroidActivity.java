package com.nosolojava.fsm.android.firstSteps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;

public class HelloAndroidActivity extends Activity {

	private static final String SESSION_ID = "pickYouApp-session";
	// the scxml resource uri, the fragment (# part) contains the desired
	// session id
	private static final Uri FSM_URI = Uri
			.parse("android.resource://com.nosolojava.fsm.android.firstSteps/raw/fsm#"
					+ SESSION_ID);
	private Uri scxmlUri=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BasicStateMachineFramework.DEBUG.set(true);
		this.setContentView(R.layout.loading_layout);

		// init a broadcast receiver for the FSM
		this.registerReceiver(this.FSM_RECEIVER, HelloAndroidActivity.FSM_INTENT_FILTER);

		// init intent to FSM service
		Intent fsmIntentService = new Intent(this, FSMServiceImpl.class);
		// set action "init FSM" and data the uri of the scxml resource
		fsmIntentService.setAction(FSM_ACTIONS.INIT_FSM_SESSION.toString());
		fsmIntentService.setData(FSM_URI);
		this.startService(fsmIntentService);

	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		this.unregisterReceiver(this.FSM_RECEIVER);
	}


	private static final IntentFilter FSM_INTENT_FILTER = new IntentFilter();
	static {
		FSM_INTENT_FILTER.addAction(FSM_ACTIONS.FSM_SESSION_INITIATED.toString());
	}

	private final BroadcastReceiver FSM_RECEIVER = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent != null && intent.getAction() != null ? intent
					.getAction() : null;
			if (action != null) {
				// when session is initiated
				if (FSM_ACTIONS.FSM_SESSION_INITIATED.toString().equals(action)) {
					// show desired layout
					HelloAndroidActivity.this
							.setContentView(R.layout.activity_main);
//					HelloAndroidActivity.this.scxmlUri=intent.getData();
				}

			}
		}
	};

}
