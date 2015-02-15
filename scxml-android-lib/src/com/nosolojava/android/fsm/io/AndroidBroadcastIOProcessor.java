package com.nosolojava.android.fsm.io;

import java.net.URI;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.nosolojava.android.fsm.util.AndroidUtils;

/**
 * <p>
 * Sends broadcast intents ( {@code context.sendBroadcast(intent));}) <br/>
 * {@code <send type="broadcast" event="your.intent.ACTION" target="http://someuri">}
 * 
 * <ul>
 * <li><b>event</b> is the intent <b>action</b>
 * <li><b>source session uri</b> is the intent <b>data</b> (so receivers could filter) in the form fsm://sessionId
 * <li><b>content</b> will be passed as an extra "com.nosolojava.fsm.EXTRA_CONTENT" (you can use
 * {@link FSM_EXTRAS#CONTENT}).
 * <li><b>target</b> will be pased as an extra "com.nosolojava.fsm.EXTRA_TARGET_URI" (you can use
 * {@link FSM_EXTRAS#TARGET_URI}).
 * 
 * @author cverdes
 *
 */
public class AndroidBroadcastIOProcessor extends AbstractAndroidIOProcessor {

	public static final String NAME = "broadcast";

	protected final Context androidContext;
	
	protected final Service fsmService;


	public AndroidBroadcastIOProcessor(Context androidContext, Service fsmService) {
		super();
		this.androidContext = androidContext;
		this.fsmService = fsmService;
	}

	@Override
	public String getName() {
		return NAME;
	}

	public void sendBroadcastFromFSM(String sessionId, FSM_ACTIONS action) {
		sendBroadcastFromFSM(sessionId, action, null);
	}

	public void sendBroadcastFromFSM(String sessionId, FSM_ACTIONS action, Object data) {
		Intent intent = new Intent(action.toString());
		URI sessionUri = getLocationStatic(sessionId);
		intent.setData(Uri.parse(sessionUri.toString()));

		if (data != null) {
			AndroidUtils.addContentToIntent(intent, data);
		}

		androidContext.sendBroadcast(intent);
	}

	public static void sendMessageToFSM(String sessionId, Context androidContext, Class<? extends Service> serviceClazz, String event) {
		sendMessageToFSM(sessionId, androidContext, serviceClazz, event, null);
	}

	public static void sendMessageToFSM(String sessionId, Context androidContext, Class<? extends Service> serviceClazz, String event, Object data) {
		 Uri fsmUri = getAndroidLocationStatic(sessionId);
		sendMessageToFSM(androidContext, serviceClazz, fsmUri, event, data);
	}

	public static void sendMessageToFSM(Context androidContext, Class<? extends Service> serviceClazz, Uri fsmUri, String event) {
		sendMessageToFSM(androidContext, serviceClazz, fsmUri, event, null);

	}

	public static void sendMessageToFSM(Context androidContext, Class<? extends Service> serviceClazz, Uri fsmUri, String event, Object data) {
		Intent intent = new Intent(androidContext, serviceClazz);
		intent.setAction(FSM_ACTIONS.SEND_EVENT_TO_FSM.toString());
		
		// add event to uri
		Uri uriWithEvent = Uri.withAppendedPath(fsmUri, event);
		intent.setData(uriWithEvent);
		if (data != null) {
			AndroidUtils.addContentToIntent(intent, data);
		}

		androidContext.startService(intent);

	}

	@Override
	protected void sendIntent(Intent intent) {
		this.androidContext.sendBroadcast(intent);
	}

}
