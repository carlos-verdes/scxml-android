package com.nosolojava.android.fsm.listener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Intent;
import android.util.Log;

import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.runtime.ContextInstance;
import com.nosolojava.fsm.runtime.listener.FSMListener;

/**
 * When a macrostep finish it sends to the view the current state config and manage if any missing intent has to be sent
 * again to the {@link FSMServiceImpl}. <br/>
 * The last is needed because the start fsm service is async and the view sends some intents on init to update initial
 * view (so, if the view event is sent before the fsm is started the intent is lost).
 * 
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
	public void onNewState(ContextInstance contextInstance) {

		//send last config broadcast
		Log.d(FSMServiceImpl.FSM, "onNewState, sending current context instance broadcast.");
		String sessionId = contextInstance.getSessionId();
		this.fsmService.sendLastFSMSessionConfigBroadcast(contextInstance);

		// manage missing intents
		Intent missingIntent;
		if (this.missingIntentsMap.containsKey(sessionId)) {
			ConcurrentLinkedQueue<Intent> missingQueue = this.missingIntentsMap.get(sessionId);

			while (!missingQueue.isEmpty()) {
				Log.d(FSMServiceImpl.FSM, String.format("onNewState, sending missing intents for sessionId %s: %s",
						sessionId, missingQueue));

				// relaunch intent
				missingIntent = missingQueue.poll();
				if (missingIntent != null) {
					Log.d(FSMServiceImpl.FSM, String.format("onNewState, handle missing intent %s for sessionId %s",
							missingIntent, sessionId));
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

	@Override
	public void onSessionStarted(ContextInstance contextInstance) {
		this.fsmService.sendSessionInitiatedBroadcast(contextInstance);

	}

	@Override
	public void onSessionEnd(ContextInstance contextInstance) {
		this.fsmService.sendSessionEndBroadcast(contextInstance);

	}

}
