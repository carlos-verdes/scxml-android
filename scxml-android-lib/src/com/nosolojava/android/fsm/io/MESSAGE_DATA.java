package com.nosolojava.android.fsm.io;

public enum MESSAGE_DATA {
	CONTENT, NAME, SESSION_ID, TARGET_URI, SOURCE_URI;

	private static final String COM_NOSOLOJAVA_FSM = "com.nosolojava.fsm.MESSAGE_DATA_";
	private String key;

	private MESSAGE_DATA() {
		this.key = COM_NOSOLOJAVA_FSM + this.name();

	}

	@Override
	public String toString() {
		return key;
	}

}
