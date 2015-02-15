package com.nosolojava.fsm.android.firstSteps;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nosolojava.android.fsm.handlers.impl.ViewStateLoaderHandler;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.fsm.android.firstSteps.receiver.FSMIntentReceiver;
import com.nosolojava.fsm.android.firstSteps.service.FirstStepsFSMService;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;

/**
 * Activity which uses FSM engine but doesn't extends another class (not forcing to extend framework classes).
 * Only useful to understand full framework and when is not possible to extend utility classes. 
 * @author cverdes
 *
 */
public class FSMCustomFSMActivity extends Activity {

	private static final String OFF_STATE = "off-state";
	private static final String CONNECTED_STATE = "connected-state";
	private static final String DISCONNECTED_STATE = "disconnected-state";
	private static final String SESSION_ID = "first-steps-main-session";
	// the scxml resource uri, the fragment (# part) contains the desired
	// session id
	private static final Uri FSM_URI = Uri
			.parse("android.resource://com.nosolojava.fsm.android.firstSteps/raw/fsm#"
					+ SESSION_ID);
	private Uri scxmlUri=null;
	private FSMIntentReceiver receiver;
	private IntentFilter intentFilter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BasicStateMachineFramework.DEBUG.set(true);
		this.setContentView(R.layout.loading_layout);

		//setup view-state map
		ViewStateLoaderHandler viewStateLoaderHandler = new ViewStateLoaderHandler(this,FirstStepsFSMService.class);
		viewStateLoaderHandler.registerStateView(R.layout.login_layout, null,DISCONNECTED_STATE);
		viewStateLoaderHandler.registerStateView(R.layout.activity_main,null, CONNECTED_STATE);
		Runnable offStateCallback= new Runnable() {
			
			public void run() {
				Button startButton= (Button) FSMCustomFSMActivity.this.findViewById(R.id.start_button);
				startButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						startFSM();
					}
				});
				
			}
		};
		viewStateLoaderHandler.registerStateView(R.layout.off_layout, offStateCallback,OFF_STATE);
		

		//init intent filter
		intentFilter= new IntentFilter();
		
		intentFilter.addAction(FSM_ACTIONS.FSM_SESSION_INITIATED.toString());
		intentFilter.addAction(FSM_ACTIONS.FSM_SESSION_ENDED.toString());
		intentFilter.addAction(FSM_ACTIONS.FSM_NEW_SESSION_CONFIG.toString());
		
		
		intentFilter.addDataScheme("fsm");
		intentFilter.addDataAuthority(SESSION_ID, null);

		
		//init receiver
		receiver= new FSMIntentReceiver(viewStateLoaderHandler);

	}


	@Override
	protected void onResume() {
		super.onResume();

		this.registerReceiver(receiver, intentFilter);

		startFSM();
	}


	protected void startFSM() {
		// init intent to FSM service
		Intent fsmIntentService = new Intent(this, FirstStepsFSMService.class);
		// set action "init FSM" and data the uri of the scxml resource
		fsmIntentService.setAction(FSM_ACTIONS.INIT_FSM_SESSION.toString());
		fsmIntentService.setData(FSM_URI);
		this.startService(fsmIntentService);
	}


	@Override
	protected void onPause() {
		super.onPause();
		
		this.unregisterReceiver(receiver);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}




	public Uri getScxmlUri() {
		return scxmlUri;
	}




	public void setScxmlUri(Uri scxmlUri) {
		this.scxmlUri = scxmlUri;
	}


}
