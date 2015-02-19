package com.nosolojava.android.remotecam;

import android.net.Uri;
import android.os.Bundle;

import com.nosolojava.android.fsm.view.impl.BasicFSMActivity;
import com.nosolojava.android.remotecam.service.MainFSMService;

public class MainActivity extends BasicFSMActivity {
	private static final String DISCONNECTED_STATE = "disconnected-state";

	private static final Uri FSM_URI = Uri
			.parse("android.resource://com.nosolojava.android.remotecam/raw/fsm#remotecamSession");

	public MainActivity() {

		super(MainFSMService.class, R.layout.loading_layout, FSM_URI);
	}

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.associateStateView(R.layout.disconnected_layout, null, DISCONNECTED_STATE);
	}




}
