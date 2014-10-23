package com.nosolojava.android.fsm.service;

import java.io.Serializable;
import java.net.URI;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcelable;
import android.util.Log;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.io.MESSAGE_DATA;

public abstract class AndroidFSMServiceTemplate extends Service {

	protected Messenger messenger;
	protected IncomingHandler handler;

	protected String sessionId;
	protected URI fsmUri;

	
	protected abstract void handleMessage(String messageName, String sessionId, android.os.Message message);

	public AndroidFSMServiceTemplate() {
		super();
		this.handler = new IncomingHandler(this);
		messenger = new Messenger(this.handler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		this.sessionId = intent.getStringExtra(FSM_EXTRAS.SESSION_ID.toString());
		this.fsmUri = (URI) intent.getSerializableExtra(FSM_EXTRAS.SOURCE_URI.toString());

		return this.messenger.getBinder();
	}

	protected void answerToFSM(String event){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, fsmUri,event);
	}
	protected void answerToFSM(String event,Serializable data){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, fsmUri,event,data);
	}
	protected void answerToFSM(String event,Parcelable data){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, fsmUri,event,data);
	}
	
	public static class IncomingHandler extends Handler {

		private final AndroidFSMServiceTemplate service;

		public IncomingHandler(AndroidFSMServiceTemplate service) {
			super();
			this.service = service;
		}


		@Override
		public void handleMessage(android.os.Message androidMessage) {
			Log.d("FSM", "BTLEClientService handle message: " + androidMessage.toString());
			Bundle data = androidMessage.getData();
			data.setClassLoader(this.getClass().getClassLoader());
			String messageName = data.getString(MESSAGE_DATA.NAME.toString());
			String sessionId = data.getString(MESSAGE_DATA.SESSION_ID.toString());

			this.service.handleMessage(messageName, sessionId, androidMessage);
		}
	}
}
