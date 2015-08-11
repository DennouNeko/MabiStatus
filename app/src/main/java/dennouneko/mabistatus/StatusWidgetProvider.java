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

public class StatusWidgetProvider extends AppWidgetProvider
{

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		ComponentName thisWidget = new ComponentName(context, StatusWidgetProvider.class);
		int allWidgetIds[] = appWidgetManager.getAppWidgetIds(thisWidget);
		for(int widgetId : allWidgetIds)
		{
			int number = (new Random().nextInt(100));
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.status_appwidget);
			
			// TODO: update remoteViews
			remoteViews.setTextViewText(R.id.status_patch, String.valueOf(number));
			
			Intent intent = new Intent(context, StatusWidgetProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
	
	public void showMain(View v)
	{
	}
}
