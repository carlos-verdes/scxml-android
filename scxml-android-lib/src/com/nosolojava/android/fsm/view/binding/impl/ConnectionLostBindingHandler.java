package com.nosolojava.android.fsm.view.binding.impl;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.view.FSMActivityIntegration;

public class ConnectionLostBindingHandler extends AbstractFSMViewBindingHandler {

	private static final String TRY_AGAIN = "An error has ocurred, please try again";
	FSMActivityIntegration fsmActivity = null;

	@Override
	public void registerXMLAttributeBinding(View view, String bindingAttribute, String bindingValue) {
	}

	@Override
	public void registerXMLElementBinding(XmlPullParser xpp) {
	}

	@Override
	public void reset() {
	}

	@Override
	public void onInitActivity(Activity activity) {
		if (FSMActivityIntegration.class.isAssignableFrom(activity.getClass())) {
			this.fsmActivity = (FSMActivityIntegration) activity;
		} else {
			this.fsmActivity = null;
		}
	}

	@Override
	public boolean handleFSMIntent(Intent intent) {
		boolean result = false;
		if (this.fsmActivity != null && FSM_ACTIONS.FSM_SESSION_EXPIRED.toString().equals(intent.getAction())) {
			result = true;

			String sessionId = intent.getExtras().getString(FSM_EXTRAS.SESSION_ID.toString());
			//if the session is the same (should always be but...)
			if (this.fsmActivity.getSessionId().equals(sessionId)) {
				if (currentActivity != null) {
					Toast.makeText(currentActivity, TRY_AGAIN, Toast.LENGTH_LONG).show();
				}

				//init the fsm again
				this.fsmActivity.initFSMHandlers();

			}
		}

		return result;
	}

}
