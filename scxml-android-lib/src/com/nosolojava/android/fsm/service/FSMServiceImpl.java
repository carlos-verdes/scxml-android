package com.nosolojava.android.fsm.service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.nosolojava.android.fsm.executable.AndroidAssign;
import com.nosolojava.android.fsm.invokeHandler.AndroidServiceInvokeHandler;
import com.nosolojava.android.fsm.io.AndroidActivityIOProcessor;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.listener.AndroidFSMListener;
import com.nosolojava.android.fsm.parser.AndroidXppStateMachineParser;
import com.nosolojava.android.fsm.util.MetadataUtil;
import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.EventType;
import com.nosolojava.fsm.runtime.FSMListener;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;

public class FSMServiceImpl extends Service {
	private static final String VIEW_INIT = "view.init";
	private static final String SYSTEM_FSM_ON_LOW_MEMORY = "system.fsm.onLowMemory";
	private static final String SYSTEM_FSM_ON_DESTROY = "system.fsm.onDestroy";
	public static final String FSM = "FSM";
	private static final String FSM_ACTION_PARSER_PREFIX = "fsmActionParser_";
	private static final String FSM_IO_PROCESSOR_PARSER_PREFIX = "fsmIOProcessor_";
	private static final String FSM_INVOKE_HANDLER_PARSER_PREFIX = "fsmInvokeHandler_";
	private static final String SLASH = "/";

	private StateMachineEngine engine = null;
	private AndroidFSMListener defaultListener;

	public FSMServiceImpl() {
		super();

	}

	@Override
	public void onCreate() {

		super.onCreate();

		try {

			Log.i(FSM, "Creating FSM service");

			BasicStateMachineFramework.DEBUG.set(true);

			List<XppActionParser> actionParsers = this.getActionParsers();
			this.engine = new BasicStateMachineEngine(actionParsers, new AndroidLogCallback());

			AndroidServiceInvokeHandler androidServiceInvokeHandler = new AndroidServiceInvokeHandler();

			List<IOProcessor> ioProcessors = this.getIOProcessors();
			ioProcessors.add(new AndroidBroadcastIOProcessor(this, this.engine));
			ioProcessors.add(new AndroidActivityIOProcessor(this, this.engine));
			ioProcessors.add(androidServiceInvokeHandler);

			List<InvokeHandler> invokeHandlers = this.getInvokeHandlers();
			invokeHandlers.add(androidServiceInvokeHandler);

			//set the android parser
			this.engine.setParser(new AndroidXppStateMachineParser(this));

			//set the context factory to include the android context
			this.engine.setContextFactory(new AndroidContextFactory(this));

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
			//			throw new RuntimeException("Error initiating FSM", e);
		}

	}

	@Override
	public void onDestroy() {
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
		super.onDestroy();
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);

		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {

		handleIntent(intent);
	}

	protected BasicEvent createEvent(Intent intent, String eventName) {
		Object data = null;

		//received data
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
			if (intent.getData() != null && FSM_ACTIONS.SEND_EVENT_TO_FSM.toString().equals(action)) {
				// data has the session id and event name
				Uri data = intent.getData();

				String eventName = data.getPath().substring(data.getPath().lastIndexOf(SLASH) + 1);
				String sessionId = data.getHost();
				BasicEvent event = createEvent(intent, eventName);

				//if session is informed --> send to this session
				if (sessionId != null) {
					if (this.engine.isSessionActive(sessionId)) {
						Log.d(FSM, String.format("Send intent %s to session %s", intent, sessionId));
						this.engine.pushEvent(sessionId, event);
					} else {

						//save missing event to relaunch on next macrostep
						Log.w(FSM, String.format(
								"Can't send intent to session %s, (session not found), saving on missing intents",
								sessionId));
						defaultListener.addMissingIntent(sessionId, intent);
					}

				} else {
					//send to all active sessions
					Collection<com.nosolojava.fsm.runtime.Context> activeContexts = engine.getActiveSessions();
					for (com.nosolojava.fsm.runtime.Context activeContext : activeContexts) {
						this.engine.pushEvent(activeContext.getSessionId(), event);
					}

				}

			}
			//
			// start fsm session schema://pathToSessionResource#sessionId 
			// example: android.resource://com.nosolojava.android.fsm.firstSteps/raw/fsm#testingSessionId
			//

			else if (intent.getData() != null && FSM_ACTIONS.INIT_FSM_SESSION.toString().equals(action)) {

				//sessionId from fragment (if any)
				String sessionId = intent.getData().getFragment();

				//if engine is not already started
				if (!engine.isSessionActive(sessionId)) {
					Log.i(FSM, "Starting FSM session: " + intent);

					//xpp uri
					URI uri = URI.create(intent.getData().toString());
					//start fsm
					starStateMachineAsync(sessionId, uri);
				}
			}
			//
			//handle view init --> to allow fsm update the view with current config
			// fsm session comes from intent extras
			// example intent.getExtras().getString(FSM_EXTRAS.SESSION_ID.toString());
			//
			else if (FSM_ACTIONS.INIT_ACTIVITY.toString().equals(action)) {
				String fsmSession = getSessionIdFromIntent(intent);
				Log.d(FSM, "on activity start, sending actual configuration. fsm session: " + fsmSession);

				BasicEvent event = new BasicEvent(VIEW_INIT);
				if (this.engine.isSessionActive(fsmSession)) {
					this.engine.pushEvent(fsmSession, event);
				} else {
					Log.w(FSM, String.format(
							"Can't send init view event to session %s, (session not found), saving on missing intents",
							fsmSession));
					defaultListener.addMissingIntent(fsmSession, intent);
				}

			}
			//
			//handle updates in assignments
			// fsm session comes from intent extras
			// example intent.getExtras().getString(FSM_EXTRAS.SESSION_ID.toString());
			//
			else if (FSM_ACTIONS.INIT_FSM_ASSIGN.toString().equals(action)) {

				//if the session is active
				String fsmSession = getSessionIdFromIntent(intent);
				if (engine.isSessionActive(fsmSession)) {
					//send the current session values
					sendCurrentAssignmentValuesToTheView(intent, fsmSession);

				} else {
					Log.w(FSM,
							String.format("Can't send init assign event to session %s, (session not found), saving on missing intents",
									fsmSession));
					defaultListener.addMissingIntent(fsmSession, intent);

				}

			} else {
				Log.d(FSM, "Event action not recognize, action: " + action);
			}
		}
	}

	protected void sendCurrentAssignmentValuesToTheView(Intent intent, String fsmSession) {
		Context context = this.engine.getSession(fsmSession);
		@SuppressWarnings("unchecked")
		Set<String> locations = (Set<String>) intent.getExtras().get(FSM_EXTRAS.CONTENT.toString());
		Serializable value;
		for (String location : locations) {
			value = context.getDataByName(location);

			Intent intentResponse = AndroidAssign.createAssignIntent(location, value);
			AndroidBroadcastIOProcessor.sendIntentToTheView(context, intentResponse);
		}
	}

	protected void starStateMachineAsync(final String sessionId, final URI uri) {
		AsyncTask<Void, Void, Boolean> startFsmTask = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				Boolean result = Boolean.valueOf(false);
				try {

					if (sessionId != null && !sessionId.equals("")) {
						engine.startFSMSession(sessionId, null, uri, null);
					} else {
						engine.startFSMSession(uri);
					}

					result = Boolean.valueOf(true);
					Log.i(FSM, "FSM session started: " + sessionId);
				} catch (Exception e) {
					// TODO manage start fsm errors
					Log.e(FSM, "Error initiating fsm, uri: " + uri, e);
				}

				return result;
			}
		};
		startFsmTask.execute((Void) null);

	}

	protected String getSessionIdFromIntent(Intent intent) {
		return intent.getExtras().getString(FSM_EXTRAS.SESSION_ID.toString());
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

}
