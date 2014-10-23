package com.nosolojava.android.fsm.listener;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.nosolojava.android.fsm.bean.PreferenceBean;

public class FSMPreferenceChangeListener implements OnSharedPreferenceChangeListener {

	private final String fsmSessionId;
	private final Context androidContext;

	public FSMPreferenceChangeListener(String sessionId, Context androidContext) {
		super();
		this.fsmSessionId = sessionId;
		this.androidContext = androidContext;

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		PreferenceBean<Serializable> pref = new PreferenceBean<Serializable>(Serializable.class, key);
		pref.sendUpdateToFSM(this.androidContext, this.fsmSessionId, sharedPreferences);

	}
}
