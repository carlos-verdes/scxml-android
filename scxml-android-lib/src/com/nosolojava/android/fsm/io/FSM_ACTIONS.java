package com.nosolojava.android.fsm.io;

public enum FSM_ACTIONS {

	FSM_SESSION_EXPIRED("FSM_SESSION_EXPIRED"), FSM_ASSIGN("FSM_ASSIGN"), SEND_EVENT_TO_FSM("SEND_EVENT_TO_FSM"), FSM_ACTIVE_STATES("FSM_ACTIVE_STATES"), INIT_FSM_SESSION(
			"INIT_FSM_SESSION"), INIT_ACTIVITY("INIT_ACTIVITY"), INIT_FSM_ASSIGN("INIT_FSM_ASSIGN");

	private static final String COM_NOSOLOJAVA_FSM = "com.nosolojava.fsm.ACTION_";

	private FSM_ACTIONS(String action) {
		this.action = COM_NOSOLOJAVA_FSM + action;
	}

	private String action;

	@Override
	public String toString() {
		return this.action;
	}

}
