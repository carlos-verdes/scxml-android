package com.nosolojava.android.fsm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.nosolojava.android.fsm.io.AndroidBroadcastIOProcessor;
import com.nosolojava.android.fsm.io.FSM_EXTRAS;
import com.nosolojava.android.fsm.service.FSMServiceImpl;
import com.nosolojava.fsm.android.R;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;

public class AndroidUtils {
	public static final String CLASS_SCHEME = "class";
	public static final String ACTION_SCHEME = "action";

	private static final String LOG_TAG = FSMServiceImpl.FSM;

	//	private static final String LOG_TAG = FSMServiceImpl.FSM;

	public static void loadImageFromUrls(String url, ImageView... views) {

		LoadImageTask task = new LoadImageTask(views);
		task.execute(url);

	}

	public static void loadImageFromUrls(String url, Set<ImageView> views) {
		LoadImageTask task = new LoadImageTask(views);
		task.execute(url);

	}

	protected static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private final Set<ImageView> views;

		public LoadImageTask(Set<ImageView> views) {
			super();
			this.views = new HashSet<ImageView>();
			this.views.addAll(views);
		}

		public LoadImageTask(ImageView... views) {
			super();
			this.views = new HashSet<ImageView>();
			for (ImageView aux : views) {
				this.views.add(aux);
			}
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = downloadImage(params[0]);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			for (ImageView view : views) {
				view.setImageBitmap(result);
			}
		}

	}

	public static String getText(String message, android.content.Context androidContext) {
		if (message.startsWith("@")) {
			String[] aux = message.substring(1).split("/");
			String resource = aux[0];
			String name = aux[1];
			Resources resources = androidContext.getResources();
			int id = resources.getIdentifier(name, resource, MetadataUtil.getAppPackage(androidContext));
			message = resources.getString(id);
		}
		return message;
	}

	// Creates Bitmap from InputStream and returns it
	public static Bitmap downloadImage(String url) {
		Bitmap bitmap = null;
		InputStream stream = null;
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;

		try {
			stream = getHttpConnection(url);
			bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
			stream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return bitmap;
	}

	private static InputStream getHttpConnection(String urlString) throws IOException {
		InputStream stream = null;
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();

		try {
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			httpConnection.setRequestMethod("GET");
			httpConnection.connect();

			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				stream = httpConnection.getInputStream();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return stream;
	}

	public static AlertDialog createAlertDialog(final Activity activity, int titleRes, int messageRes) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

		// set title
		alertDialogBuilder.setTitle(titleRes);

		// set dialog message
		alertDialogBuilder.setMessage(messageRes).setPositiveButton(R.string.close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						activity.finish();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		return alertDialog;
	}

	public static void addContentToIntent(Intent intent, Object body) {
		if (body != null) {
			if (Parcelable.class.isAssignableFrom(body.getClass())) {
				addContentToIntent(intent, (Parcelable) body);
			} else if (Serializable.class.isAssignableFrom(body.getClass())) {
				addContentToIntent(intent, (Serializable) body);
			} else{
				Log.w(LOG_TAG,String.format("Intent should be Parcelable or Serializable: %s",body));
			}
		}
	}

	public static void addContentToIntent(Intent intent, Serializable body) {
		intent.putExtra(FSM_EXTRAS.CONTENT.toString(), body);

	}

	public static void addContentToIntent(Intent intent, Parcelable body) {
		intent.putExtra(FSM_EXTRAS.CONTENT.toString(), body);
	}

	public static void addSessionToFilter(IntentFilter intentFilter, String sessionId) {
		URI uri = AndroidBroadcastIOProcessor.getLocationStatic(sessionId);
		intentFilter.addDataScheme(uri.getScheme());
		intentFilter.addDataAuthority(uri.getAuthority(), null);
	}

	/**
	 * Used to create intents inside the fsm using uris. <br/>
	 * It create the intent from {@link InvokeInfo#getSource()} uri.
	 * <p>
	 * There are two types of uri's accepted:
	 * <ul>
	 * <li><b>action</b>:your_intent_action. <br/>
	 * Example: {@code action:com.nosolojava.mock.START_SERVICE_ACTION}
	 * <li><b>class</b>:your_target_class_name. <br/>
	 * Example {@code class:com.service.YourService} will be equivalent to
	 * {@code new Intent(androidContext, com.service.YourService.class)}
	 * </ul>
	 * <p>
	 * The intent data has the return Uri so the called service could answer the fsm using the {@link AndroidBroadcastIOProcessor#sendMessageToFSM(Context, android.net.Uri, String, Object)}. <br/>
	 * 
	 * @return
	 */
	public static Intent createIntentForExternalServices(Context androidContext, URI uri, com.nosolojava.fsm.runtime.Context fsmContext) {
		Intent bindingIntent = null;

		String scheme = uri.getScheme();

		if (ACTION_SCHEME.equals(scheme)) {
			bindingIntent = createImplicitIntentFromURI(uri);
		} else if (CLASS_SCHEME.equals(scheme)) {
			bindingIntent = createExplicitIntentFromURI(androidContext, uri);
		}

		if (bindingIntent != null) {
			//set the return uri
			String sessionId= fsmContext.getSessionId();
			bindingIntent.setData(AndroidBroadcastIOProcessor.getAndroidLocationStatic(sessionId));

		} else {
			Log.w(LOG_TAG, "Error creating intent from uri, message src: " + uri);
		}
		return bindingIntent;
	}

	public static Intent createExplicitIntentFromURI(Context androidContext, URI messageSource) {
		Intent bindingIntent = null;
		//class:com.nosolojava.service.YourService
		String serviceClassName = messageSource.getSchemeSpecificPart();
		@SuppressWarnings("rawtypes")
		Class clazz = null;
		try {
			clazz = Class.forName(serviceClassName);
		} catch (ClassNotFoundException e) {
			// TODO manage error when passing class
			Log.e(LOG_TAG, String.format("Error invoking system service, class not found exception, class: %s", serviceClassName), e);
		}

		if (clazz != null) {
			bindingIntent = new Intent(androidContext, clazz);
		}
		return bindingIntent;
	}

	public static Intent createImplicitIntentFromURI(URI messageSource) {
		Intent bindingIntent;
		//action:com.nosolojava.Service
		String action = messageSource.getSchemeSpecificPart();

		bindingIntent = new Intent(action);
		return bindingIntent;
	}


	public static String getFSMSessionFromFSMIntent(Intent intent) {
		Uri fsmURI=intent.getData();
		return getFSMSessionFromUri(fsmURI);
		
	}

	public static String getFSMSessionFromUri(Uri fsmURI) {
		String result = null;

		if (fsmURI != null) {
			result = fsmURI.getAuthority();
		}
		return result;
		
	}

	public static String getFSMSessionFromUri(URI fsmURI) {
		String result = null;

		if (fsmURI != null) {
			result = fsmURI.getAuthority();
		}
		return result;
	}

	
}
