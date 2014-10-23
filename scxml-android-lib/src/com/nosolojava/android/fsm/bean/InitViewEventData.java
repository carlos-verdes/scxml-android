package com.nosolojava.android.fsm.bean;

import java.io.Serializable;
import java.util.Set;

public class InitViewEventData implements Serializable {
	private static final long serialVersionUID = 6647886363884641731L;

	private final String activityAction;
	private final Set<String> contextBindingNames;

	public InitViewEventData(String activityAction, Set<String> contextBindingNames) {
		super();
		if (!Serializable.class.isAssignableFrom(contextBindingNames.getClass())) {
			throw new RuntimeException("Can't send an event if the data is not Serializable");
		}

		this.activityAction = activityAction;
		this.contextBindingNames = contextBindingNames;
	}

	public String getActivityAction() {
		return activityAction;
	}

	public Set<String> getContextBindingNames() {
		return contextBindingNames;
	}

}
