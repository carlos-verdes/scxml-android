package com.nosolojava.android.fsm.io;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * <p>
 * Start new activity ({@code androidContext.startActivity(intent)}. <br/>
 * {@code <send type="activitiy" event="your.activity.Class" target="http://someuri">}
 * <ul>
 * <li><b>event</b> is the activity <b>class</b>
 * <li><b>source session uri</b> is the intent <b>data</b> (so receivers could filter) in the form fsm://sessionId
 * <li><b>content</b> will be passed as an extra "com.nosolojava.fsm.EXTRA_CONTENT" (you can use
 * {@link FSM_EXTRAS#CONTENT}).
 * <li><b>target</b> will be pased as an extra "com.nosolojava.fsm.EXTRA_TARGET_URI" (you can use
 * {@link FSM_EXTRAS#TARGET_URI}).
 * 
 * @author cverdes
 *
 */
public class AndroidActivityIOProcessor extends AndroidBroadcastIOProcessor {

	public static final String NAME = "activity";

	public AndroidActivityIOProcessor(Context androidContext, Service fsmService) {
		super(androidContext, fsmService);
	}

	@Override
	protected void sendIntent(Intent intent) {
		this.androidContext.startActivity(intent);
	}

	@Override
	protected void loadEvent(String event, Intent intent) {
		intent.setClassName(this.androidContext, event);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
