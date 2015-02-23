package com.nosolojava.android.remotecam.service;

import java.util.Arrays;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.MESSAGE_DATA;

public class Cam2Service extends Service {
	private static final String REMOTE_CAM_LOG_TAG = "remoteCam";

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private CameraManager manager;
	private Uri sessionUri;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			logI("handle message");

			Toast.makeText(getApplicationContext(), "hello, a messages arrived!", Toast.LENGTH_SHORT).show();

			String messageName = msg.getData().getString(MESSAGE_DATA.NAME.toString());

			logI(String.format("message name: %s", messageName));
			if ("controller.action.getDevices".equals(messageName)) {
				try {
					getDevices(msg);
				} catch (CameraAccessException e) {
					logE("Error geting camera devices", e);
					sendEventToFSM("service.camera.getDevices.error", e);

				}
			}

		}

	}

	private void getDevices(Message msg) throws CameraAccessException {
		logI("getting devices");
		String[] devicesIds = manager.getCameraIdList();

		logI(String.format("device found:  %s", java.util.Arrays.toString(devicesIds)));

		sendEventToFSM("service.camera.getDevices.result", devicesIds);
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();

		logI("creating cam service");
		manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

	}
	

	@Override
	public void onDestroy() {
		logI("destroying cam service");
		super.onDestroy();
	}



	/**
	 * When binding to the service, we return an interface to our messenger for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		logI("onbind");

		Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();

		this.sessionUri = intent.getData();

		sendEventToFSM("service.camera.ready");

		return mMessenger.getBinder();
	}

	protected void sendEventToFSM(String event) {
		sendEventToFSM(event, null);
	}

	protected void sendEventToFSM(String event, Object data) {
		AndroidBroadcastIOProcessor.sendMessageToFSM(this, MainFSMService.class, this.sessionUri, event, data);
	}



	private void logI(String message) {
		Log.i(REMOTE_CAM_LOG_TAG, message);
	}

	private void logE(String message, Throwable t) {
		Log.e(REMOTE_CAM_LOG_TAG, message, t);
	}
}
