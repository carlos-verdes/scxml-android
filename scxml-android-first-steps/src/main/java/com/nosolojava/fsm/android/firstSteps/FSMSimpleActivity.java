package com.nosolojava.fsm.android.firstSteps;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nosolojava.android.fsm.view.impl.BasicFSMActivity;
import com.nosolojava.fsm.android.firstSteps.service.FirstStepsFSMService;

/**
 * Activity which extends AbstractFSMActivity
 * 
 * @author cverdes
 *
 */
public class FSMSimpleActivity extends BasicFSMActivity {

	private static final String OFF_STATE = "off-state";
	private static final String CONNECTED_STATE = "connected-state";
	private static final String DISCONNECTED_STATE = "disconnected-state";
	private static final String SESSION_ID = "first-steps-main-session";

	// the scxml resource uri, the fragment (# part) contains the desired
	// session id
	private static final Uri FSM_URI = Uri.parse("android.resource://com.nosolojava.fsm.android.firstSteps/raw/fsm#"
			+ SESSION_ID);
	
	
	public FSMSimpleActivity() {
		super(FirstStepsFSMService.class, R.layout.loading_layout, FSM_URI);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup view-state map
		associateStateView(R.layout.login_layout, null, DISCONNECTED_STATE);
		associateStateView(R.layout.activity_main, null, CONNECTED_STATE);
		Runnable offStateCallback = new Runnable() {

			public void run() {
				Button startButton = (Button) FSMSimpleActivity.this.findViewById(R.id.start_button);
				startButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						FSMSimpleActivity.this.fsmHelper.sendInitFSMIntent();
					}
				});

			}
		};
		associateStateView(R.layout.off_layout, offStateCallback, OFF_STATE);


	}

}
