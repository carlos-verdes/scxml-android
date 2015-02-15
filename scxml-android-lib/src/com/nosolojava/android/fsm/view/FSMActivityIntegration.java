package com.nosolojava.android.fsm.view;

import java.io.Serializable;

import android.os.Parcelable;

import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * <p>
 * This interface is implemented by front classes (Activity, Fragment, FragmentActivity) that are
 * integrated with a FSM session.
 * 
 * @author cverdes
 *
 */
public interface FSMActivityIntegration {

	void registerFSMViewBindingHandler(XPPFSMViewBindingHandler handler);
	boolean unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler);

	/**
	 * This method associates a view with a set of states, so when one of this states is active the activity changes
	 * it's current view.
	 * 
	 * @param view
	 *            viewId to load
	 * @param callback
	 *            callback to init the view (optional)
	 * @param states
	 *            state/s associated with this view
	 */
	void associateStateView(Integer view, Runnable callback, String... states);

	void pushEventToFSM(String eventName);

	void pushEventToFSM(String eventName, Serializable data);

	void pushEventToFSM(String eventName, Parcelable data);

	void onNewStateConfig(ContextInstance newConfig);

}