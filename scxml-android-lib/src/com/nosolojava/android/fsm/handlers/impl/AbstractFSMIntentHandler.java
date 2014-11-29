package com.nosolojava.android.fsm.handlers.impl;

import com.nosolojava.android.fsm.handlers.FSMIntentHandler;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;

import android.content.Intent;
import android.os.Bundle;

public abstract class AbstractFSMIntentHandler<T> implements FSMIntentHandler {

	public abstract boolean passActionFilter(Intent action);

	protected abstract void onActionIntentInternal(Intent action, T infoT);

	@Override
	public void onActionIntent(Intent action) {

		if (action != null && passActionFilter(action)) {
			Bundle extras = action.getExtras();
			Object info = null;
			if (extras != null && extras.containsKey(FSM_EXTRAS.CONTENT.toString())) {
				info = extras.get(FSM_EXTRAS.CONTENT.toString());

			}

			@SuppressWarnings("unchecked")
			T infoT = (T) info;

			onActionIntentInternal(action, infoT);

		}

	}

}
