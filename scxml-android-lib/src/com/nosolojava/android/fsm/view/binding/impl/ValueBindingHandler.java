package com.nosolojava.android.fsm.view.binding.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.nosolojava.android.fsm.bean.AssignData;
import com.nosolojava.android.fsm.bean.AssignDataSerializable;
import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_ACTIONS;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.util.AndroidUtils;

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

	private Map<String, Set<TextView>> textValueBindingMap = new ConcurrentHashMap<String, Set<TextView>>();
	private Map<String, Set<SmartImageView>> smartImageValueBindingMap = new ConcurrentHashMap<String, Set<SmartImageView>>();
	private Map<String, Set<ImageView>> normalImageValueBindingMap = new ConcurrentHashMap<String, Set<ImageView>>();
	private Map<EditText, TextWatcher> textWatchers = new ConcurrentHashMap<EditText, TextWatcher>();

	public static String VALUE_ATTRIBUTE = "value";
	public static final String INIT_EVENT = "view.valueBinding.init";
	protected static final String NEW_VAL_EVENT = "view.valueBinding.newVal";
	private static final String LOG_TAG = "ValueBind";

	private AtomicBoolean userHasEdited = new AtomicBoolean(false);

	public ValueBindingHandler() {
		super();
	}

	@Override
	public Class<? extends View> getViewClass() {
		return TextView.class;
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
							Serializable data = new AssignDataSerializable(location, value);
							ValueBindingHandler.this.userHasEdited.set(true);
							AndroidBroadcastIOProcessor.sendMessageToFSM(currentActivity, fsmSessionId, NEW_VAL_EVENT,
									data);

						} else {
							Log.d("TEST", "onTextChanged " + editView.getId() + " : |" + s.toString()
									+ "| IS THE SAME!! don't fire event");

						}

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						String value = s != null ? s.toString() : "NULL";
						this.lastString = value;

						//						Log.d("TEST", "beforeTextChanged " + editView.getId() + " : |" + s.toString() + "|");
					}

					@Override
					public void afterTextChanged(Editable s) {

						//						Log.d("TEST",
						//								"afterTextChanged: " + editView.getId() + "|" + s.toString() + "|, length: "
						//										+ s.length() + ", drawableState: " + editView.getDrawableState()
						//										+ ", editableText: |" + editView.getEditableText() + "|, freeze?: "
						//										+ editView.getFreezesText() + ", text: |" + editView.getText() + "|");

					}
				};
				this.textWatchers.put(editView, watcher);

			}

		}
	}

	@Override
	public void onInitActivity(Activity activity) {

		this.currentActivity = activity;

	}

	@Override
	public void onBind(Activity activity, String fsmSessionId) {
		super.onBind(activity, fsmSessionId);

		for (Entry<EditText, TextWatcher> entry : this.textWatchers.entrySet()) {
			entry.getKey().addTextChangedListener(entry.getValue());
		}

		CopyOnWriteArraySet<String> locations = new CopyOnWriteArraySet<String>(textValueBindingMap.keySet());
		locations.addAll(this.smartImageValueBindingMap.keySet());
		locations.addAll(this.normalImageValueBindingMap.keySet());
		Intent intent = new Intent(FSM_ACTIONS.INIT_FSM_ASSIGN.toString());
		intent.putExtra(FSM_EXTRAS.SESSION_ID.toString(), fsmSessionId);
		intent.putExtra(FSM_EXTRAS.CONTENT.toString(), locations);
		
		//send event to fsm service to get the current datamodel info
		activity.startService(intent);

	}

	@Override
	public void onUnbind(Activity activity, String fsmSessionId) {

		for (Entry<EditText, TextWatcher> entry : this.textWatchers.entrySet()) {
			entry.getKey().removeTextChangedListener(entry.getValue());

		}

		super.onUnbind(activity, fsmSessionId);

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
		if (FSM_ACTIONS.FSM_ASSIGN.toString().equals(intent.getAction())) {
			handled = true;
			AssignData assignData = (AssignData) intent.getExtras().get(FSM_EXTRAS.CONTENT.toString());

			String location = assignData.getName();

			if (this.smartImageValueBindingMap.containsKey(location)) {

				loadSmartImages(assignData, location);

			}
			if (this.normalImageValueBindingMap.containsKey(location)) {

				loadNormalImages(assignData, location);

			}
			if (this.textValueBindingMap.containsKey(location)) {
				Set<TextView> views = this.textValueBindingMap.get(location);
				String stringValue = extractStringValue(assignData);

				String currentValue;
				for (TextView view : views) {
					currentValue = view.getText().toString();

					//avoid changes if the value is the same
					if (!currentValue.equals(stringValue)) {
						//avoid if this control has focus and the user has already edit (this is necesary due to a bug in textwatcher which sends more events than expected
						if (!view.hasFocus() || !userHasEdited.get()) {

							//remove text watcher to avoid loops
							TextWatcher watcher = this.textWatchers.get(view);

							if (watcher != null) {
								view.removeTextChangedListener(watcher);
							}

							Log.d("TEST", "!!! CHANGED!!! oldVal: |" + currentValue + "|, newVal: |" + stringValue
									+ "|");
							//update value
							view.setText(stringValue);

							//add again the watcher
							if (watcher != null) {
								view.addTextChangedListener(watcher);
							}
						}
					}
				}
			}
		}

		return handled;
	}

	protected void loadNormalImages(AssignData assignData, String location) {
		Set<ImageView> imageViews = this.normalImageValueBindingMap.get(location);
		String uri = extractStringValue(assignData);

		try {
			AndroidUtils.loadImageFromUrls(uri, imageViews);

		} catch (Exception e) {
			Log.w(LOG_TAG, "Error downloading image", e);
		}
	}

	protected void loadSmartImages(AssignData assignData, String location) {
		Set<SmartImageView> imageViews = this.smartImageValueBindingMap.get(location);
		String uri = extractStringValue(assignData);
		for (SmartImageView sImageView : imageViews) {
			sImageView.setImageUrl(uri);
		}
	}

	protected String extractStringValue(AssignData assignData) {
		Object value = assignData.getValue();
		if (value == null) {
			value = "";
		}

		String stringValue = value.toString();
		stringValue = AndroidUtils.getText(stringValue, currentActivity);
		return stringValue;
	}
}
