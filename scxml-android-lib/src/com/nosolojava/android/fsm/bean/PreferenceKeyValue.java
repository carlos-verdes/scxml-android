package com.nosolojava.android.fsm.bean;

import java.io.Serializable;

public class PreferenceKeyValue<T> implements Serializable {
	private static final long serialVersionUID = -612613647197994511L;
	public static final String PREFERENCE_EVENT = "system.preferences.newValue";

	private final String key;
	private T value;

	public PreferenceKeyValue(String key, T value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "PreferenceKeyValue [key=" + key + ", value=" + value + "]";
	}

}
