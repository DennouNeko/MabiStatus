package dennouneko.mabistatus;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import android.text.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import java.io.*;
import org.apache.http.entity.*;
import java.lang.annotation.*;

public class MyHTTP
{
	private static MyHTTP mInstance;
	private HttpClient mClient;
	
	private MyHTTP()
	{
		mClient = new DefaultHttpClient();
	}
	
	public static MyHTTP getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new MyHTTP();
		}
		return mInstance;
	}
	
	public String get(String url) throws ClientProtocolException, IOException
	{
		HttpResponse resp = mClient.execute(new HttpGet(url));
		StatusLine sline = resp.getStatusLine();
		if(sline.getStatusCode() == HttpStatus.SC_OK)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			resp.getEntity().writeTo(out);
			String ret = out.toString();
			out.close();
			return ret;
		}
		return "";
	}
}
