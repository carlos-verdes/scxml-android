package com.nosolojava.android.fsm.view.binding.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;

/**
 * <p>
 * This handler register views associated with states.
 * 
 * <p>
 * When the FSM transitions to a new configuration sends an event that is managed by this handler showing/hiding the
 * associated views.
 * <p>
 * On init sends an event to the FSM (view.statesHandler.init) to get the current configuration of the FSM (the active
 * states).
 * 
 * @author Carlos Verdes
 * 
 */
public class OnNewStateBindingHandler extends AbstractFSMViewBindingHandler {

	private CopyOnWriteArraySet<View> beingShowedViewList = new CopyOnWriteArraySet<View>();
	private Map<String, Set<View>> showInStateViewsMap = new HashMap<String, Set<View>>();

	public static final String STATE_ATTRIBUTE = "state";

	@Override
	public void reset() {
		this.beingShowedViewList.clear();
		this.showInStateViewsMap.clear();
	}

	public <T extends View> void registerViewBinding(T view, String state) {

		Set<View> views;
		if (this.showInStateViewsMap.containsKey(state)) {
			views = this.showInStateViewsMap.get(state);
		} else {
			views = new HashSet<View>();
		}

		views.add(view);
		beingShowedViewList.add(view);
		showInStateViewsMap.put(state, views);
	}

	@Override
	public boolean handleFSMIntent(Intent intent) {

		boolean handled = false;
		if (FSM_ACTIONS.FSM_ACTIVE_STATES.toString().equals(intent.getAction())) {
			handled = true;

			@SuppressWarnings("unchecked")
			ArrayList<String> activeStates = (ArrayList<String>) intent.getExtras().get(FSM_EXTRAS.CONTENT.toString());

			Log.d(FSMServiceImpl.FSM, "New state binding received, active states: " + activeStates);

			// hide all the views
			ArrayList<View> activeViews = calculateActiveViews(activeStates);
			hideActiveViews(activeViews);

			//check which view should be shown
			showViews(activeViews);

		}

		return handled;
	}

	ArrayList<View> calculateActiveViews(ArrayList<String> activeStates) {
		ArrayList<View> result = new ArrayList<View>();
		for (String activeState : activeStates) {
			Set<View> showViews = showInStateViewsMap.get(activeState);
			if (showViews != null) {
				for (View view : showViews) {
					result.add(view);
				}
			}
		}

		return result;

	}

	protected void showViews(ArrayList<View> activeViews) {
		//for each active view
		for (View view : activeViews) {
			//if is now being showed
			if (view.getVisibility() != View.VISIBLE || !beingShowedViewList.contains(view)) {
				//show and add to showed list
				view.setVisibility(View.VISIBLE);
				beingShowedViewList.add(view);
			}

		}
	}

	protected void hideActiveViews(ArrayList<View> activeViews) {
		//for each being showed view
		for (View view : beingShowedViewList) {
			//if is not active
			if (!activeViews.contains(view)) {
				//hide view and remove from being showed list
				view.setVisibility(View.GONE);
				beingShowedViewList.remove(view);
			}

		}
	}

	@Override
	public void registerXMLAttributeBinding(View view, String bindingAttribute, String bindingValue) {
		if (STATE_ATTRIBUTE.equals(bindingAttribute)) {

			String[] states = bindingValue.split(",");
			for (String state : states) {
				registerViewBinding(view, state);

			}

		}
	}

	@Override
	public void registerXMLElementBinding(XmlPullParser xpp) {
	}

	@Override
	public void onInitActivity(Activity activity) {

	}

}
