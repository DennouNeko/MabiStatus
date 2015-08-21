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
import java.util.*;
import org.json.*;

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
		// t.setText(isConnected(this) ? (isMobile(this) ? "Mobile" : "Wideband") : "Disconnected");
		(new MainUpdater(this)).execute();
	}
	
	private class MainUpdater extends AsyncTask<Void, Void, JSONObject >
	{
		private Context mCtx;
		private static final String tag = "MainActivity$MainUpdater";

		@Override
		protected void onPreExecute()
		{
			TextView t = (TextView)findViewById(R.id.message);
			t.setVisibility(View.VISIBLE);
			t.setText(R.string.message_loading);
		}
		
		public MainUpdater(Context ctx)
		{
			mCtx = ctx;
		}
		
		@Override
		protected JSONObject doInBackground(Void[] p1)
		{
			JSONObject data = null;
			
			if(isConnected(mCtx))
			{
				MyHTTP http = MyHTTP.getInstance();
				data = http.getDailyInfo(mCtx);
			}
			else
			{
				Log.d(tag, "No connected!");
			}
			return data;
		}
		
		@Override
		protected void onPostExecute(JSONObject result)
		{
			TextView t = (TextView)findViewById(R.id.message);
			try
			{
				if(result != null)
				{
					// t.setText(result.getString("result"));
					if(result.isNull("error"))
					{
						// t.setVisibility(View.GONE);
						t.setText(result.getString("date"));
						if(!result.isNull("today"))
						{
							JSONObject daily = (JSONObject)result.get("today");
							JSONObject tara = (JSONObject)daily.get("Tara");
							JSONObject tail = (JSONObject)daily.get("Taillteann");
							((TextView)findViewById(R.id.daily_today_tara)).setText(tara.getString("Normal"));
							((TextView)findViewById(R.id.daily_today_tail)).setText(tail.getString("Normal"));
						}
						if(!result.isNull("tomorrow"))
						{
							JSONObject daily = (JSONObject)result.get("tomorrow");
							JSONObject tara = (JSONObject)daily.get("Tara");
							JSONObject tail = (JSONObject)daily.get("Taillteann");
							((TextView)findViewById(R.id.daily_tomorrow_tara)).setText(tara.getString("Normal"));
							((TextView)findViewById(R.id.daily_tomorrow_tail)).setText(tail.getString("Normal"));
						}
					}
					else
					{
						t.setText(result.getString("error"));
					}
				}
				else
				{
					t.setText("Offline");
				}
			}
			catch(JSONException e)
			{
				t.setText(e.getMessage());
			}
		}
	}
}
