package com.nosolojava.android.fsm.listener;

import java.io.Serializable;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.nosolojava.android.fsm.bean.PreferenceBean;

public class FSMPreferenceChangeListener implements OnSharedPreferenceChangeListener {

	private final String fsmSessionId;
	private final Context androidContext;
	private final Class<? extends Service> fsmServiceClazz;

	public FSMPreferenceChangeListener(String fsmSessionId, Context androidContext,
			Class<? extends Service> fsmServiceClazz) {
		super();
		this.fsmSessionId = fsmSessionId;
		this.androidContext = androidContext;
		this.fsmServiceClazz = fsmServiceClazz;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		PreferenceBean<Serializable> pref = new PreferenceBean<Serializable>(Serializable.class, fsmServiceClazz, key);
		pref.sendUpdateToFSM(this.androidContext, this.fsmSessionId, sharedPreferences);

	}
}
