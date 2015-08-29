package dennouneko.mabistatus;
import android.content.*;
import android.util.*;

public class PowerState extends BroadcastReceiver
{
	private static final String tag = "PowerState";
	static boolean mPlugged = false;
	static boolean mActive = false;
	static PowerState mInstance = null;
	
	private PowerState()
	{
	}
	
	public static PowerState getInstance(Context ctx)
	{
		if(mInstance == null)
		{
			mInstance = new PowerState();
		}
		return mInstance;
	}
	
	public static void register(Context ctx)
	{
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		PowerState rec = getInstance(ctx);
		Intent last = ctx.registerReceiver(rec, filter);
		if(last != null)
		{
			rec.onReceive(ctx, last);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			Log.v(tag, "Screen: OFF");
			mActive = false;
		}
		else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			Log.v(tag, "Screen: ON");
			mActive = true;
		}
		else if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
		{
			Log.v(tag, "Unplugged");
			mPlugged = false;
		}
		else if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
		{
			Log.v(tag, "Plugged");
			mPlugged = true;
		}
	}
	
	public static boolean isActive()
	{
		return mActive;
	}
	
	public static boolean isPlugged()
	{
		return mPlugged;
	}
}
