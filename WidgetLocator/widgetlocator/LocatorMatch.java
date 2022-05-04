package widgetlocator;

public class LocatorMatch
{
	private Locator locator1=null;
	private Locator locator2=null;
	private int score=0;
	private int maxScore=0;
	private boolean isValid=false;
	private String comment=null;

	public LocatorMatch(Locator locator1, Locator locator2, int score, int maxScore)
	{
		this.locator1 = locator1;
		this.locator2 = locator2;
		this.score = score;
		this.maxScore = maxScore;
	}

	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public void setValid(boolean isValid)
	{
		this.isValid = isValid;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public Locator getLocator1()
	{
		return locator1;
	}

	public void setLocator1(Locator widget1)
	{
		this.locator1 = widget1;
	}

	public Locator getLocator2()
	{
		return locator2;
	}

	public void setLocator2(Locator widget)
	{
		this.locator2 = widget;
	}

	public int getMaxScore()
	{
		return maxScore;
	}

	public void setMaxScore(int maxScore)
	{
		this.maxScore = maxScore;
	}

	/**
	 * @return The match in percent or 0 if maxScore is zero
	 */
	public int getMatchPercent()
	{
		if(maxScore>0)
		{
			return (100*score)/maxScore;
		}
		return 0;
	}
}
