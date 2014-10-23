package com.nosolojava.android.fsm.service;

import java.text.MessageFormat;

import android.util.Log;

import com.nosolojava.fsm.runtime.FSMLogCallback;

public class AndroidLogCallback implements FSMLogCallback {

	public static String TAG="SCXML";
	
	@Override
	public void logDebug(String text) {
		Log.d(TAG,text);
	}

	@Override
	public void logDebug(String text, Object[] data) {
		MessageFormat mf= new MessageFormat(text);
		text=mf.format(data);
		
		logDebug(text);

	}

	@Override
	public void logInfo(String text) {
		Log.i(TAG,text);

	}

	@Override
	public void logWarning(String text) {
		Log.w(TAG,text);

	}

	@Override
	public void logError(String text) {
		Log.e(TAG,text);
	}

}
