package com.nosolojava.android.fsm.view.binding;

import org.xmlpull.v1.XmlPullParser;

import android.view.View;

/**
 * Binding handler based on xml layouts.
 * 
 * @author Carlos Verdes
 * 
 */
public interface XPPFSMViewBindingHandler extends FSMViewBindingHandler {

	/**
	 * This is used when parsing the xml layout to identify if the element or attribute is relevant for this handler.
	 * 
	 * @return
	 */
	String getNamespace();

	/**
	 * Register a binding based on an xml attribute.
	 * 
	 * @param view
	 *            view to be bound
	 * @param bindingAttribute
	 *            xml attribute name
	 * @param bindingValue
	 *            xml attribute value
	 */
	void registerXMLAttributeBinding(View view, String bindingAttribute, String bindingValue);

	/**
	 * Register a binding based on an xml element.
	 * <p>
	 * For performance and flexibility it's been decided to pass the xpp directly pointing to the element.
	 * <p>
	 * It means that <strong>there should NOT be two handlers with he same combination of namespace + elementTagName
	 * </strong>.
	 * 
	 * @param xpp
	 */
	void registerXMLElementBinding(XmlPullParser xpp);

}
