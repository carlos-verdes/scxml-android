package com.nosolojava.android.fsm.view.binding.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.nosolojava.android.fsm.bean.AssignParcelableString;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.util.AndroidUtils;
import com.nosolojava.fsm.runtime.ContextInstance;

/**
 * <p>
 * This handler bind FSM context data with TextViews.
 * 
 * <p>
 * When a new value is changed in the FSM with the action "assign" then an event ("controller.assign.newValue") is sent
 * by the FSM. This event is handled by this class which updates the proper TextView (with a
 * view.setText(newVal.toString()) calling).
 * 
 * <p>
 * On init an event ("view.valueBinding.init") is sent to FSM in order to receive the current context data.
 * 
 * <p>
 * When the TextView changes then an event ("view.valueBinding.newVal") is sent to the FSM to update the context value.
 * 
 * @author Carlos Verdes
 * 
 */
public class ValueBindingHandler extends AbstractFSMViewBindingHandler {

	public static String VALUE_ATTRIBUTE = "value";
	public static final String INIT_EVENT = "view.valueBinding.init";
	protected static final String NEW_VAL_EVENT = "view.valueBinding.newVal";
	private static final String LOG_TAG = "ValueBind";

	private Map<String, Set<TextView>> textValueBindingMap = new ConcurrentHashMap<String, Set<TextView>>();
	private Map<String, Set<SmartImageView>> smartImageValueBindingMap = new ConcurrentHashMap<String, Set<SmartImageView>>();
	private Map<String, Set<ImageView>> normalImageValueBindingMap = new ConcurrentHashMap<String, Set<ImageView>>();
	private Map<EditText, TextWatcher> textWatchers = new ConcurrentHashMap<EditText, TextWatcher>();

	private ContextInstance lastContextInstance = null;

	private AtomicBoolean userHasEdited = new AtomicBoolean(false);

	public ValueBindingHandler() {
		super();

	}

	@Override
	public void reset() {
		this.textValueBindingMap.clear();
		this.textWatchers.clear();

		this.smartImageValueBindingMap.clear();
		this.normalImageValueBindingMap.clear();
	}

	protected <T extends View> void registerViewBinding(final T view, final String location) {

		if (SmartImageView.class.isAssignableFrom(view.getClass())) {

			Set<SmartImageView> imageViews;

			if (this.smartImageValueBindingMap.containsKey(location)) {
				imageViews = this.smartImageValueBindingMap.get(location);
			} else {
				imageViews = new CopyOnWriteArraySet<SmartImageView>();
				this.smartImageValueBindingMap.put(location, imageViews);
			}
			imageViews.add((SmartImageView) view);

		} else if (ImageView.class.isAssignableFrom(view.getClass())) {

			ImageView iview = (ImageView) view;

			Set<ImageView> imageViews;
			if (this.normalImageValueBindingMap.containsKey(location)) {
				imageViews = this.normalImageValueBindingMap.get(location);
			} else {
				imageViews = new CopyOnWriteArraySet<ImageView>();
				this.normalImageValueBindingMap.put(location, imageViews);
			}

			imageViews.add(iview);

		} else if (TextView.class.isAssignableFrom(view.getClass())) {
			Set<TextView> views;

			if (this.textValueBindingMap.containsKey(location)) {
				views = this.textValueBindingMap.get(location);
			} else {
				views = new CopyOnWriteArraySet<TextView>();
				this.textValueBindingMap.put(location, views);
			}

			views.add((TextView) view);

			if (EditText.class.isAssignableFrom(view.getClass())) {

				final EditText editView = (EditText) view;
				TextWatcher watcher = new TextWatcher() {

					private String lastString = "1234123ASDFAS4~~~$$$";

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

						final String value = s != null ? s.toString() : "NULL";
						if (!value.equals(lastString.toString())) {

							Log.d("TEST", "Sending new val: " + location + "= |" + value + "|");
							Parcelable data = new AssignParcelableString(location, value);
							ValueBindingHandler.this.userHasEdited.set(true);
							AndroidBroadcastIOProcessor.sendMessageToFSM(fsmSessionId, currentActivity,
									fsmServiceClazz, NEW_VAL_EVENT, data);

						} else {
							Log.d("TEST", "onTextChanged " + editView.getId() + " : |" + s.toString()
									+ "| IS THE SAME!! don't fire event");

						}

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						String value = s != null ? s.toString() : "NULL";
						this.lastString = value;

						// Log.d("TEST", "beforeTextChanged " + editView.getId() + " : |" + s.toString() + "|");
					}

					@Override
					public void afterTextChanged(Editable s) {

						// Log.d("TEST",
						// "afterTextChanged: " + editView.getId() + "|" + s.toString() + "|, length: "
						// + s.length() + ", drawableState: " + editView.getDrawableState()
						// + ", editableText: |" + editView.getEditableText() + "|, freeze?: "
						// + editView.getFreezesText() + ", text: |" + editView.getText() + "|");

					}
				};
				this.textWatchers.put(editView, watcher);

			}

		}
	}

	@Override
	public void onBind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {
		super.onBind(activity, fsmServiceClazz, fsmSessionId);

		for (Entry<EditText, TextWatcher> entry : this.textWatchers.entrySet()) {
			entry.getKey().addTextChangedListener(entry.getValue());
		}

		CopyOnWriteArraySet<String> locations = new CopyOnWriteArraySet<String>(textValueBindingMap.keySet());
		locations.addAll(this.smartImageValueBindingMap.keySet());
		locations.addAll(this.normalImageValueBindingMap.keySet());

		this.lastContextInstance = null;
	}

	@Override
	public void onUnbind(Activity activity, Class<? extends Service> fsmServiceClazz, String fsmSessionId) {

		for (Entry<EditText, TextWatcher> entry : this.textWatchers.entrySet()) {
			entry.getKey().removeTextChangedListener(entry.getValue());

		}

		super.onUnbind(activity, fsmServiceClazz, fsmSessionId);

	}

	@Override
	public void registerXMLAttributeBinding(View view, String bindingAttribute, String bindingValue) {
		if (VALUE_ATTRIBUTE.equals(bindingAttribute)) {
			registerViewBinding(view, bindingValue);
		}
	}

	@Override
	public void registerXMLElementBinding(XmlPullParser xpp) {
	}

	@Override
	public boolean handleFSMIntent(Intent intent) {

		boolean handled = false;
		if (FSM_ACTIONS.FSM_NEW_SESSION_CONFIG.toString().equals(intent.getAction())) {
			handled = true;

			// get the new config
			ContextInstance newContextInstance = (ContextInstance) intent.getExtras()
					.get(FSM_EXTRAS.CONTENT.toString());

			updateView(newContextInstance);
		}

		return handled;
	}

	@Override
	public void updateView(ContextInstance newContextInstance) {
		// update smart images
		updateSmartImages(newContextInstance, lastContextInstance);

		// update normal images
		updateNormalImages(newContextInstance, lastContextInstance);

		// update text views
		updateTextViews(newContextInstance, lastContextInstance);

		this.lastContextInstance = newContextInstance;
	}

	private void updateSmartImages(ContextInstance newContextInstance, ContextInstance oldContextInstance) {
		String key;
		String newUrl;
		// for each smart image
		for (Entry<String, Set<SmartImageView>> entry : this.smartImageValueBindingMap.entrySet()) {
			key = entry.getKey();
			newUrl = newContextInstance.getDataByName(key);

			// if context resolves the key
			if (newUrl != null && !"".equals(newUrl)) {
				// check if the value has changed
				if (oldContextInstance == null || !oldContextInstance.getDataByName(key).equals(newUrl)) {
					// load new uris
					for (SmartImageView smartImageView : entry.getValue()) {
						smartImageView.setImageUrl(newUrl);
					}
				}
			}

		}

	}

	protected void updateNormalImages(ContextInstance newContextInstance, ContextInstance oldContextInstance) {
		String key;
		String newUrl;
		// for each normal image
		for (Entry<String, Set<ImageView>> entry : this.normalImageValueBindingMap.entrySet()) {
			key = entry.getKey();
			newUrl = newContextInstance.getDataByName(key);

			// if context resolves the key
			if (newUrl != null && !"".equals(newUrl)) {
				// check if the value has changed
				if (oldContextInstance == null || !oldContextInstance.getDataByName(key).equals(newUrl)) {
					// load new uris
					try {
						AndroidUtils.loadImageFromUrls(newUrl, entry.getValue());

					} catch (Exception e) {
						Log.w(LOG_TAG, "Error downloading image", e);
					}
				}
			}

		}

	}

	private void updateTextViews(ContextInstance newContextInstance, ContextInstance oldContextInstance) {

		String key;
		String oldValue;
		String newValue;
		// for each text view
		for (Entry<String, Set<TextView>> entry : this.textValueBindingMap.entrySet()) {
			key = entry.getKey();
			newValue = newContextInstance.getDataByName(key);

			// if context resolves the key
			if (newValue != null && !"".equals(newValue)) {
				// check if the value has changed

				if (oldContextInstance == null || oldContextInstance.getDataByName(key) == null
						|| !oldContextInstance.getDataByName(key).equals(newValue)) {
					oldValue = oldContextInstance != null ? (String) oldContextInstance.getDataByName(key) : null;

					// load new text value
					for (TextView textView : entry.getValue()) {
						if (!textView.hasFocus() || !this.userHasEdited.get()) {

							// remove text watcher to avoid loops
							TextWatcher watcher = this.textWatchers.get(textView);
							if (watcher != null) {
								textView.removeTextChangedListener(watcher);
							}

							Log.d("TEST", "!!! CHANGED!!! oldVal: |" + oldValue + "|, newVal: |" + newValue + "|");
							// update value
							textView.setText(newValue);

							// add again the watcher
							if (watcher != null) {
								textView.addTextChangedListener(watcher);
							}
						}

					}
				}
			}

		}

	}

}
