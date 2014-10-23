package com.nosolojava.android.fsm.bean;

import android.os.Parcelable;

import com.nosolojava.fsm.runtime.Event;

public interface AndroidEvent extends Event {

	Parcelable getParcel();

}
