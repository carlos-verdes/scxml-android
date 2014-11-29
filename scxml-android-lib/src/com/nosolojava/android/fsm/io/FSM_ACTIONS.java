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

	INIT_FSM_SESSION("INIT_FSM_SESSION"),
	FSM_SESSION_INITIATED("FSM_SESSION_INITIATED"),
	FSM_SESSION_ENDED("NOTIFY_FSM_SESSION_ENDED"), 
	FSM_NEW_SESSION_CONFIG("FSM_NEW_SESSION_CONFIG"), 
	SEND_EVENT_TO_FSM("SEND_EVENT_TO_FSM"); 

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
