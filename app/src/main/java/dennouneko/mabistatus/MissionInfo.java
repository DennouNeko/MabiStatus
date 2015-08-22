package dennouneko.mabistatus;

public class MissionInfo
{
	String name = "?";
	String players = "?";
	String time = "?";
	String info = "";
	boolean timeLimit = false;
	int[] gold = new int[5];
	int[] exp = new int[5];
	
	public String getName()
	{
		return name;
	}

	public MissionInfo setName(String val)
	{
		name = val;
		return this;
	}

	public String getPlayers()
	{
		return players;
	}
	
	public MissionInfo setPlayers(String val)
	{
		players = val;
		return this;
	}
	
	public String getTime()
	{
		return time;
	}
	
	public boolean isTimeLimit()
	{
		return timeLimit;
	}
	
	public MissionInfo setTime(String val, boolean isLimit)
	{
		time = val;
		timeLimit = isLimit;
		return this;
	}
	
	public String getInfo()
	{
		return info;
	}
	
	public MissionInfo setInfo(String val)
	{
		info = val;
		return this;
	}
	
	public int getGold(int level)
	{
		if(level < 0 || level > 4) return 0;
		return gold[level];
	}
	
	public MissionInfo setGold(int level, int val)
	{
		if(level >= 0 && level <= 4)
		{
			gold[level] = val;
		}
		return this;
	}
	
	public int getExp(int level)
	{
		if(level < 0 || level > 4) return 0;
		return exp[level];
	}
	
	public int getExpDaily(int level)
	{
		if(level < 0 || level > 4) return 0;
		return exp[level] * 2;
	}
	
	public MissionInfo setExp(int level, int val)
	{
		if(level >= 0 && level <= 4)
		{
			exp[level] = val;
		}
		return this;
	}
}
