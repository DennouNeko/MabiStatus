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
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;
import android.content.res.*;
import android.text.style.*;
import android.widget.AbsoluteLayout.*;
import java.text.*;

public class MainActivity extends Activity 
{
	private static final String tag = "MainActivity";
	public static final int mIdStatus = 1;
	public static final String serverTimezone = "America/Los_Angeles";
	private String mReqDate;
	
	private DiskCache cacheDaily = new DiskCache(this);
	
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
		
		// Intent showIntent = new Intent(ctx, MainActivity.class);
		Intent showIntent = new Intent();
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
        updateContent(false);
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
				updateContent(true);
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
	
	private RelativeLayout.LayoutParams makeBelow(int id)
	{
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.BELOW, id);
		return param;
	}

	private void updateDetails(RelativeLayout view, MissionInfo data)
	{
		view.removeAllViewsInLayout();
		TextView players = new TextView(this);
		players.setId(view.generateViewId());
		players.setText("Party size: " + data.getPlayers());
		view.addView(players);

		TextView time = new TextView(this);
		time.setLayoutParams(makeBelow(players.getId()));
		time.setId(view.generateViewId());
		time.setText((data.isTimeLimit() ? "Time limit: " : "Time: ") + data.getTime());
		view.addView(time);

		TableLayout rewards = new TableLayout(this);
		RelativeLayout.LayoutParams rewardsParams = makeBelow(time.getId());
		rewardsParams.width = LayoutParams.FILL_PARENT;
		rewards.setLayoutParams(rewardsParams);
		rewards.setId(view.generateViewId());
		TableRow hdr = new TableRow(this);
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		hdr.setLayoutParams(rowParams);
		TextView rank = new TextView(this);
		hdr.addView(rank);
		TextView gold = new TextView(this);
		gold.setText(" Gold");
		hdr.addView(gold);
		TextView exp = new TextView(this);
		exp.setText(" EXP");
		hdr.addView(exp);
		rewards.addView(hdr);
		for(int i = 0; i < 5; i++)
		{
			TableRow row = new TableRow(this);
			row.setLayoutParams(rowParams);
			String diff = "?";
			switch(i)
			{
				case 0: diff = "Basic"; break;
				case 1: diff = "Intermediate"; break;
				case 2: diff = "Advanced"; break;
				case 3: diff = "Hard"; break;
				case 4: diff = "Elite"; break;
			}
			rank = new TextView(this);
			rank.setText(diff);
			row.addView(rank);

			// TODO: Figure out a nice way to add cell padding
			gold = new TextView(this);
			gold.setText(" " + String.valueOf(data.getGold(i)));
			gold.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			// gold.setLayoutParams(margin);
			row.addView(gold);

			exp = new TextView(this);
			exp.setText(" " + String.valueOf(data.getExpDaily(i)));
			exp.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			// exp.setLayoutParams(margin);
			row.addView(exp);

			rewards.addView(row);
		}
		view.addView(rewards);

		String info = data.getInfo();
		if(info != null && !info.equals(""))
		{
			TextView infoView = new TextView(this);
			infoView.setLayoutParams(makeBelow(rewards.getId()));
			infoView.setId(view.generateViewId());
			infoView.setText(data.getInfo());
			view.addView(infoView);
		}
	}

	private void updateDaily(String name, int header, int details)
	{
		MissionInfo info = getMissionInfo(name);
		TextView tvHeader = (TextView)findViewById(header);
		RelativeLayout tvDetails = (RelativeLayout)findViewById(details);

		if(info != null)
		{
			tvHeader.setText(info.getName());
			updateDetails(tvDetails, info);
		}
		else
		{
			tvHeader.setText(name);
			tvDetails.removeAllViewsInLayout();
		}
	}
	
	private void updateDailyInfo(JSONArray result)
	{
		TextView t = (TextView)findViewById(R.id.message);
		try
		{
			if(result != null)
			{
				// t.setVisibility(View.GONE);
				t.setText(mReqDate);
				if(!result.isNull(0))
				{
					JSONObject daily = result.getJSONObject(0);
					String tara = daily.getJSONObject("Tara").getJSONObject("normal").getString("name");
					String tail = daily.getJSONObject("Taillteann").getJSONObject("normal").getString("name");
					updateDaily(tara, R.id.daily_today_tara, R.id.daily_today_tara_content);
					updateDaily(tail, R.id.daily_today_tail, R.id.daily_today_tail_content);
				}
				if(!result.isNull(1))
				{
					JSONObject daily = result.getJSONObject(1);
					String tara = daily.getJSONObject("Tara").getJSONObject("normal").getString("name");
					String tail = daily.getJSONObject("Taillteann").getJSONObject("normal").getString("name");
					updateDaily(tara, R.id.daily_tomorrow_tara, R.id.daily_tomorrow_tara_content);
					updateDaily(tail, R.id.daily_tomorrow_tail, R.id.daily_tomorrow_tail_content);
				}
			}
			else
			{
				t.setText("Offline");
			}
		}
		catch(JSONException e)
		{
			Log.d(tag, e.getMessage());
		}
	}
	
	public void updateContent(boolean force)
	{
		// TextView t = (TextView)findViewById(R.id.message);
		// t.setText(isConnected(this) ? (isMobile(this) ? "Mobile" : "Wideband") : "Disconnected");
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR, -7);
		SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd");
		sdfServer.setTimeZone(TimeZone.getTimeZone(serverTimezone));
		mReqDate = sdfServer.format(now.getTime());
		
		cacheDaily.load("cache_daily.json");
		if(force || !mReqDate.equals(cacheDaily.getSignature()))
		{
			Log.v(tag, "Getting dailies for " + mReqDate);
			(new MainUpdater(this)).execute(mReqDate);
		}
		else
		{
			Log.v(tag, "Using cached dailies for " + mReqDate);
			try
			{
				JSONArray tmp = new JSONArray(cacheDaily.get());
				updateDailyInfo(tmp);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private MissionInfo getMissionInfo(String name)
	{
		MissionInfoHandler mih = MissionInfoHandler.getInstance();
		
		if(!mih.isLoaded())
		{
			AssetManager assets = getBaseContext().getAssets();
			try
			{
				InputStream is = assets.open("daily/details.xml");
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(mih);
				
				InputSource inStream = new InputSource(is);
				xr.parse(inStream);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return mih.getMission(name);
	}
	
	private class MainUpdater extends AsyncTask<String, Void, JSONArray >
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
		protected JSONArray doInBackground(String[] p1)
		{
			JSONArray data = null;
			
			if(isConnected(mCtx))
			{
				MyHTTP http = MyHTTP.getInstance();
				data = http.getDailyInfo(mCtx, p1[0]);
			}
			else
			{
				Log.d(tag, "Not connected!");
			}
			return data;
		}
		
		@Override
		protected void onPostExecute(JSONArray result)
		{
			if(result != null)
			{
				cacheDaily.putSignature(mReqDate);
				cacheDaily.put(result.toString());
				cacheDaily.flush();
			}
			updateDailyInfo(result);
		}
	}
}
