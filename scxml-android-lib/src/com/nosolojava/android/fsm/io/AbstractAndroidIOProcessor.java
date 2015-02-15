package com.nosolojava.android.fsm.io;

import java.net.URI;

import android.content.Intent;
import android.net.Uri;

import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

/**
 * <p>
 * Base class to send intents from FSM sessions.
 * 
 * @author cverdes
 *
 */
public abstract class AbstractAndroidIOProcessor implements IOProcessor {

	protected static final String LOG_TAG = "FSM";
	public static final String FSM_SCHEME = "fsm";

	// fsm session template fsm://{sessionId}
	protected static final String FSM_SESSION_TEMPLATE = FSM_SCHEME + "://%s";

	// fsm session event template fsm://{sessionId}/{event}
	protected static final String MESSAGE_TO_FSM_TEMPLATE = FSM_SCHEME + "://%s/%s";
	private StateMachineEngine engine;

	// abstract methods
	protected abstract void sendIntent(Intent intent);

	@Override
	public URI getLocation(String sessionId) {
		return getLocationStatic(sessionId);
	}

	public static URI getLocationStatic(String sessionId) {
		URI uri = URI.create(String.format(FSM_SESSION_TEMPLATE, sessionId));
		return uri;
	}

	public static Uri getAndroidLocationStatic(String sessionId) {
		Uri uri = Uri.parse(String.format(FSM_SESSION_TEMPLATE, sessionId));
		return uri;
	}

	@Override
	public void sendMessageFromFSM(Message message) {

		if (message != null) {
			Intent intent = createIntentFromMessage(message);
			sendIntent(intent);
		}

	}

	protected Intent createIntentFromMessage(Message message) {

		Intent intent = new Intent();

		// <send type="broadcast" event="your.intent.ACTION" target="dataUrl">
		// event is the action
		// data is the source session uri so receivers could filter
		// target will be pased as an extra

		// target is an extra
		if (message.getTarget() != null) {
			loadTarget(message.getTarget(), intent);
		}

		// event is the action
		if (message.getName() != null) {
			loadEvent(message.getName(), intent);
		}

		// the body is the extra info
		Object body = message.getBody();
		AndroidUtils.addContentToIntent(intent, body);

		// add the source uri so the receiver could answer the fsm
		intent.setData(Uri.parse(message.getSource().toString()));

		return intent;
	}

	protected void loadEvent(String event, Intent intent) {
		intent.setAction(event);
	}

	protected void loadTarget(URI target, Intent intent) {
		intent.putExtra(FSM_EXTRAS.TARGET_URI.toString(), Uri.parse(target.toString()));
	}

	@Override
	public void sendEventToFSM(String sessionId, Event event) {
		if (this.engine.isSessionActive(sessionId)) {
			this.engine.pushEvent(sessionId, event);
		}

	}

	@Override
	public void setEngine(StateMachineEngine engine) {
		this.engine = engine;

	}

}
