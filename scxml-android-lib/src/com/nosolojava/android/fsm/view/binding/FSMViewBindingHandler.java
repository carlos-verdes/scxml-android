package com.nosolojava.android.fsm.view.binding;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.ContextInstance;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

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
	 * Used to identify which type of view manage this handler
	 * 
	 * @return view class that this handler is able to manage
	 */
	Class<? extends View> getViewClass();

	/**
	 * Reset method, called before starting the binding register.
	 */
	public void reset();

	/**
	 * This method will be called when the activity is restarted. This is useful to send init event to the FSM like
	 * "tell me in which state are we now" event.
	 * 
	 * @param activity
	 */
	public void onInitActivity(Activity activity);

	/**
	 * This method will be called after the activity is attached to FSM.
	 */
	public void onBind(Activity activity, String fsmSessionId);

	/**
	 * This method will be called before the activity is detached from FSM.
	 */
	public void onUnbind(Activity activity, String fsmSessionId);

	/**
	 * Register a data binding associated to a view.
	 * 
	 * @param view
	 *            view to be bound
	 * @param dataBinding
	 *            value used to do the binding
	 */
	public <T extends View> void registerViewBinding(T view, Serializable dataBinding);

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