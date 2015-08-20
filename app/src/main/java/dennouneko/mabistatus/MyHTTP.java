package dennouneko.mabistatus;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import android.text.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import java.io.*;
import org.apache.http.entity.*;
import java.lang.annotation.*;
import android.net.*;
import android.app.*;
import java.net.*;
import android.util.*;
import java.nio.*;
import android.content.*;
import java.util.*;

public class MyHTTP
{
	private static MyHTTP mInstance;
	private static final String tag = "MyHTTP";
	private static final String urlPatch = "http://php-dennouneko.rhcloud.com/proxy.php?type=patch";
	private static final String urlDaily = "http://php-dennouneko.rhcloud.com/proxy.php?type=daily";
	
	private MyHTTP()
	{
	}
	
	public static MyHTTP getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new MyHTTP();
		}
		return mInstance;
	}
	
	public String getData(String src) throws ClientProtocolException, IOException
	{
		InputStream is = null;
		try
		{
			Log.d(tag, "Starting request");
			URL url = new URL(src);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			Log.d(tag, "Setting up params");
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// start of query
			Log.d(tag, "Sending query");
			conn.connect();
			int code = conn.getResponseCode();
			
			Log.d(tag, "Response code: " + code);
			is = conn.getInputStream();
			
			String ret = readIt(is, -1);
			return ret;
		}
		finally
		{
			if(is != null)
			{
				is.close();
				is = null;
			}
		}
	}
	
	public String readIt(InputStream is, int len) throws IOException, UnsupportedEncodingException
	{
		Reader reader = null;
		reader = new InputStreamReader(is, "UTF-8");
		if(len < 0)
		{
			StringBuffer out = new StringBuffer();
			char[] buffer = new char[1024];
			for(;;)
			{
				int rds = reader.read(buffer, 0, buffer.length);
				if(rds < 0) break;
				out.append(buffer, 0, rds);
			}
			return out.toString();
		}
		else
		{
			char[] buf = new char[len];
			reader.read(buf);
			return new String(buf);
		}
	}
	
	public int getPatchStatus(Context ctx)
	{
		String result = "";
		int ret = 0;
		try
		{
			result = getData(urlPatch);
		}
		catch(ClientProtocolException e)
		{
			return -1;
		}
		catch(IOException e)
		{
			return -1;
		}
		String parts[] = result.split("\n");
		HashMap<String, String> data = new HashMap<String, String>();

		for(String line : parts)
		{
			String pt[] = line.trim().split("=", 2);
			if(pt.length > 1) data.put(pt[0], pt[1]);
		}
		
		Log.v(tag, "Response:\n" + result);

		String patchAccept = data.get("patch_accept");
		
		if(patchAccept != null)
		{
			Log.d(tag, "patch_accept = " + patchAccept);
			if(patchAccept.equals("0"))
			{
				ret = 1;
			}
			else if(patchAccept.equals("1"))
			{
				ret = 2;
			}
		}
		else
		{
			Log.d(tag, "patchAccept is undefined!");
			ret = 0;
		}
		
		return ret;
	}
	
	public int getLoginStatus(Context ctx)
	{
		return 2; // For now pretend it's online
	}
	
	public Map<String, String> getDailyInfo(Context ctx)
	{
		Map<String, String> ret = new HashMap<String, String>();
		
		return ret;
	}
}
