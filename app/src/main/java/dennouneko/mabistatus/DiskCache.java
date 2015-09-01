package dennouneko.mabistatus;
import java.io.*;
import android.content.*;
import org.json.*;
import android.util.*;

public class DiskCache
{
	private static final String tag = "DiskCache";
	File mName = null;
	Context mCtx = null;
	
	String mSignature = null;
 	JSONArray mData = null;
	
	public DiskCache(Context ctx)
	{
		mCtx = ctx;
	}
	
	public void load(String name)
	{
		mName = new File(mCtx.getCacheDir(), name);

		FileReader fh = null;

		try
		{
			// load the whole cache file
			fh = new FileReader(mName.getAbsolutePath());
			String data = "";
			StringBuffer out = new StringBuffer();
			char[] buf = new char[1024];
			for(;;)
			{
				int rds = fh.read(buf);
				if(rds < 0) break;
				out.append(buf, 0, rds);
			}
			data = out.toString();
			// parse the data
			JSONObject temp = new JSONObject(data);

			mSignature = temp.getString("signature");
			mData = temp.getJSONArray("data");
		}
		catch(FileNotFoundException e)
		{
			Log.w(tag, "File not found: " + mName.getAbsolutePath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				// cleanup
				if(fh != null)
				{
					fh.close();
					fh = null;
				}

				if(mData == null)
				{
					mSignature = null;
					mData = new JSONArray();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void putSignature(String sig)
	{
		mSignature = sig;
	}
	
	public void put(String value)
	{
		put(0, value);
	}
	
	public void put(int idx, String value)
	{
		if(mData == null)
		{
			mData = new JSONArray();
		}
		
		try
		{
			mData.put(idx, value);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int length()
	{
		if(mData == null) return 0;
		return mData.length();
	}
	
	public String getSignature()
	{
		return mSignature;
	}
	
	public String get() throws JSONException
	{
		return get(0);
	}
	
	public String get(int idx) throws JSONException
	{
		if(mData == null)
		{
			throw new NullPointerException();
		}
		
		return mData.getString(idx);
	}
	
	public void flush()
	{
		// write current state back to original file
		FileWriter fh = null;
		if(mData == null)
		{
			throw new NullPointerException();
		}
		try
		{
			Log.v(tag, mName.getAbsolutePath());
			fh = new FileWriter(mName.getAbsolutePath());
			// build the "carrier" object
			JSONObject temp = new JSONObject();
			temp.accumulate("signature", mSignature);
			temp.accumulate("data", mData);
			fh.write(temp.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				// cleanup
				if(fh != null)
				{
					fh.close();
					fh = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
