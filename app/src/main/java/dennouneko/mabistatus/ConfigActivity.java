package dennouneko.mabistatus;
import android.app.*;
import android.os.*;
import android.preference.*;

public class ConfigActivity extends PreferenceActivity
{
	public static final String KEY_PREF_NOTIFY = "pref_notify";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
