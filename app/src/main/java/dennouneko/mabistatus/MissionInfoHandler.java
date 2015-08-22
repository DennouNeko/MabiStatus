package dennouneko.mabistatus;
import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import android.util.*;

public class MissionInfoHandler extends DefaultHandler
{
	private static final String tag = "MissionInfoHandler";
	
	Map<String, MissionInfo> data;
	String curName = "";
	String curValue = "";
	boolean curElem = false;
	MissionInfo curMission = null;
	static MissionInfoHandler mInstance = null;
	boolean mLoaded = false;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if(curElem)
		{
			curValue = curValue + new String(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		curElem = false;
		curValue = curValue.trim();
		
		if(qName.equals("name"))
		{
			curMission.setName(curValue);
		}
		else if(qName.equals("players"))
		{
			curMission.setPlayers(curValue);
		}
		else if(qName.equals("time-limit"))
		{
			curMission.setTime(curValue, true);
		}
		else if(qName.equals("time"))
		{
			curMission.setTime(curValue, false);
		}
		else if(qName.equals("info"))
		{
			curMission.setInfo(curValue);
		}
		
		else if(qName.equals("mission"))
		{
			Log.v(tag, "Adding mission " + curName + " (" + curMission.getName() + ")");
			data.put(curName, curMission);
			curName = "";
			curMission = null;
		}
		else if(qName.equals("details"))
		{
			mLoaded = true;
		}
		
		curValue = "";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		curElem = true;
		
		if(qName.equals("details"))
		{
			data = new HashMap<String, MissionInfo>();
			mLoaded = false;
		}
		else if(qName.equals("mission"))
		{
			curName = attributes.getValue("id");
			curMission = new MissionInfo();
			curMission.setName(curName);
		}
		else if(qName.equals("reward"))
		{
			int level = Integer.parseInt(attributes.getValue("level"));
			int exp = Integer.parseInt(attributes.getValue("exp"));
			int gold = Integer.parseInt(attributes.getValue("gold"));
			curMission.setExp(level, exp);
			curMission.setGold(level, gold);
			
			Log.v(tag, String.format("Diff: %d, %d, %d", level, exp, gold));
		}
	}
	
	private MissionInfoHandler()
	{
	}
	
	public static MissionInfoHandler getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new MissionInfoHandler();
		}
		return mInstance;
	}
	
	public MissionInfo getMission(String name)
	{
		return data.get(name);
	}
	
	public boolean isLoaded()
	{
		return mLoaded;
	}
}
