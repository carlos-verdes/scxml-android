package com.nosolojava.android.fsm.handlers;

import android.content.Intent;

import com.nosolojava.android.fsm.handlers.impl.NewFSMConfigurationHandler;

/**
 * <p>
 * Interface which objective is to manage intents from FSM.
 * Some handy abstract classes are already implemented like a template for custom apps:
 * * <ul>
 * <li>{@link NewFSMConfigurationHandler}
 * @author cverdes
 *
 */
public interface FSMIntentHandler {

	void onActionIntent(Intent action);
}
