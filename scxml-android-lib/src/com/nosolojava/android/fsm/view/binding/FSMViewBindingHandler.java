package com.nosolojava.android.fsm.view.binding;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;

import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * Manage the binding between the FSM and the View in Android. It will be used when the activity handler is parsing the
 * view to register all the bindings and when an event is received from FSM to handle it.
 * 
 * @author Carlos Verdes
 * 
 * @param <T>
 *            type of view that is managed by this handler
 */
public interface FSMViewBindingHandler {

	/**
	 * Reset method, called before starting the binding register.
	 */
	public void reset();


	/**
	 * This method will be called after the activity is attached to FSM.
	 */
	public void onBind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId);

	/**
	 * This method will be called before the activity is detached from FSM.
	 */
	public void onUnbind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId);

	/**
	 * When an event is received by the activity (from a send FSM action) this method is called.
	 * 
	 * @param intent
	 *            FSM intent sent
	 * @return true if this handler has been used, false otherwise
	 */
	public boolean handleFSMIntent(Intent intent);
	
	/**
	 * Updates the view with the new config from FSM
	 * @param newContextInstance
	 */
	public void updateView(ContextInstance newContextInstance);

}