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
		// TextView t = (TextView)findViewById(R.id.message);
		// t.setText(isConnected(this) ? (isMobile(this) ? "Mobile" : "Wideband") : "Disconnected");
		(new MainUpdater(this)).execute();
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
				Log.d(tag, "Not connected!");
			}
			return data;
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
			TextView players = new TextView(mCtx);
			players.setId(view.generateViewId());
			players.setText("Party size: " + data.getPlayers());
			view.addView(players);
			
			TextView time = new TextView(mCtx);
			time.setLayoutParams(makeBelow(players.getId()));
			time.setId(view.generateViewId());
			time.setText((data.isTimeLimit() ? "Time limit: " : "Time: ") + data.getTime());
			view.addView(time);
			
			TableLayout rewards = new TableLayout(mCtx);
			RelativeLayout.LayoutParams rewardsParams = makeBelow(time.getId());
			rewardsParams.width = LayoutParams.FILL_PARENT;
			rewards.setLayoutParams(rewardsParams);
			rewards.setId(view.generateViewId());
			TableRow hdr = new TableRow(mCtx);
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			hdr.setLayoutParams(rowParams);
			TextView rank = new TextView(mCtx);
			hdr.addView(rank);
			TextView gold = new TextView(mCtx);
			gold.setText("Gold");
			hdr.addView(gold);
			TextView exp = new TextView(mCtx);
			exp.setText("EXP");
			hdr.addView(exp);
			rewards.addView(hdr);
			for(int i = 0; i < 5; i++)
			{
				TableRow row = new TableRow(mCtx);
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
				rank = new TextView(mCtx);
				rank.setText(diff);
				row.addView(rank);
				
				gold = new TextView(mCtx);
				gold.setText(String.valueOf(data.getGold(i)));
				gold.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
				row.addView(gold);
				
				exp = new TextView(mCtx);
				exp.setText(String.valueOf(data.getExpDaily(i)));
				exp.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
				row.addView(exp);
				
				rewards.addView(row);
			}
			view.addView(rewards);
			
			String info = data.getInfo();
			if(info != null && !info.equals(""))
			{
				TextView infoView = new TextView(mCtx);
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
							String tara = ((JSONObject)daily.get("Tara")).getString("Normal");
							String tail = ((JSONObject)daily.get("Taillteann")).getString("Normal");
							updateDaily(tara, R.id.daily_today_tara, R.id.daily_today_tara_content);
							updateDaily(tail, R.id.daily_today_tail, R.id.daily_today_tail_content);
						}
						if(!result.isNull("tomorrow"))
						{
							JSONObject daily = (JSONObject)result.get("tomorrow");
							String tara = ((JSONObject)daily.get("Tara")).getString("Normal");
							String tail = ((JSONObject)daily.get("Taillteann")).getString("Normal");
							updateDaily(tara, R.id.daily_tomorrow_tara, R.id.daily_tomorrow_tara_content);
							updateDaily(tail, R.id.daily_tomorrow_tail, R.id.daily_tomorrow_tail_content);
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
