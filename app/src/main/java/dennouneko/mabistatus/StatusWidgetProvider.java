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
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.v(tag, "onUpdate");
		new MyTask(context).execute();
	}
	
	public static void updateAllWidgets(Context ctx)
	{
		Log.v(tag, "Forcing widget update");
		new MyTask(ctx).execute();
	}
	
	private static class MyTask extends AsyncTask<Void, Void, Integer>
	{
		Context mCtx;
		public MyTask(Context ctx)
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
				login = 2; // For now pretend it's online
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
			
			if((login > 0 && patch == 1) || (patch > 0 && login == 1))
			{
				res = R.string.status_maint;
			}
			else if(patch == 2 && login == 2)
			{
				res = R.string.status_online;
			}
			String resultText = mCtx.getResources().getText(res).toString();
			
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
