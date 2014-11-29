package com.nosolojava.android.fsm.view.binding.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.View;

import com.nosolojava.android.fsm.view.binding.XPPFSMViewBindingHandler;

public class ViewBindingParser {

	private static final String LOG_TAG = "fsBind";
	public static final String ID = "id";

	private final Map<String, List<XPPFSMViewBindingHandler>> xppViewHandlers = new HashMap<String, List<XPPFSMViewBindingHandler>>();

	public ViewBindingParser() {
		super();

		// default bindin handlers

		// on state change handler
		registerFSMViewBindingHandler(new OnNewStateBindingHandler());

		// value binding handler
		registerFSMViewBindingHandler(new ValueBindingHandler());

		// onclick handler
		registerFSMViewBindingHandler(new OnclickBindingHandler());

	}

	public void parseXppViewBasedHandlers(Activity androidActivity, int viewId) {
		XmlResourceParser xpp = androidActivity.getResources().getXml(viewId);

		try {
			xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

			// until the end of the document
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {

				if (xpp.getEventType() == XmlPullParser.START_TAG) {

					String namespace = xpp.getNamespace();

					// if namespace is Android
					if ("".equals(namespace) || XPPFSMViewBindingHandler.ANDROID_NS.equals(namespace)) {
						// parse android element (att based)
						parseAndroidElement(androidActivity, xpp);

					} else
					// if there is any handler
					if (this.xppViewHandlers.containsKey(namespace)) {
						// parse the custom element (element based)
						parseCustomXmlElement(xpp, namespace);

					}

					// go to next xpp event
					xpp.next();

				} else {
					// on any other xpp event do next
					xpp.next();
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error parsing", e);
		}

	}

	protected void parseCustomXmlElement(XmlPullParser xpp, String namespace) {
		for (XPPFSMViewBindingHandler handler : this.xppViewHandlers.get(namespace)) {
			handler.registerXMLElementBinding(xpp);
		}
	}

	protected void parseAndroidElement(Activity androidActivity, XmlPullParser xpp) {
		View view = null;

		int attCount = xpp.getAttributeCount();
		String attNs;
		// for each attribute
		for (int i = 0; i < attCount; i++) {
			attNs = xpp.getAttributeNamespace(i);
			// if some element has a handler
			if (!XPPFSMViewBindingHandler.ANDROID_NS.equals(attNs) && this.xppViewHandlers.containsKey(attNs)) {
				registerXppAttBinding(androidActivity, xpp, view, attNs, i);
			}
		}
	}

	protected void registerXppAttBinding(Activity androidActivity, XmlPullParser xpp, View view, String attNs, int i) {
		String attValue;
		String attName;
		if (view == null) {
			view = this.getViewFromXpp(androidActivity, xpp, XPPFSMViewBindingHandler.ANDROID_NS);
		}

		if (view != null) {
			attName = xpp.getAttributeName(i);
			attValue = xpp.getAttributeValue(i);
			List<XPPFSMViewBindingHandler> handlers = this.xppViewHandlers.get(attNs);
			for (XPPFSMViewBindingHandler handler : handlers) {
				handler.registerXMLAttributeBinding(view, attName, attValue);
			}
		}
	}

	private View getViewFromXpp(Activity androidActivity, XmlPullParser xpp, String androidNs) {

		int id = getViewId(xpp, androidNs);

		View view = id != -1 ? androidActivity.findViewById(id) : null;

		return view;

	}

	private int getViewId(XmlPullParser xpp, String androidNs) {
		int id = -1;

		String idString = xpp.getAttributeValue(androidNs, ID);
		if (idString == null) {
			Log.w(LOG_TAG,
					"Error getting android id, review your layout elements (all must have an id to do a binding).");
		} else {
			idString = idString.replaceAll("\\D", "");
			id = Integer.parseInt(idString);
		}
		return id;
	}

	public boolean registerFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		List<XPPFSMViewBindingHandler> handlers;
		String namespace = handler.getNamespace();
		if (this.xppViewHandlers.containsKey(namespace)) {
			handlers = this.xppViewHandlers.get(namespace);
		} else {
			handlers = new ArrayList<XPPFSMViewBindingHandler>();
		}

		handlers.add(handler);
		this.xppViewHandlers.put(namespace, handlers);

		return true;
	}

	public Object unregisterFSMViewBindingHandler(XPPFSMViewBindingHandler handler) {
		return this.xppViewHandlers.remove(handler.getNamespace());
	}

	public List<XPPFSMViewBindingHandler> getNosolojavaHandlers(){
		return this.xppViewHandlers.get(AbstractFSMViewBindingHandler.BASIC_FSM_VIEW_HANDLER_NAMESPACE);

	}

	public Collection<List<XPPFSMViewBindingHandler>> getHandlerList() {
		Collection<List<XPPFSMViewBindingHandler>> result = this.xppViewHandlers.values();
		return result;
	}
}
