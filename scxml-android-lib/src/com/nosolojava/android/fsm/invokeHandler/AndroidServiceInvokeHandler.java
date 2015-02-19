package com.nosolojava.android.fsm.invokeHandler;

import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.MESSAGE_DATA;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.PlatformEvents;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.AbstractBasicInvokeHandler;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

/**
 * <p>Used to invoke android services from fsm.
 * <p>Example with action:
 * {@code <invoke id="chatServiceInvokeId" type="service" autoforward="false" src="action:com.nosolojava.chat.START_SERVICE_ACTION" />}
 * 
 * <p>Example with classname:
 * {@code <invoke id="chatServiceInvokeId" type="service" autoforward="false" src="class:com.nosolojava.chat.MyChatService" />}
 * 
 * <p>The src will be used to create the intent, see: {@link AndroidUtils#createIntentForExternalServices(android.content.Context, URI, Context)}. <br/>
 * the id can be used later to send events to this service (that's the reason why this class implements also
 * IOProcessor.
 * <p>
 * To send events to the invoked service use the scxml send with the next arguments:
 * <ul>
 * <li>type: always invoked-service</li>
 * <li>event: the event name to be handled
 * <li>target: pass the id in the fragment part (the part after # in uris)</li>
 * </ul>
 * Example:
 * {@code <send type="invoked-service" event="controller.action.login" namelist="username password" target="#chatServiceInvokeId" />}
 * 
 * <p>
 * Finally in the called service the message can be handled getting the event name and data from the
 * {@link android.os.Message} like the next code: 
 * <p>{@code  public void handleMessage(android.os.Message msg){
 * <br/>&nbsp;&nbsp;&nbsp;{@code Bundle messageData = msg.getData();}
 * <br/>&nbsp;&nbsp;&nbsp;{@code String messageName = messageData.getString(MESSAGE_DATA.NAME.toString());}
 * <br/>&nbsp;&nbsp;&nbsp;{@code String sessionId = messageData.getString(MESSAGE_DATA.SESSION_ID.toString());}
 * <br/>&nbsp;&nbsp;&nbsp;{@code HashMap<String, String> messageBody= (HashMap<String, String>)}
 * <br/>&nbsp;&nbsp;&nbsp;{@code messageData.getSerializable(MESSAGE_DATA.CONTENT.toString());}
 * <br/>}
 *
 * @author Carlos Verdes
 *
 */
public class AndroidServiceInvokeHandler extends AbstractBasicInvokeHandler implements IOProcessor {

	private static final String ERROR_EVENT_NAME = PlatformEvents.EXECUTION_ERROR.toString()
			+ ".invoke.android.service";
	private static final Event ERROR_EVENT = new BasicEvent(ERROR_EVENT_NAME);

	private static final long serialVersionUID = -2816548281755751590L;

	private static final String INVOKE_TYPE = "service";
	private static final String SEND_TYPE = "invoked-service";

	private static final String LOG_TAG = FSMServiceImpl.FSM;

	private final android.content.Context androidContext;
	private StateMachineEngine engine;

	private final ConcurrentHashMap<String, SCXMLServiceConn> connMap = new ConcurrentHashMap<String, AndroidServiceInvokeHandler.SCXMLServiceConn>();

	public AndroidServiceInvokeHandler(android.content.Context androidContext, StateMachineEngine engine) {
		super();
		this.androidContext = androidContext;
		this.engine = engine;
	}

	@Override
	public String getType() {
		return INVOKE_TYPE;
	}

	@Override
	public String getName() {
		return SEND_TYPE;
	}

	@Override
	public URI getLocation(String sessionId) {
		// the receiver will be the broadcast IO processor
		URI location = AndroidBroadcastIOProcessor.getLocationStatic(sessionId);
		return location;
	}

	@Override
	public void sendMessageFromFSM(Message message) {

		URI target = message.getTarget();

		if (target != null && target.getFragment() != null) {
			String invokeId = target.getFragment();

			Context context = this.getContextByInvokeId(invokeId);
			if (context != null) {
				SCXMLServiceConn conn = this.connMap.get(invokeId);
				if (conn != null) {
					Messenger messenger = conn.getMessenger();

					android.os.Message androidMessage = createAndroidMessage(message, context.getSessionId());

					try {
						if (messenger != null) {
							messenger.send(androidMessage);
						} else {
							Log.w(LOG_TAG, String.format(
									"Error sending message to service, messenger is null. Invokeid: %s", invokeId));
						}
					} catch (RemoteException e) {
						Log.e(LOG_TAG, "Error sending message to android service.", e);
						sendEventToFSM(invokeId, ERROR_EVENT);
					}
				}

			} else {
				Log.w(LOG_TAG, String.format(
						"Can't send message (cant' find connection) for Android service invoke id %s.", invokeId));

			}

		}

	}

	@Override
	public void setEngine(StateMachineEngine engine) {
		this.engine = engine;
	}

	
	@Override
	public void sendEventToFSM(String sessionId, Event event) {
		if(this.engine.isSessionActive(sessionId)){
			this.engine.pushEvent(sessionId, event);
		}
		
	}
	protected class SCXMLServiceConn implements ServiceConnection {
		private AtomicBoolean connected = new AtomicBoolean(false);
		private Messenger serviceMessenger = null;

		private CountDownLatch initLatch = new CountDownLatch(1);

		private final String invokeId;

		public SCXMLServiceConn(String invokeId) {
			super();
			this.invokeId = invokeId;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(LOG_TAG, "onServiceConnected, invokeId: " + invokeId);
			this.initLatch.countDown();
			this.serviceMessenger = new Messenger(service);
			this.connected.set(true);

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(LOG_TAG, "onServiceDisconnected, invokeId: " + invokeId);
			this.initLatch.countDown();
			this.connected.set(false);
			this.serviceMessenger = null;
		}

		public CountDownLatch getInitLatch() {
			return this.initLatch;
		}

		public Messenger getMessenger() {
			return this.serviceMessenger;
		}

		public boolean isConnected() {
			return this.connected.get();
		}
	};

	@Override
	public void invokeServiceInternal(InvokeInfo invokeInfo, com.nosolojava.fsm.runtime.Context fsmContext) {
		String invokeId = invokeInfo.getInvokeId();
		Log.d(LOG_TAG, "service invoke, invokeId: " + invokeId);

		Intent bindingIntent = AndroidUtils.createIntentForExternalServices(androidContext, invokeInfo.getSource(),
				fsmContext);

		if (bindingIntent != null) {
			SCXMLServiceConn conn = new SCXMLServiceConn(invokeId);

			androidContext.bindService(bindingIntent, conn, android.content.Context.BIND_AUTO_CREATE);

			this.connMap.put(invokeId, conn);

			// TODO configure init invoke timeout
			// wait until the invoke is initiated
			CountDownLatch latch = conn.getInitLatch();
			waitUntilInit(latch, 3000, TimeUnit.MILLISECONDS, invokeId);
		} else {
			Log.w(LOG_TAG,
					String.format("Error invoking service, can't create binding from uri: %s", invokeInfo.getSource()));
		}

	}

	public boolean waitUntilInit(CountDownLatch latch, long timeout, TimeUnit unit, String invokeId) {
		Log.d(LOG_TAG, String.format("wait service invoke init, invokeId: %s", invokeId));
		boolean result = false;
		try {
			result = latch.await(timeout, unit);
		} catch (InterruptedException e) {
			// TODO send invoke error event
			Log.e(LOG_TAG, String.format("Error initiating service invoke, invokeId: %s", invokeId));
		}
		Log.d(LOG_TAG, String.format("service invoke initiated with result %b, invokeId: %s", result, invokeId));

		return result;
	}

	@Override
	public void sendMessageToService(Message message, Context context) {
		Log.d(LOG_TAG, "send message to service, message: " + message.getName());
		this.sendMessageFromFSM(message);

	}

	protected android.os.Message createAndroidMessage(Message message, String sessionId) {
		android.os.Message androidMessage = android.os.Message.obtain();
		Object body = message.getBody();
		if (body != null) {
			if (Parcelable.class.isAssignableFrom(body.getClass())) {
				androidMessage.getData().putParcelable(MESSAGE_DATA.CONTENT.toString(), (Parcelable) body);
			} else {
				androidMessage.getData().putSerializable(MESSAGE_DATA.CONTENT.toString(), (Serializable) body);

			}
		}
		// include message name and session id
		androidMessage.getData().putString(MESSAGE_DATA.NAME.toString(), message.getName());
		androidMessage.getData().putString(MESSAGE_DATA.SESSION_ID.toString(), sessionId);
		androidMessage.getData().putSerializable(MESSAGE_DATA.TARGET_URI.toString(), message.getTarget());
		androidMessage.getData().putSerializable(MESSAGE_DATA.SOURCE_URI.toString(), message.getSource());

		return androidMessage;
	}

	@Override
	public void onEndSession(String invokeId, Context context) {
		SCXMLServiceConn conn = this.connMap.get(invokeId);
		if (conn != null) {
			this.androidContext.unbindService(conn);
			this.connMap.remove(invokeId);
		}
	}

}
