package com.nosolojava.android.fsm.executable;

import java.io.Serializable;

import android.content.Intent;
import android.os.Parcelable;

import com.nosolojava.android.fsm.bean.AssignDataParcelable;
import com.nosolojava.android.fsm.bean.AssignDataSerializable;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicAssign;
import com.nosolojava.fsm.runtime.Context;

public class AndroidAssign extends BasicAssign {
	private static final long serialVersionUID = 6964099132134295336L;

	protected AndroidAssign(String location, String expression, String value, String toText) {
		super(location, expression, value, toText);
	}

	public static AndroidAssign assignByExpression(String location, String expression) {
		String toText = "AndroidBasicAssign [" + location + "= " + expression + "]";
		return new AndroidAssign(location, expression, null, toText);
	}

	public static AndroidAssign assignByValue(String location, String value) {
		String toText = "AndroidBasicAssign [" + location + "= " + value + "]";
		return new AndroidAssign(location, null, value, toText);
	}

	@Override
	public void run(Context context) {
		super.run(context);

		String realLocation = getRealLocation(context);
		if (context.existsVarName(realLocation)) {
			Object value = context.getDataByName(realLocation);

			Intent intent = createAssignIntent(realLocation, value);

			AndroidBroadcastIOProcessor.sendIntentToTheView(context, intent);

		}

	}

	public static Intent createAssignIntent(String realLocation, Object value) {
		Intent intent = new Intent(FSM_ACTIONS.FSM_ASSIGN.toString());
		if (value != null && Parcelable.class.isAssignableFrom(value.getClass())) {
			AssignDataParcelable bodyParcelable = new AssignDataParcelable(realLocation, (Parcelable) value);
			intent.putExtra(FSM_EXTRAS.CONTENT.toString(), bodyParcelable);
		} else {
			AssignDataSerializable bodySerializable = new AssignDataSerializable(realLocation, (Serializable) value);
			intent.putExtra(FSM_EXTRAS.CONTENT.toString(), bodySerializable);
		}
		return intent;
	}

}
