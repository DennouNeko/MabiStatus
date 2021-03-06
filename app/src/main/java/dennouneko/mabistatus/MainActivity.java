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
import android.preference.*;

public class MainActivity extends Activity 
{
	private static final String tag = "MainActivity";
	public static final int mIdStatus = 1;
	public static final String serverTimezone = "America/Los_Angeles";
	private String mReqDate;
	
	private DiskCache cacheDaily = new DiskCache(this);
	
	private void doTest()
	{
		// nothing interesting here
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean notify = pref.getBoolean(ConfigActivity.KEY_PREF_NOTIFY, true);
		notifyStatus(this, notify ? "true" : "false");
	}
	
	private void doSettings()
	{
		// show the settings activity
		Intent config = new Intent(this, ConfigActivity.class);
		startActivity(config);
	}
	
	public void showAbout()
	{
		// display an "about" toast
		Resources r = getResources();
		String translator = r.getString(R.string.about_translator);
		String appName = r.getString(R.string.app_name);
		String message = appName + " by DennouNeko\n2015";
		if(!translator.equals(""))
		{
			message += translator;
		}
		Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
		t.show();
	}
	
	public static void notifyStatus(Context ctx, String message)
	{
		// helper function for notifications
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
	
	public static void notifyStatus(Context ctx, int resid)
	{
		String msg = ctx.getString(resid);
		notifyStatus(ctx, msg);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		// register the power state listener
		PowerState.register(getApplicationContext());
		
		// set activity layout
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
		// helper function to make single item togglable
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
		// helper funtion to make a toggle group
		// max one of items is visible at any time
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
		// menu click handler
		switch(item.getItemId())
		{
			case R.id.menu_refresh:
				updateContent(true);
				return true;
			case R.id.menu_refresh_widget:
				StatusWidgetProvider.updateAllWidgets(getApplicationContext());
				return true;
			case R.id.menu_settings:
				doSettings();
				return true;
			case R.id.menu_about:
				showAbout();
				return true;
			/* case R.id.menu_debug:
				doTest();
				return true;//*/
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// show menu
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public static boolean isConnected(Context ctx)
	{
		// helper function for checking network state
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
	
	public static boolean isMobile(Context ctx)
	{
		// helper function for checking connection type
		ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connMgr == null) return true; // no service, assume it's low-speed transfer
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if(info == null) return true;
		Log.v(tag, "Network type = " + info.getTypeName());
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
		// layout helper function
		// puts one item below another
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.BELOW, id);
		return param;
	}

	private void updateDetails(RelativeLayout view, MissionInfo data)
	{
		// clear layout
		view.removeAllViewsInLayout();
		
		// add party size info
		TextView players = new TextView(this);
		players.setId(view.generateViewId());
		players.setText("Party size: " + data.getPlayers());
		view.addView(players);
		
		// add duration/time limit
		TextView time = new TextView(this);
		time.setLayoutParams(makeBelow(players.getId()));
		time.setId(view.generateViewId());
		time.setText((data.isTimeLimit() ? "Time limit: " : "Time: ") + data.getTime());
		view.addView(time);
		
		// add rewards
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
		
		// add extra info, if there is any
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
		// update info for a single daily mission
		MissionInfo info = getMissionInfo(name);
		TextView tvHeader = (TextView)findViewById(header);
		RelativeLayout tvDetails = (RelativeLayout)findViewById(details);

		if(info != null)
		{
			// if there are any details for given mission
			tvHeader.setText(info.getName());
			updateDetails(tvDetails, info);
		}
		else
		{
			// missingno
			tvHeader.setText(name);
			tvDetails.removeAllViewsInLayout();
		}
	}
	
	private void updateDailyInfo(JSONArray result)
	{
		// process data and perform update
		TextView t = (TextView)findViewById(R.id.message);
		try
		{
			if(result != null)
			{
				// t.setVisibility(View.GONE);
				t.setText(mReqDate);
				if(!result.isNull(0))
				{
					// got data for "today"
					JSONObject daily = result.getJSONObject(0);
					String tara = daily.getJSONObject("Tara").getJSONObject("normal").getString("name");
					String tail = daily.getJSONObject("Taillteann").getJSONObject("normal").getString("name");
					updateDaily(tara, R.id.daily_today_tara, R.id.daily_today_tara_content);
					updateDaily(tail, R.id.daily_today_tail, R.id.daily_today_tail_content);
				}
				if(!result.isNull(1))
				{
					// got data for "tomorrow"
					JSONObject daily = result.getJSONObject(1);
					String tara = daily.getJSONObject("Tara").getJSONObject("normal").getString("name");
					String tail = daily.getJSONObject("Taillteann").getJSONObject("normal").getString("name");
					updateDaily(tara, R.id.daily_tomorrow_tara, R.id.daily_tomorrow_tara_content);
					updateDaily(tail, R.id.daily_tomorrow_tail, R.id.daily_tomorrow_tail_content);
				}
			}
			else
			{
				// invalid data
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
		// prepare the "current daily date"
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR, -7);
		SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd");
		sdfServer.setTimeZone(TimeZone.getTimeZone(serverTimezone));
		mReqDate = sdfServer.format(now.getTime());
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean wifi = pref.getBoolean(ConfigActivity.KEY_PREF_WIFI, true);
		boolean mobile = MainActivity.isMobile(this);
		
		// perform update respecting preferences
		// and cached data, unless user forced update
		cacheDaily.load("cache_daily.json");
		if(force || (!mReqDate.equals(cacheDaily.getSignature()) && (!wifi || !mobile)))
		{
			Log.v(tag, "Getting dailies for " + mReqDate);
			(new MainUpdater(this)).execute(mReqDate);
		}
		else
		{
			Log.v(tag, "Using cached dailies for " + cacheDaily.getSignature());
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
		// helper funtion for retrieving mission details from
		// embedded database
		MissionInfoHandler mih = MissionInfoHandler.getInstance();
		
		// liad data if we haven't done it yet
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
			// let user know we have started update
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
			
			// no need to re-check settings here
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
				// update cache, if we got a result
				cacheDaily.putSignature(mReqDate);
				cacheDaily.put(result.toString());
				cacheDaily.flush();
			}
			updateDailyInfo(result);
		}
	}
}
