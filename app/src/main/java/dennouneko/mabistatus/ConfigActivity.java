package dennouneko.mabistatus;
import android.app.*;
import android.os.*;
import android.preference.*;

public class ConfigActivity extends PreferenceActivity
{
	public static final String KEY_PREF_NOTIFY = "pref_notify";
	public static final String KEY_PREF_SLEEP_UPDATES = "pref_sleepUpdates";
	public static final String KEY_PREF_WIFI = "pref_wifi";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// update the config view with xml data
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
