package com.nosolojava.android.fsm.io;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.nosolojava.android.fsm.service.AndroidFSMContext;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class AndroidBroadcastIOProcessor implements IOProcessor {

	private static final String LOG_TAG = "FSM";

	public static final String SEND_EVENT_FSMACTION = "sendEvent";

	public static final String NAME = "broadcast";

	protected final Service androidService;

	public static final String FSM_SCHEME = "fsm";

	//fsm://sessionId/event
	private static final String MESSAGE_TO_FSM_TEMPLATE = FSM_SCHEME + "://{0}/{1}";
	protected static final ThreadLocal<MessageFormat> MESSAGE_TO_FSM_MF = new ThreadLocal<MessageFormat>() {

		@Override
		public MessageFormat initialValue() {
			return new MessageFormat(MESSAGE_TO_FSM_TEMPLATE);
		}

	};

	public AndroidBroadcastIOProcessor(Service androidService) {
		this(androidService, null);
	}

	public AndroidBroadcastIOProcessor(Service androidService, StateMachineEngine engine) {
		super();

		this.androidService = androidService;

		if (engine != null) {
			this.setEngine(engine);
		}

	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public URI getLocation(String sessionId) {
		return getLocationStatic(sessionId);
	}

	public static URI getLocationStatic(String sessionId) {
		URI uri = URI.create(MESSAGE_TO_FSM_MF.get().format(new Object[] { sessionId, "" }));
		return uri;

	}

	@Override
	public void sendMessage(Message message) {

		if (message != null) {
			Intent intent = createIntentFromMessage(message);
			this.androidService.sendBroadcast(intent);
		}

	}

	public Intent createIntentFromMessage(Message message) {

		Intent intent = new Intent();

		//<send type="broadcast" event="action:your.intent.ACTION" target="dataUrl">
		//<send type="broadcast" event="class:your.c" target="dataUrl">

		//target is the intent data
		if (message.getTarget() != null) {
			intent.setData(Uri.parse(message.getTarget().toString()));
		}

		// event is the action
		if (message.getName() != null) {

			try {
				URI messageUri = new URI(message.getName());

				if (AndroidUtils.ACTION_SCHEME.equals(messageUri.getScheme())) {
					intent.setAction(messageUri.getSchemeSpecificPart());
				} else if (AndroidUtils.CLASS_SCHEME.equals(messageUri.getScheme())) {
					intent.setClassName(this.androidService.getBaseContext(), messageUri.getSchemeSpecificPart());
				}
			} catch (URISyntaxException e) {
				Log.w(LOG_TAG,
						String.format(
								"The message name should be a URI (\"action:intent.action.YOUR_ACTION\" or \"class:your.component.Classname\"), will be passed to intent like an action. Message name: %s",
								message.getName()));
				// TODO review is a message event with no schema is considered valid
				intent.setAction(message.getName());

			}
		}

		//the body is the extra info
		Object body = message.getBody();
		AndroidUtils.addContentToIntent(intent, body);

		//add the source uri so the receiver could answer the fsm
		intent.putExtra(FSM_EXTRAS.SESSION_ID.toString(), message.getSource().getAuthority());
		intent.putExtra(FSM_EXTRAS.SOURCE_URI.toString(), message.getSource());

		return intent;
	}

	public static void sendMessageToFSM(Context androidContext, URI fsmURI, String eventName) {
		String sessionId = AndroidUtils.getFSMSessionFromUri(fsmURI);
		sendMessageToFSM(androidContext, sessionId, eventName);

	}

	public static void sendMessageToFSM(Context androidContext, URI fsmURI, String eventName, Serializable body) {
		String sessionId = AndroidUtils.getFSMSessionFromUri(fsmURI);
		sendMessageToFSM(androidContext, sessionId, eventName, body);
	}

	public static void sendMessageToFSM(Context androidContext, URI fsmURI, String eventName, Parcelable body) {
		String sessionId = AndroidUtils.getFSMSessionFromUri(fsmURI);
		sendMessageToFSM(androidContext, sessionId, eventName, body);
	}

	/**
	 * Send a message to a FSM session (an event).
	 * 
	 * @param sessionId
	 *            target session (if null the message will be sent to all active sessions)
	 * @param eventName
	 * @param body
	 */

	public static void sendMessageToFSM(Context androidContext, String sessionId, String eventName, Serializable body) {

		// fsm://sessionId/sendEvent/eventName
		Intent intent = initFSMintent(sessionId, eventName);
		AndroidUtils.addContentToIntent(intent, body);

		sendIntentToFSM(androidContext, intent);
	}

	public static void sendMessageToFSM(Context androidContext, String sessionId, String eventName) {
		Parcelable aux = null;
		sendMessageToFSM(androidContext, sessionId, eventName, aux);
	}

	public static void sendMessageToFSM(Context androidContext, String sessionId, String eventName, Parcelable body) {
		// fsm://sessionId/sendEvent/eventName

		Intent intent = initFSMintent(sessionId, eventName);
		AndroidUtils.addContentToIntent(intent, body);

		sendIntentToFSM(androidContext, intent);

	}

	protected static void sendIntentToFSM(Context androidContext, Intent intent) {
		androidContext.startService(intent);
	}

	protected static Intent initFSMintent(String sessionId, String eventName) {
		Intent intent = new Intent(FSM_ACTIONS.SEND_EVENT_TO_FSM.toString());

		sessionId = sessionId != null ? sessionId : "";
		Uri dataUri = Uri.parse(MESSAGE_TO_FSM_MF.get().format(new Object[] { sessionId, eventName }));
		intent.setData(dataUri);
		return intent;
	}

	public static void sendIntentToTheView(com.nosolojava.fsm.runtime.Context context, Intent intent) {
		AndroidFSMContext androidFSMContext = (AndroidFSMContext) context;
		android.content.Context androidContext = androidFSMContext.getAndroidContext();

		sendIntentToTheView(androidContext, context.getSessionId(), intent);
	}

	public static void sendIntentToTheView(Context androidContext, String sessionId, Intent intent) {

		// add session id
		intent.putExtra(FSM_EXTRAS.SESSION_ID.toString(), sessionId);
		intent.setData(Uri.parse(MESSAGE_TO_FSM_MF.get().format(new Object[] { sessionId, "" })));

		androidContext.sendBroadcast(intent);
	}

	@Override
	public void setEngine(StateMachineEngine engine) {
	}

}
