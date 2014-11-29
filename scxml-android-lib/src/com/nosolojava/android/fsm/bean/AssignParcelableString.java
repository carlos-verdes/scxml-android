package com.nosolojava.android.fsm.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignParcelableString implements Parcelable {

	private final String name;
	private final String value;

	
	public AssignParcelableString(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public static final Parcelable.Creator<AssignParcelableString> CREATOR = new Creator<AssignParcelableString>() {

		@Override
		public AssignParcelableString[] newArray(int size) {
			return new AssignParcelableString[size];
		}

		@Override
		public AssignParcelableString createFromParcel(Parcel source) {

			String name= source.readString();
			String value= source.readString();
			AssignParcelableString assign = new AssignParcelableString(name,value);

			return assign;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.value);

	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
