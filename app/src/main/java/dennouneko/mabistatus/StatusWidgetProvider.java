package dennouneko.mabistatus;
import android.app.Service;
import android.appwidget.*;
import android.content.*;
import java.security.*;
import android.os.*;
import android.widget.*;
import android.app.*;
import java.util.*;
import android.view.*;
import android.widget.RemoteViewsService.*;
import android.widget.RemoteViews.*;
import org.apache.http.client.*;
import java.io.*;
import junit.framework.*;
import android.net.*;
import android.util.*;
import android.nfc.*;
import android.net.wifi.*;
import android.preference.*;

public class StatusWidgetProvider extends AppWidgetProvider
{
	private static final String tag = "StatusWidgetProvider";
	private static int mOldStatus = -1;
	
	static PowerManager mPowerManager = null;
	static PowerManager.WakeLock mWakeLock = null;
	static WifiManager.WifiLock mWifiLock = null;
	static public final int INTERVAL1 = 1 * 60 * 1000;
	static public final int INTERVAL5 = 5 * 60 * 1000;
	static public final int INTERVAL30 = 30 * 60 * 1000;
	static private int mInterval = INTERVAL5;
	
	public static void setAlarm(Context context)
	{
		// prepare update event
		PendingIntent update = getUpdateIntent(context);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(update); // just to make sure we aren't adding infinite alarms
		long at = System.currentTimeMillis() + mInterval;
		mgr.set(AlarmManager.RTC_WAKEUP, at, update);
	}
	
	public static PendingIntent getUpdateIntent(Context context)
	{
		// unified update intent
		// to make sure the AlarmMsnager
		// can find and cancel event
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName providerName = new ComponentName(context, StatusWidgetProvider.class);
		int[] ids = manager.getAppWidgetIds(providerName);
		Intent intent = new Intent(context, StatusWidgetProvider.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	
	@Override
	public void onEnabled(Context context)
	{
		// initial update
		Log.d(tag, "onEnabled");
		setAlarm(context);
		updateAllWidgets(context);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.v(tag, "onUpdate");
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean upsleep = pref.getBoolean(ConfigActivity.KEY_PREF_SLEEP_UPDATES, false);
		boolean wifi = pref.getBoolean(ConfigActivity.KEY_PREF_WIFI, true);
		boolean isMobile = MainActivity.isMobile(context);
		// start update, respecting user settings
		if((upsleep || PowerState.isActive()) && (!wifi || !isMobile))
		{
			try
			{
				// WakeLock - making sure cpu won't go to sleep
				// while we're updating
				if(mPowerManager == null)
				{
					mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				}
				if(mPowerManager != null && mWakeLock == null)
				{
					mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Widget update");
				}
				if(mWakeLock != null)
				{
					mWakeLock.acquire();
				}
				
				// WifiLock - keeping connection alive
				// if possible
				WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				if(wifiMgr != null && mWifiLock == null)
				{
					mWifiLock = wifiMgr.createWifiLock(tag + ".WifiLock");
				}
				if(mWifiLock != null)
				{
					mWifiLock.acquire();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			// perform actual update in separate thread
			new WidgetUpdater(context).execute();
		}
		else
		{
			Log.d(tag, "Ignoring update");
			if(!upsleep && !PowerState.isActive())
				Log.d(tag, "sleep mode");
			
			if(wifi && isMobile)
				Log.d(tag, "mobile connection");
				
		}
		// schedule another update, in case something went wrong
		setAlarm(context);
	}
	
	public static void updateAllWidgets(Context ctx)
	{
		// force update, ignoring settings
		Log.v(tag, "Forcing widget update");
		new WidgetUpdater(ctx).execute();
	}
	
	public static int getCurStatus()
	{
		return mOldStatus;
	}
	
	private static void setCurStatus(int s)
	{
		mOldStatus = s;
	}
	
	private static class WidgetUpdater extends AsyncTask<Void, Void, Integer>
	{
		private Context mCtx;
		private static final String tag = "StatusWidgetProvider$WidgetUpdater";

		@Override
		protected void onPreExecute()
		{
			RemoteViews updateViews = new RemoteViews(mCtx.getPackageName(), R.layout.status_appwidget);
			updateViews.setTextViewText(R.id.status_login, "...");
			updateView(updateViews);
		}
		
		public WidgetUpdater(Context ctx)
		{
			mCtx = ctx;
		}

		@Override
		protected Integer doInBackground(Void[] param)
		{
			int patch = 0;
			int login = 0;
			
			// retry ip to 5 times, in case
			// network needs time for startup
			for(int t = 0; t < 5; t++)
			{
				if(t > 0)
				{
					try
					{
						Thread.sleep(1000);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				if(MainActivity.isConnected(mCtx))
				{
					MyHTTP http = MyHTTP.getInstance();
					patch = http.getPatchStatus(mCtx) & 0x0f;
					login = http.getLoginStatus(mCtx) & 0x0f;
				}
				else
				{
					Log.d(tag, "Not connected!");
				}//*/
				if(patch != 0x0f || login != 0x0f)
				{
					// at least one of replies was valid
					break;
				}
			}
			return patch | (login << 4);
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			Log.v(tag, "onPostExecute(" + Integer.toHexString(result) + ")");
			int res = R.string.status_offline;
			int patch = (result >> 0) & 0x0f;
			int login = (result >> 4) & 0x0f;
			int status = 0;
			
			// analyze result
			if((login > 0 && login < 0x0f && patch == 1) || (patch > 0 && patch < 0x0f && login == 1))
			{
				// at least one of servers is in maint mode
				res = R.string.status_maint;
				status = 1;
				mInterval = INTERVAL5;
			}
			else if(patch == 2 && login == 2)
			{
				// both servers are alive
				res = R.string.status_online;
				status = 2;
				mInterval = INTERVAL30;
			}
			else if(patch == 0x0f || login == 0x0f)
			{
				// Something went wrong...
				mInterval = INTERVAL1;
			}
			// update the timer
			setAlarm(mCtx);
			
			// push result to the widget
			String resultText = mCtx.getResources().getText(res).toString();
			
			int oldStatus = getCurStatus();
			
			if(oldStatus != status)
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
				boolean notify = pref.getBoolean(ConfigActivity.KEY_PREF_NOTIFY, false);
				
				// check if we have anything to notify about,
				// respecting settings
				if(notify)
				{
					if(oldStatus == 2 && status == 1)
					{
						// online -> maint
						MainActivity.notifyStatus(mCtx, R.string.message_maintenance);
					}
					else if(oldStatus == 1 && status == 2)
					{
						// maint -> online
						MainActivity.notifyStatus(mCtx, R.string.message_online);
					}
				}
				
				if(status > 0)
				{
					setCurStatus(status);
				}
			}
			
			RemoteViews updateViews = new RemoteViews(mCtx.getPackageName(), R.layout.status_appwidget);
			updateViews.setTextViewText(R.id.status_login, resultText);

			updateView(updateViews);
			
			// cpu can go to sleep now
			// release any locks we've acquired
			if(mWifiLock != null)
			{
				mWifiLock.release();
				mWifiLock = null;
			}
			if(mWakeLock != null)
			{
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		
		private void updateView(RemoteViews data)
		{
			Intent intent = new Intent(mCtx, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 0, intent, 0);
			data.setOnClickPendingIntent(R.id.widget_content, pendingIntent);

			// push the update to home screen
			ComponentName thisWidget = new ComponentName(mCtx, StatusWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(mCtx);
			manager.updateAppWidget(thisWidget, data);
		}
	}
}
