package com.nosolojava.android.fsm.io.intent.toFSM;

import android.content.Context;

import com.nosolojava.android.fsm.io.intent.BasicFSMIntent;
import com.nosolojava.android.fsm.service.FSMServiceImpl;

public class ToFSMEventIntent extends BasicFSMIntent {

	public ToFSMEventIntent(String fsmSessionId, Context packageContext, Class<? extends FSMServiceImpl> fsmServiceClass) {
		super(fsmSessionId, packageContext, fsmServiceClass);
	}



}
