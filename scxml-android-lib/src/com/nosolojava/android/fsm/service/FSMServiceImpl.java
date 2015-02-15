package com.nosolojava.android.fsm.service;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.nosolojava.android.fsm.invokeHandler.AndroidServiceInvokeHandler;
import com.nosolojava.android.fsm.io.AndroidActivityIOProcessor;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.listener.AndroidFSMListener;
import com.nosolojava.android.fsm.parser.AndroidXppStateMachineParser;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.android.fsm.util.MetadataUtil;
import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.ContextInstance;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.EventType;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;
import com.nosolojava.fsm.runtime.listener.FSMListener;

public class FSMServiceImpl extends Service {
	// private static final String VIEW_INIT = "view.init";
	private static final String SYSTEM_FSM_ON_LOW_MEMORY = "system.fsm.onLowMemory";
	private static final String SYSTEM_FSM_ON_DESTROY = "system.fsm.onDestroy";
	public static final String FSM = "FSM";
	private static final String FSM_ACTION_PARSER_PREFIX = "fsmActionParser_";
	private static final String FSM_IO_PROCESSOR_PARSER_PREFIX = "fsmIOProcessor_";
	private static final String FSM_INVOKE_HANDLER_PARSER_PREFIX = "fsmInvokeHandler_";
	private static final String SLASH = "/";

	private StateMachineEngine engine = null;
	private AndroidFSMListener defaultListener;
	private AndroidBroadcastIOProcessor androidBroadcastIO;

	public FSMServiceImpl() {
		super();

	}

	@Override
	public void onCreate() {

		super.onCreate();

		initEngine();

	}

	@Override
	public void onDestroy() {
		stopEngine();
		super.onDestroy();
	}

	protected void initEngine() {
		try {

			Log.i(FSM, "Creating FSM service");

			BasicStateMachineFramework.DEBUG.set(true);

			List<XppActionParser> actionParsers = this.getActionParsers();
			this.engine = new BasicStateMachineEngine(actionParsers, new AndroidLogCallback());

			AndroidServiceInvokeHandler androidServiceInvokeHandler = new AndroidServiceInvokeHandler(this, this.engine);

			List<IOProcessor> ioProcessors = this.getIOProcessors();
			this.androidBroadcastIO = new AndroidBroadcastIOProcessor(this, this);
			ioProcessors.add(this.androidBroadcastIO);
			ioProcessors.add(new AndroidActivityIOProcessor(this, this));
			ioProcessors.add(androidServiceInvokeHandler);

			List<InvokeHandler> invokeHandlers = this.getInvokeHandlers();
			invokeHandlers.add(androidServiceInvokeHandler);

			// set the android parser
			this.engine.setParser(new AndroidXppStateMachineParser(this));

			for (IOProcessor ioProcessor : ioProcessors) {
				this.engine.registerIOProcessor(ioProcessor);
			}

			for (InvokeHandler invokeHandler : invokeHandlers) {
				this.engine.registerInvokeHandler(invokeHandler);
			}

			List<FSMListener> listeners = this.createListeners();
			for (FSMListener listener : listeners) {
				this.engine.getStateMachineFramework().registerListener(listener);
			}

			this.engine.start();

		} catch (Exception e) {
			Log.e(FSM, "Error initiating FSM", e);
			// throw new RuntimeException("Error initiating FSM", e);
		}
	}

	protected void stopEngine() {
		if (engine != null) {

			pushEventToAllSessions(SYSTEM_FSM_ON_DESTROY);

			try {
				boolean shutdown = engine.shutdownAndWait(100, TimeUnit.MILLISECONDS);
				// only save state if the shutdown has been succesfully
				if (shutdown) {

				}
			} catch (InterruptedException e) {
				// TODO manage error shutting down fsm
				Log.e(FSM, "Error stopping fsm engine.", e);
			}
		}
	}

	protected int getDefaultXML() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		int xmlId;
		Class<?> clazz = MetadataUtil.classFromName(".R$xml", this);
		Log.d(FSM, "class: " + clazz);
		Field field = clazz.getField("fsm");
		Log.d(FSM, "field: " + field);

		xmlId = field.getInt(null);
		Log.d(FSM, "default xmlId: " + xmlId);
		return xmlId;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);

		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {

		handleIntent(intent);
	}

	protected BasicEvent createEventFromIntent(Intent intent) {
		Object data = null;

		String eventName = getEventNameFromIntent(intent);

		// received data
		if (intent.hasExtra(FSM_EXTRAS.CONTENT.toString())) {
			data = intent.getExtras().get(FSM_EXTRAS.CONTENT.toString());
		}

		BasicEvent event = new BasicEvent(eventName, EventType.EXTERNAL, "", null, "", "", data);
		return event;
	}

	public void handleIntent(Intent intent) {
		// if push event intent
		if (intent != null) {
			String action = intent.getAction();
			//
			// handle events to FSM fsm://sessionId/event
			//
			if (intentIsEventToFSM(intent)) {

				// get session id
				String sessionId = getEventSessionIdFromIntent(intent);

				// transform intent into FSM event
				BasicEvent event = createEventFromIntent(intent);

				// if session is informed --> send to this session
				if (sessionId != null) {
					if (this.engine.isSessionActive(sessionId)) {
						Log.d(FSM, String.format("Send intent %s to session %s", intent, sessionId));
						this.engine.pushEvent(sessionId, event);
					} else {

						// save missing event to relaunch on next macrostep
						Log.w(FSM, String.format(
								"Can't send intent to session %s, (session not found), saving on missing intents",
								sessionId));
						defaultListener.addMissingIntent(sessionId, intent);
					}

				} else {
					// send to all active sessions
					Collection<com.nosolojava.fsm.runtime.Context> activeContexts = engine.getActiveSessions();
					for (com.nosolojava.fsm.runtime.Context activeContext : activeContexts) {
						this.engine.pushEvent(activeContext.getSessionId(), event);
					}

				}

			}
			//
			// start fsm session schema://pathToSessionResource#sessionId
			// example:
			// android.resource://com.nosolojava.android.fsm.firstSteps/raw/fsm#testingSessionId
			//

			else if (intentIsInitSessionEvent(intent)) {

				// sessionId from fragment (if any)
				String sessionId = getIntentSessionIdForInitSessionEvent(intent);

				// if engine is not already started
				if (!engine.isSessionActive(sessionId)) {
					Log.i(FSM, "Starting FSM session: " + intent);

					// start fsm
					starStateMachineAsync(sessionId, intent.getData().toString());
				} else {
					// send confirmation to the caller
					Context context = engine.getSession(sessionId);
					sendSessionInitiatedBroadcast(context.getLastStableConfiguration());
				}
			}
			//
			// handle updates in assignments
			// fsm session comes from intent extras
			// example
			// intent.getExtras().getString(FSM_EXTRAS.SESSION_ID.toString());
			//
			// else if (FSM_ACTIONS.INIT_FSM_ASSIGN.toString().equals(action)) {
			//
			// // if the session is active
			// String fsmSession = getSessionIdFromIntent(intent);
			// if (engine.isSessionActive(fsmSession)) {
			// // send last config
			// Context context = engine.getSession(fsmSession);
			// sendLastFSMSessionConfigBroadcast(context.getLastStableConfiguration());
			//
			// } else {
			// Log.w(FSM,
			// String.format(
			// "Can't send init assign event to session %s, (session not found), saving on missing intents",
			// fsmSession));
			// defaultListener.addMissingIntent(fsmSession, intent);
			//
			// }
			//
			// }
			else {
				Log.d(FSM, "Event action not recognize, action: " + action);
			}
		}
	}

	protected boolean intentIsInitSessionEvent(Intent intent) {
		return intent.getData() != null && FSM_ACTIONS.INIT_FSM_SESSION.toString().equals(intent.getAction());
	}

	protected String getEventSessionIdFromIntent(Intent intent) {
		// fsm://sessionId/eventName
		return intent.getData().getHost();
	}

	protected String getEventNameFromIntent(Intent intent) {
		// fsm://sessionId/eventName
		return intent.getData().getPath().substring(intent.getData().getPath().lastIndexOf(SLASH) + 1);
	}

	protected String getIntentSessionIdForInitSessionEvent(Intent intent) {
		// schema://host/path#sessionId
		String sessionId = intent.getData().getFragment();
		return sessionId;
	}

	protected boolean intentIsEventToFSM(Intent intent) {
		return intent.getData() != null && FSM_ACTIONS.SEND_EVENT_TO_FSM.toString().equals(intent.getAction());
	}

	final protected void starStateMachineAsync(String sessionId, String uri) {
		AsyncTask<String, Void, Boolean> startFsmTask = new AsyncTask<String, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(String... params) {
				Boolean result = Boolean.valueOf(false);
				String sessionId = params[0];
				String uriString = params[1];
				URI uri = URI.create(uriString);
				try {
					Context context;

					if (sessionId != null && !sessionId.equals("")) {
						context = engine.startFSMSession(sessionId, null, uri, null);
					} else {
						context = engine.startFSMSession(uri);
					}

					result = Boolean.valueOf(true);
					Log.i(FSM, String.format("FSM session started with sessionId %s", sessionId));
					Log.i(FSM, "Sending initiated intent.");

					sendSessionInitiatedBroadcast(context.getLastStableConfiguration());
				} catch (Exception e) {
					// TODO manage start fsm errors
					Log.e(FSM, "Error initiating fsm, uri: " + uri, e);
				}

				return result;
			}

		};
		startFsmTask.execute(sessionId, uri);

	}

	public void sendSessionInitiatedBroadcast(ContextInstance contextInstance) {
		String sessionId = contextInstance.getSessionId();
		this.androidBroadcastIO.sendBroadcastFromFSM(sessionId, FSM_ACTIONS.FSM_SESSION_INITIATED, contextInstance);
	}

	public void sendSessionEndBroadcast(ContextInstance contextInstance) {

		String sessionId = contextInstance.getSessionId();
		this.androidBroadcastIO.sendBroadcastFromFSM(sessionId, FSM_ACTIONS.FSM_SESSION_ENDED, contextInstance);
	}

	public void sendLastFSMSessionConfigBroadcast(ContextInstance contextInstance) {
		this.androidBroadcastIO.sendBroadcastFromFSM(contextInstance.getSessionId(),
				FSM_ACTIONS.FSM_NEW_SESSION_CONFIG, contextInstance);

	}

	protected String getSessionIdFromIntent(Intent intent) {
		Uri sessionUri = intent.getData();
		String sessionId = AndroidUtils.getFSMSessionFromUri(sessionUri);
		return intent.getExtras().getString(sessionId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected List<FSMListener> createListeners() {
		List<FSMListener> listeners = new ArrayList<FSMListener>();
		defaultListener = new AndroidFSMListener(this);
		listeners.add(defaultListener);
		return listeners;
	}

	protected void pushEventToAllSessions(String eventName) {
		Event systemDestroyEvent = new BasicEvent(eventName);
		for (Context context : engine.getActiveSessions()) {
			engine.pushEvent(context.getSessionId(), systemDestroyEvent);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onLowMemory() {
		pushEventToAllSessions(SYSTEM_FSM_ON_LOW_MEMORY);
		super.onLowMemory();
	}

	private List<XppActionParser> getActionParsers() throws Exception {
		List<XppActionParser> parsers = MetadataUtil.instantiateObjects(FSM_ACTION_PARSER_PREFIX, this);

		return parsers;

	}

	protected List<IOProcessor> getIOProcessors() throws Exception {
		List<IOProcessor> ioProcessors = MetadataUtil.instantiateObjects(FSM_IO_PROCESSOR_PARSER_PREFIX, this);

		return ioProcessors;
	}

	private List<InvokeHandler> getInvokeHandlers() throws Exception {

		List<InvokeHandler> invokeHandlers = MetadataUtil.instantiateObjects(FSM_INVOKE_HANDLER_PARSER_PREFIX, this);

		return invokeHandlers;
	}

}
