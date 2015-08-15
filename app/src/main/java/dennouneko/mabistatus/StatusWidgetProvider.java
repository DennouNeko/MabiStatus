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
	private static final String tag = "StstusWidgetProvider";
	private static final String patch_url = "http://php-dennouneko.rhcloud.com/proxy.php?type=patch";
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.v(tag, "onUpdate");
		new MyTask(context).execute(patch_url);
	}
	
	public static void updateAllWidgets(Context ctx)
	{
		Log.v(tag, "Forcing widget update");
		new MyTask(ctx).execute(patch_url);
	}
	
	private static class MyTask extends AsyncTask<String, Void, String>
	{
		Context mCtx;
		public MyTask(Context ctx)
		{
			mCtx = ctx;
		}

		@Override
		protected String doInBackground(String[] p1)
		{
			String ret = "";
			if(MainActivity.isConnected(mCtx))
			{
			try
			{
				ret = MyHTTP.getInstance().getData(p1[0]);
			}
			catch(ClientProtocolException e)
			{
				ret = "CPE:" + e.getMessage();
			}
			catch(IOException e)
			{
				ret = "IO:" + e.getMessage();
			}//*/
			}
			else
			{
				Log.d(tag, "Not connected!");
				ret = "Net";
			}//*/
			return ret;
		}

		@Override
		protected void onPostExecute(String result)
		{
			String parts[] = result.split("\n");
			HashMap<String, String> data = new HashMap<String, String>();
			
			for(String line : parts)
			{
				String pt[] = line.trim().split("=", 2);
				if(pt.length > 1) data.put(pt[0], pt[1]);
			}
			
			String resultText = mCtx.getResources().getText(R.string.status_offline).toString();
			Log.v(tag, "Response:\n" + result);
			
			String patchAccept = data.get("patch_accept");
			if(patchAccept != null)
			{
				Log.d(tag, "patch_accept = " + patchAccept);
				if(patchAccept.equals("0"))
				{
					resultText = mCtx.getResources().getText(R.string.status_maint).toString();
				}
				else if(patchAccept.equals("1"))
				{
					resultText = mCtx.getResources().getText(R.string.status_online).toString();
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
		}
	}
}
