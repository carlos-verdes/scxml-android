package com.nosolojava.android.fsm.handlers.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;

import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;
import com.nosolojava.android.fsm.view.binding.impl.ViewBindingParser;
import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * Class which shows view based on states.
 * 
 * Register the state-view relationships and call
 * {@link AbstractFSMIntentHandler#onActionIntentInternal(Intent, ContextInstance)} on new intent inside your Activity
 * or Fragment.
 * 
 * @author cverdes
 *
 */
public class ViewStateLoaderHandler extends NewFSMConfigurationHandler {

	private final Activity androidActivity;
	private final Class<? extends Service> fsmServiceClazz;

	private ConcurrentHashMap<String, Integer> stateViewMap = new ConcurrentHashMap<String, Integer>();
	private ConcurrentHashMap<Integer, Runnable> viewCallableMap = new ConcurrentHashMap<Integer, Runnable>();
	private AtomicInteger lastViewId = new AtomicInteger(-1);

	private ViewBindingParser viewParser;

	public ViewStateLoaderHandler(Activity androidActivity, Class<? extends Service> fsmServiceClazz) {
		super();
		this.androidActivity = androidActivity;
		this.fsmServiceClazz = fsmServiceClazz;
		this.viewParser = new ViewBindingParser(this.fsmServiceClazz);

	}

	private void initBindingHandlers(Integer viewId, ContextInstance contextInstance) {
		// parse the view
		viewParser.parseXppViewBasedHandlers(this.androidActivity, viewId);

		// init all handlers
		for (XPPFSMViewBindingHandler handler : viewParser.getNosolojavaHandlers()) {
			handler.onBind(this.androidActivity, this.fsmServiceClazz, contextInstance.getSessionId());
		}
	}

	public void registerStateView(Integer view, Runnable callback, String... states) {
		for (String state : states) {
			stateViewMap.put(state, view);
		}

		if (callback != null) {
			viewCallableMap.put(view, callback);
		}

	}

	@Override
	protected void onActionIntentInternal(Intent action, ContextInstance contextInstance) {

		if (contextInstance != null) {
			List<String> states = contextInstance.getActiveStates();

			for (String stateName : states) {
				if (stateViewMap.containsKey(stateName)) {

					// load first match
					Integer viewId = stateViewMap.get(stateName);

					// avoid multiple calls with same config
					if (lastViewId.getAndSet(viewId) != viewId) {
						androidActivity.setContentView(viewId);

						// call on change callback
						if (this.viewCallableMap.containsKey(viewId)) {
							Runnable callback = this.viewCallableMap.get(viewId);
							callback.run();
						}

						// init handlers
						initBindingHandlers(viewId, contextInstance);

					}

					// update view
					updateView(contextInstance);

					break;
				}
			}

		}

	}

	private void updateView(ContextInstance contextInstance) {
		for (XPPFSMViewBindingHandler handler : this.viewParser.getNosolojavaHandlers()) {
			handler.updateView(contextInstance);
		}

	}

}
