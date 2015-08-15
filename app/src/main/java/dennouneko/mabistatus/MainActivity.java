package dennouneko.mabistatus;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity 
{
	private static final String tag = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        updateContent();
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
	
	public void updateWidget(View view)
	{
		Log.v(tag, "updateWidget");
		updateContent();
		StatusWidgetProvider.updateAllWidgets(this);
	}
}
