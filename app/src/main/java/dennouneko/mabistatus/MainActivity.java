package dennouneko.mabistatus;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.media.*;
import android.view.View.*;

public class MainActivity extends Activity 
{
	private static final String tag = "MainActivity";
	public static final int mIdStatus = 1;
	
	private void doTest()
	{
		notifyStatus(getApplicationContext(), "Server went Online");
	}
	
	public static void notifyStatus(Context ctx, String message)
	{
		Notification.Builder noti = new Notification.Builder(ctx)
			.setContentTitle("Mabinogi Server Status")
			.setContentText(message)
			.setSmallIcon(R.drawable.mabisign);
		
		noti.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		noti.setAutoCancel(true);
		
		Intent showIntent = new Intent(ctx, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, showIntent, 0);
		
		noti.setContentIntent(contentIntent);
		
		NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(mIdStatus, noti.build());
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        updateContent();
		int[] items = {R.id.daily_today_tara, R.id.daily_today_tara_content,
					R.id.daily_today_tail, R.id.daily_today_tail_content,
					R.id.daily_tomorrow_tara, R.id.daily_tomorrow_tara_content,
					R.id.daily_tomorrow_tail, R.id.daily_tomorrow_tail_content};
		makeToggleGroup(items);
    }
	
	private void makeToggle(int elem, int content)
	{
		View src = findViewById(elem);
		final View dst = findViewById(content);
		
		src.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if(dst.getVisibility() == View.VISIBLE)
				{
					dst.setVisibility(View.GONE);
				}
				else
				{
					dst.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void makeToggleGroup(final int[] items)
	{
		for(int j = 0; j < items.length; j += 2)
		{
			final int elem = items[j];
			findViewById(elem).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					for(int i = 0; i < items.length; i += 2)
					{
						if(elem != items[i])
						{
							findViewById(items[i+1]).setVisibility(View.GONE);
						}
						else
						{
							View itm = findViewById(items[i+1]);
							itm.setVisibility(itm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
						}
					}
				}
			});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_refresh:
				updateContent();
				return true;
			case R.id.menu_refresh_widget:
				StatusWidgetProvider.updateAllWidgets(this);
				return true;
			case R.id.menu_debug:
				doTest();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public static boolean isConnected(Context ctx)
	{
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	public static boolean isMobile(Context ctx)
	{
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		switch(info.getType())
		{
		case ConnectivityManager.TYPE_MOBILE:
			return true;
		default:
			return false;
		}
	}
	
	public void updateContent()
	{
		TextView t = (TextView)findViewById(R.id.message);
		t.setText(isConnected(this) ? (isMobile(this) ? "Mobile" : "Online") : "Offline");
	}
}
