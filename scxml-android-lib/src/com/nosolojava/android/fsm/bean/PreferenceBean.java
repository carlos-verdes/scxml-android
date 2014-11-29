package com.nosolojava.android.fsm.bean;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;

public class PreferenceBean<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -2306309356215155702L;

	public static final Class<?>[] TEXT_CLASSES = new Class<?>[] { String.class };
	public static final Class<?>[] INTEGER_CLASSES = new Class<?>[] { int.class, Integer.class };
	public static final Class<?>[] LONG_CLASSES = new Class<?>[] { Long.class, long.class };
	public static final Class<?>[] DOUBLE_CLASSES = new Class<?>[] { Double.class, double.class };
	public static final Class<?>[] FLOAT_CLASSES = new Class<?>[] { Float.class, float.class };
	public static final Class<?>[] BOOLEAN_CLASSES = new Class<?>[] { Boolean.class, boolean.class };
	public static final Class<?>[] BYTE_ARRAY_CLASSES = new Class<?>[] { byte[].class };

	private Class<? extends T> clazz;
	private final String key;

	public PreferenceBean(Class<? extends T> clazz, String key) {
		super();
		this.clazz = clazz;
		this.key = key;

	}

	public PreferenceKeyValue<T> getPrefKeyValue(SharedPreferences preferences) {
		T value = getValueFromPreferences(preferences);
		PreferenceKeyValue<T> prefKeyValue = new PreferenceKeyValue<T>(this.key, value);
		return prefKeyValue;
	}

	@SuppressWarnings("unchecked")
	public T getValueFromPreferences(SharedPreferences preferences) {
		T result = null;
		String key = this.getKey();
		if (isAssignable(clazz, TEXT_CLASSES)) {
			result = (T) preferences.getString(key, "");
		} else if (isAssignable(clazz, INTEGER_CLASSES)) {
			result = (T) (Integer) preferences.getInt(key, -1);
		} else if (isAssignable(clazz, LONG_CLASSES)) {
			result = (T) (Long) preferences.getLong(key, -1);
		} else if (isAssignable(clazz, FLOAT_CLASSES)) {
			result = (T) (Float) preferences.getFloat(key, -1);
		} else if (isAssignable(clazz, BOOLEAN_CLASSES)) {
			result = (T) (Boolean) preferences.getBoolean(key, false);
		}
		return result;
	}

	public String getKey() {
		return this.key;
	}

	public void updatePreference(SharedPreferences preferences, T newValue) {

		String key = getKey();

		if (isAssignable(clazz, TEXT_CLASSES)) {
			preferences.edit().putString(key, (String) newValue);
		} else if (isAssignable(clazz, INTEGER_CLASSES)) {
			preferences.edit().putInt(key, (Integer) newValue);
		} else if (isAssignable(clazz, LONG_CLASSES)) {
			preferences.edit().putLong(key, (Long) newValue);
		} else if (isAssignable(clazz, FLOAT_CLASSES)) {
			preferences.edit().putFloat(key, (Float) newValue);
		} else if (isAssignable(clazz, BOOLEAN_CLASSES)) {
			preferences.edit().putBoolean(key, (Boolean) newValue);
		}
		preferences.edit().commit();

	}

	public void sendUpdateToFSM(Context androidContext, String sessionId, SharedPreferences preferences) {

		Serializable keyVal = getPrefKeyValue(preferences);
		AndroidBroadcastIOProcessor.sendMessageToFSM(sessionId, androidContext, PreferenceKeyValue.PREFERENCE_EVENT,
				keyVal);
	}

	private boolean isAssignable(Class<?> fieldClass, Class<?>... classes) {
		boolean result = false;

		for (Class<?> clazz : classes) {
			result = result || fieldClass.isAssignableFrom(clazz);
			if (result) {
				break;
			}
		}

		return result;
	}
}
