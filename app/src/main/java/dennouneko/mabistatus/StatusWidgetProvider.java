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

public class StatusWidgetProvider extends AppWidgetProvider
{
	private static final String tag = "StatusWidgetProvider";
	private static int mOldStatus = -1;
	
	static PowerManager mPowerManager = null;
	static PowerManager.WakeLock mWakeLock = null;
	static private int mInterval = 5 * 60 * 1000;
	
	public static void setAlarm(Context context)
	{
		PendingIntent update = getUpdateIntent(context);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(update);
		long at = System.currentTimeMillis() + mInterval;
		mgr.set(AlarmManager.RTC_WAKEUP, at, update);
	}
	
	public static PendingIntent getUpdateIntent(Context context)
	{
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
		Log.d(tag, "onEnabled");
		setAlarm(context);
		updateAllWidgets(context);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.v(tag, "onUpdate");
		try
		{
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		new WidgetUpdater(context).execute();
		setAlarm(context);
	}
	
	public static void updateAllWidgets(Context ctx)
	{
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
		
		public WidgetUpdater(Context ctx)
		{
			mCtx = ctx;
		}

		@Override
		protected Integer doInBackground(Void[] param)
		{
			int patch = 0;
			int login = 0;
			
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
			
			if((login > 0 && login < 0x0f && patch == 1) || (patch > 0 && patch < 0x0f && login == 1))
			{
				res = R.string.status_maint;
				status = 1;
			}
			else if(patch == 2 && login == 2)
			{
				res = R.string.status_online;
				status = 2;
			}
			String resultText = mCtx.getResources().getText(res).toString();
			
			int oldStatus = getCurStatus();
			
			if(oldStatus != status)
			{
				if(oldStatus == 2 && status == 1)
				{
					MainActivity.notifyStatus(mCtx, R.string.message_maintenance);
				}
				else if(oldStatus == 1 && status == 2)
				{
					MainActivity.notifyStatus(mCtx, R.string.message_online);
				}
				
				if(status > 0)
				{
					setCurStatus(status);
				}
			}
			
			RemoteViews updateViews = new RemoteViews(mCtx.getPackageName(), R.layout.status_appwidget);
			updateViews.setTextViewText(R.id.status_login, resultText);

			Intent intent = new Intent(mCtx, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 0, intent, 0);
			updateViews.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
			
			// push the update to home screen
			ComponentName thisWidget = new ComponentName(mCtx, StatusWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(mCtx);
			manager.updateAppWidget(thisWidget, updateViews);
			
			if(mWakeLock != null)
			{
				mWakeLock.release();
				mWakeLock = null;
			}
		}
	}
}
