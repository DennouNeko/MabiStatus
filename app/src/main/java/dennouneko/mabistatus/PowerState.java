package dennouneko.mabistatus;
import android.content.*;
import android.util.*;

public class PowerState extends BroadcastReceiver
{
	private static final String tag = "PowerState";
	static boolean mPlugged = false;
	static boolean mActive = false;
	static PowerState mInstance = null;
	boolean registered = false;
	
	private PowerState()
	{
	}
	
	public static PowerState getInstance(Context ctx)
	{
		// there can be only one
		if(mInstance == null)
		{
			mInstance = new PowerState();
		}
		return mInstance;
	}
	
	public static void register(Context ctx)
	{
		// register as broadcast receiver
		PowerState rec = getInstance(ctx);
		if(!rec.registered)
		{
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_POWER_CONNECTED);
			filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			Intent last = ctx.registerReceiver(rec, filter);
			rec.registered = true;
			if(last != null)
			{
				rec.onReceive(ctx, last);
			}
		}
	}
	
	public static void unregister(Context ctx)
	{
		PowerState rec = getInstance(ctx);
		if(rec.registered)
		{
			ctx.unregisterReceiver(rec);
			rec.registered = false;
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// process received intent and update device state
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
