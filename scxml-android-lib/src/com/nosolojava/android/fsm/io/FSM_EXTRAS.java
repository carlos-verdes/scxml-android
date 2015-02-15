package com.nosolojava.android.fsm.io;

public enum FSM_EXTRAS {

	CONTENT, TARGET_URI, NAME, SCXML_URI;

	private static final String COM_NOSOLOJAVA_FSM = "com.nosolojava.fsm.EXTRA_";

	private FSM_EXTRAS() {
		this.value = COM_NOSOLOJAVA_FSM + "_" + name();
	}

	private String value;

	@Override
	public String toString() {
		return this.value;
	}

}
