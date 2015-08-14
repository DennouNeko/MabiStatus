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

public class MyHTTP
{
	private static MyHTTP mInstance;
	
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
			Log.d("MabiStatus", "Starting request");
			URL url = new URL(src);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			Log.d("MabiStatus", "Setting up params");
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// start of query
			Log.d("MabiStatus", "Sending query");
			conn.connect();
			int code = conn.getResponseCode();
			
			Log.d("MabiStatus", "Response code: " + code);
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
}
