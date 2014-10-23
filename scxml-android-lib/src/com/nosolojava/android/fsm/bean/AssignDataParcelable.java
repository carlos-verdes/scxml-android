package com.nosolojava.android.fsm.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignDataParcelable extends AssignData implements Parcelable {

	private final String name;
	private final Parcelable value;

	public AssignDataParcelable(String name, Parcelable value) {
		super();
		this.name = name;
		this.value = value;
	}

	public static final Parcelable.Creator<AssignDataParcelable> CREATOR = new Parcelable.Creator<AssignDataParcelable>() {

		@Override
		public AssignDataParcelable createFromParcel(Parcel source) {
			String name = source.readString();
			@SuppressWarnings("rawtypes")
			Class clazz= (Class) source.readValue(Class.class.getClassLoader());
			Parcelable value = source.readParcelable(clazz.getClassLoader());

			AssignDataParcelable result = new AssignDataParcelable(name, value);

			return result;
		}

		@Override
		public AssignDataParcelable[] newArray(int size) {
			return null;
		}

	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(name);
		dest.writeValue(value.getClass());
		dest.writeParcelable(this.value, flags);
		
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "AssignDataParcelable [name=" + name + ", value=" + value + "]";
	}

}
