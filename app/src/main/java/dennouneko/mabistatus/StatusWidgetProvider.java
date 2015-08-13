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

public class StatusWidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		context.startService(new Intent(context, UpdateService.class));
		
		/* RemoteViews updateViews = UpdateService.buildUpdate(context);

		// push the update to home screen
		ComponentName thisWidget = new ComponentName(context, StatusWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews); //*/
	}
	
	public static class UpdateService extends Service
	{
		@Override
		public void onStart(Intent intent, int startId)
		{
			// build the update
			RemoteViews updateViews = buildUpdate(this);
			
			// push the update to home screen
			ComponentName thisWidget = new ComponentName(this, StatusWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, updateViews);
		}
		
		public static RemoteViews buildUpdate(Context context)
		{
			RemoteViews updateViews = null;
			
			updateViews = new RemoteViews(context.getPackageName(), R.layout.status_appwidget);
			
			int scode = (new Random().nextInt(3));
			String sval = context.getResources().getText(R.string.status_offline).toString();
			if(scode == 1)
				sval = context.getResources().getText(R.string.status_maint).toString();
			else if(scode == 2)
				sval = context.getResources().getText(R.string.status_online).toString();
		
			updateViews.setTextViewText(R.id.status_login, sval);

			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			updateViews.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
			
			return updateViews;
		}

		@Override
		public IBinder onBind(Intent p1)
		{
			// no need to bind to this service
			return null;
		}
	}
}
