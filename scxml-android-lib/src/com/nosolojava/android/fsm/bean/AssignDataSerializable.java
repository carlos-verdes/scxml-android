package com.nosolojava.android.fsm.bean;

import java.io.Serializable;

public class AssignDataSerializable extends AssignData implements Serializable {
	private static final long serialVersionUID = -758926167252218082L;

	private final String name;
	private final Serializable value;

	public AssignDataSerializable(String name, Serializable value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "AssignDataSerializable [name=" + name + ", value=" + value + "]";
	}

}
