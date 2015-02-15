package com.nosolojava.android.fsm.service;

import java.io.Serializable;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcelable;
import android.util.Log;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.MESSAGE_DATA;
import com.nosolojava.android.fsm.util.AndroidUtils;

public abstract class AndroidFSMServiceTemplate extends Service {

	protected Messenger messenger;
	protected IncomingHandler handler;

	protected String sessionId;
	protected Uri fsmUri;

	
	protected abstract void handleMessage(String messageName, String sessionId, android.os.Message message);

	public AndroidFSMServiceTemplate() {
		super();
		this.handler = new IncomingHandler(this);
		messenger = new Messenger(this.handler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		
		this.fsmUri = intent.getData();
		this.sessionId = AndroidUtils.getFSMSessionFromUri(this.fsmUri);

		return this.messenger.getBinder();
	}

	protected void answerToFSM(String event){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, this.getClass(), fsmUri,event);
	}
	protected void answerToFSM(String event,Serializable data){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, this.getClass(), fsmUri,event,data);
	}
	protected void answerToFSM(String event,Parcelable data){
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, this.getClass(), fsmUri,event,data);
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
