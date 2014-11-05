package com.nosolojava.android.fsm.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.nosolojava.android.fsm.executable.AndroidAssign;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.parser.XppStateMachineParser;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.executable.Executable;

public class AndroidXppStateMachineParser extends XppStateMachineParser {

	private final android.content.Context androidContext;

	public AndroidXppStateMachineParser(android.content.Context androidContext) throws ConfigurationException {
		super();
		this.androidContext = androidContext;
	}

	public AndroidXppStateMachineParser(List<XppActionParser> actionParsers, android.content.Context androidContext)
			throws ConfigurationException {
		super(actionParsers);
		this.androidContext = androidContext;
	}

	@Override
	public boolean validURI(URI source) {
		boolean result = super.validURI(source);
		if (!result) {
			result = isAndroidXMLSource(source);
		}

		return result;
	}

	private boolean isAndroidXMLSource(URI source) {
		return source != null && ContentResolver.SCHEME_ANDROID_RESOURCE.equals(source.getScheme());
	}

	@Override
	public StateMachineModel parseScxml(URI source) throws ConfigurationException, IOException, SCXMLParserException {

		StateMachineModel result = null;
		if (isAndroidXMLSource(source)) {
			try {
				Log.d(FSMServiceImpl.FSM, "Init android parse scxml, uri: " + source);
				XmlPullParser xpp = getXpp(source);
				Log.d(FSMServiceImpl.FSM, "Parse scxml, xpp: " + xpp);
				result = this.parseXPP(xpp);

			} catch (Exception e) {
				throw new ConfigurationException("Error getting xml resource, source: " + source, e);
			}
		} else {
			result = super.parseScxml(source);
		}
		return result;
	}

	protected XmlPullParser getXpp(URI source) throws FileNotFoundException, XmlPullParserException {
		InputStream is = this.androidContext.getApplicationContext().getContentResolver()
				.openInputStream(Uri.parse(source.toString()));
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(is, "UTF-8");
		return xpp;
	}

	/*	protected int getXMLId(String name) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
			int xmlId;
			Class<?> clazz = MetadataUtil.classFromName(R$XML, this.androidContext);
			Field field = clazz.getField(name);

			xmlId = field.getInt(null);
			return xmlId;
		}
	*/
	@Override
	protected Executable createAssignByValue(String location, String value) {
//		Log.d("SCXML", "parsing android assign");
		return AndroidAssign.assignByValue(location, value);
	}

	@Override
	protected Executable createAssignByExpression(String location, String expr) {
//		Log.d("SCXML", "parsing android assign");
		return AndroidAssign.assignByExpression(location, expr);
	}

}
