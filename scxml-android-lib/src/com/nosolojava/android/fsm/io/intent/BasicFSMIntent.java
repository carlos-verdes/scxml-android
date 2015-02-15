package com.nosolojava.android.fsm.io.intent;

import java.util.Iterator;

import com.nosolojava.android.fsm.service.FSMServiceImpl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class BasicFSMIntent extends Intent {

	public static final String FSM_SCHEME = "fsm";

	private static final String SESSION_SEGMENT = "session";

	// fsm session uri template--> fsm://session/{sessionId}
	public static final String FSM_SESSION_TEMPLATE = FSM_SCHEME + "://" + SESSION_SEGMENT + "/%s";

	public BasicFSMIntent(String fsmSessionId, Context packageContext,
			Class<? extends FSMServiceImpl> fsmServiceClass) {
		super();

		this.setClass(packageContext, fsmServiceClass);

		String uriString = String.format(FSM_SESSION_TEMPLATE, fsmSessionId);
		this.setData(Uri.parse(uriString));

	}

	public String getSessionId() {
		String sessionId = null;

		sessionId = findSegment(this.getData(), SESSION_SEGMENT);

		return sessionId;
	}

	protected String findSegment(Uri uri, String segment) {
		String sessionId = null;

		Iterator<String> pathSegmentsIter = uri.getPathSegments().iterator();

		boolean sessionSegmentFound = false;
		while (!sessionSegmentFound && pathSegmentsIter.hasNext()) {
			sessionSegmentFound = segment.equals(pathSegmentsIter.next());
		}

		if (sessionSegmentFound && pathSegmentsIter.hasNext()) {
			sessionId = pathSegmentsIter.next();
		}
		return sessionId;
	}

	public Uri getFSMUri() {
		return this.getData();
	}

}
