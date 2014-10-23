package com.nosolojava.android.fsm.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class MetadataUtil {

	public static Bundle getApplicationMetadata(Context ctx) {
		ApplicationInfo applicationInfo = ctx.getApplicationInfo();
		String appPackage = applicationInfo.packageName;
		Bundle metaData = null;
		try {
			metaData = ctx.getPackageManager().getApplicationInfo(appPackage, PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			Log.e("FSM", "Error getting app data", e);
		}

		return metaData;
	}

	public static String getAppPackage(Context ctx) {
		ApplicationInfo applicationInfo = ctx.getApplicationInfo();
		String appPackage = applicationInfo.packageName;
		return appPackage;

	}

	public static Class<?> classFromName(String className, Context ctx) throws ClassNotFoundException {
		if (className.startsWith(".")) {
			String packName = getAppPackage(ctx);
			className = packName + className;
		}

		Class<?> classIntance = Class.forName(className);
		return classIntance;
	}

	@SuppressWarnings("unchecked")
	public static <T> T instantiateObject(String parserClass, String packageName) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<T> actionClass;
		T parser;
		Constructor<T> cons;
		String auxParserClass = parserClass.startsWith(".") ? packageName + parserClass : parserClass;
		Log.i("FSM", "instantiating: " + auxParserClass);

		actionClass = (Class<T>) Class.forName(auxParserClass);
		cons = actionClass.getConstructor();
		parser = cons.newInstance();
		return parser;
	}

	public static <T> List<T> instantiateObjects(String prefix, Context ctx) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		int i = 1;
		Bundle metadata = getContextMetadata(ctx);
		String classname = metadata.getString(prefix + i);
		List<T> result = new ArrayList<T>();

		String packageName = getAppPackage(ctx);
		T parser;
		while (classname != null) {
			parser = MetadataUtil.instantiateObject(classname, packageName);
			result.add(parser);

			i++;
			classname = metadata.getString(prefix + i);
		}

		return result;
	}

	public static Bundle getContextMetadata(Context context) {
		Bundle metadata = null;
		try {
			metadata = context.getPackageManager().getServiceInfo(new ComponentName(context, context.getClass()),
					PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			Log.e("FSM", "Error getting context app data", e);
		}
		return metadata;
	}

}
