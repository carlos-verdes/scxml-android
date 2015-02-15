package com.nosolojava.fsm.android.firstSteps.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.runtime.ContextInstance;

public class FirstStepsFSMService extends FSMServiceImpl {

	@Override
	public void sendSessionInitiatedBroadcast(ContextInstance contextInstance) {
		// call super
		super.sendSessionInitiatedBroadcast(contextInstance);

		// create a notification
		Intent intentStart = new Intent(this, com.nosolojava.fsm.android.firstSteps.FSMSimpleActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 133, intentStart,
				Intent.FLAG_FROM_BACKGROUND);

		Notification notification = new NotificationCompat.Builder(this)
				// .setCategory(Notification.CATEGORY_SERVICE)
				.setContentTitle("Android FSM first steps service.").setContentText("This keeps FSM alive")
				.setContentIntent(pendingIntent).setSmallIcon(com.nosolojava.fsm.android.R.drawable.ic_action_gear)
				.getNotification();

		notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
		this.startForeground(133, notification);

	}

	@Override
	public void sendSessionEndBroadcast(ContextInstance contextInstance) {
		// stop the service
		this.stopForeground(true);

		// call super
		super.sendSessionEndBroadcast(contextInstance);
	}

}
