package com.nosolojava.android.fsm.handlers.impl;

import android.content.Intent;

import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * Template class to intercept new configuration intents from FSM. Just implement abstract method
 * {@link AbstractFSMIntentHandler#onActionIntentInternal(Intent, ContextInstance)}
 * 
 * @author cverdes
 *
 * @param <ContextInstance>
 */
public abstract class NewFSMConfigurationHandler extends AbstractFSMIntentHandler<ContextInstance> {

	private final static FSM_ACTIONS[] VALID_ACTIONS = new FSM_ACTIONS[] { FSM_ACTIONS.FSM_NEW_SESSION_CONFIG,
			FSM_ACTIONS.FSM_SESSION_INITIATED };

	@Override
	public boolean passActionFilter(Intent intentAction) {
		String action = intentAction.getAction();

		boolean result = false;

		for (FSM_ACTIONS validAction : VALID_ACTIONS) {
			result = result || validAction.toString().equals(action);
		}

		return result;
	}

}
