package com.nosolojava.android.fsm.io.intent.toFSM;

import android.content.Context;
import android.net.Uri;

import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;

public class StartFSMSessionEventIntent extends ToFSMEventIntent {

	public StartFSMSessionEventIntent(String fsmSessionId, Uri scxmlUri, Context packageContext,
			Class<? extends FSMServiceImpl> fsmServiceClass) {
		super(fsmSessionId, packageContext, fsmServiceClass);

		this.putExtra(FSM_EXTRAS.SCXML_URI.toString(), scxmlUri);
	}

	public Uri getSCXMLUri() {
		return this.getParcelableExtra(FSM_EXTRAS.SCXML_URI.toString());
	}

}
