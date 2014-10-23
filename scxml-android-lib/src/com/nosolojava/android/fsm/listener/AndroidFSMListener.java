package com.nosolojava.android.fsm.listener;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Intent;
import android.util.Log;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.FSMListener;

/**
 * When a macrostep finish it sends to the view the current state config and manage if any missing intent has to be sent again to the {@link FSMServiceImpl}.
 * <br/> The last is needed because the start fsm service is async and the view sends some intents on init to update initial view (so, if the view event is sent before the fsm is started the intent is lost).
 * @author Carlos Verdes
 *
 */
public class AndroidFSMListener implements FSMListener {

	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Intent>> missingIntentsMap = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Intent>>();

	private final FSMServiceImpl fsmService;

	public AndroidFSMListener(FSMServiceImpl fsmService) {
		super();
		this.fsmService = fsmService;
	}

	@Override
	public void onMacroStepFinished(Context context) {

		Log.d(FSMServiceImpl.FSM, "onMacroStepFinish, sending active states intent.");
		Intent intent = new Intent(FSM_ACTIONS.FSM_ACTIVE_STATES.toString());
		ArrayList<String> activeStateNames = getActiveStateNames(context);
		Log.d(FSMServiceImpl.FSM, String.format("onMacroStepFinish, active states: %s ", activeStateNames));
		intent.putExtra(FSM_EXTRAS.CONTENT.toString(), activeStateNames);

		AndroidBroadcastIOProcessor.sendIntentToTheView(context, intent);

		//manage missing intents

		//for each missing intent
		String sessionId = context.getSessionId();
		Intent missingIntent;
		if (this.missingIntentsMap.containsKey(sessionId)) {
			ConcurrentLinkedQueue<Intent> missingQueue = this.missingIntentsMap.get(sessionId);

			while (!missingQueue.isEmpty()) {
				Log.d(FSMServiceImpl.FSM,
						String.format("onMacroStepFinish, missing intents for sessionId %s: %s", sessionId, missingQueue));

				//relaunch intent
				missingIntent = missingQueue.poll();
				if (missingIntent != null) {
					Log.d(FSMServiceImpl.FSM, String.format(
							"onMacroStepFinish, handle missing intent %s for sessionId %s", missingIntent, sessionId));
					this.fsmService.handleIntent(missingIntent);
				}
			}

		}

	}

	public void addMissingIntent(String sessionId, Intent intent) {

		this.missingIntentsMap.putIfAbsent(sessionId, new ConcurrentLinkedQueue<Intent>());
		ConcurrentLinkedQueue<Intent> missingQueue = this.missingIntentsMap.get(sessionId);
		missingQueue.add(intent);
	}

	public ArrayList<String> getActiveStateNames(Context context) {
		ArrayList<String> activeStatesStringList = new ArrayList<String>();
		SortedSet<State> activeStates = context.getActiveStates();
		for (State aux : activeStates) {
			activeStatesStringList.add(aux.getName());
		}

		return activeStatesStringList;
	}

}
