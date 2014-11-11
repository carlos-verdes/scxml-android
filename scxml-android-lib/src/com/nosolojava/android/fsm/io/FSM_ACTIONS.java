package com.nosolojava.android.fsm.io;

import com.nosolojava.android.fsm.service.FSMServiceImpl;

/**
 * <p>Actions to interact with the FSM.
 * <ul>
 * <li><strong>INIT_FSM_ASSIGN:</strong> used to start a new FSM session. The data will contains the uri for the scxml resource. Withing the uri fragment (# part) could be passed the session id.<br/>
 * Example with session: android.resource://com.nosolojava.fsm.android.firstSteps/raw/fsm#first-steps-session
 * <li><strong>FSM_SESSION_INITIATED: </strong> sent by {@link FSMServiceImpl} when the fsm session has been initiated. The intent data contains the response uri (the session id could not be passed in the previous step) 
 * @author cverdes
 *
 */
public enum FSM_ACTIONS {

	
	INIT_FSM_ASSIGN("INIT_FSM_ASSIGN"),
	FSM_SESSION_EXPIRED("FSM_SESSION_EXPIRED"), 
	FSM_ASSIGN("FSM_ASSIGN"), 
	SEND_EVENT_TO_FSM("SEND_EVENT_TO_FSM"), 
	FSM_ACTIVE_STATES("FSM_ACTIVE_STATES"), 
	INIT_FSM_SESSION("INIT_FSM_SESSION"), 
	INIT_ACTIVITY("INIT_ACTIVITY"), 
	FSM_SESSION_INITIATED("FSM_SESSION_INITIATED");

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
