package widgetlocator;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class WidgetLocator
{
	private static String[] LOCATORS =         { "tag", "class", "name", "id", "href", "alt", "xpath", "idxpath", "is_button", "location", "area", "shape", "visible_text", "neighbor_text" };
	private double[] WEIGHTS =                 { 1.5,   0.5,     1.5,    1.5,  0.5,    0.5,   0.5,     0.5,       0.5,         0.5,        0.5,    0.5,     1.5,            1.5 };
	private static int[] SIMILARITY_FUNCTION = { 0,     1,       0,      0,    1,      1,     1,       1,         0,           3,          2,      2,       1,              4 };
	private static int FIRST_APP_INDEX = 0;
	private static int END_APP_INDEX = FIRST_APP_INDEX;
	private static int NO_THREADS = 20;

	private static Hashtable<String, List<File>> folderHash = new Hashtable<String, List<File>>();
	private static Hashtable<String, Properties> propertiesHash = new Hashtable<String, Properties>();

	private WebDriver webDriver=null;
	private String elementsToExtract="input,textarea,button,select,a,h1,h2,h3,h4,h5,li,span,div,p,th,tr,td,label,svg";

	private static String[] OLD_WEB_SITES = {"https://web.archive.org/web/20180702000525if_/https://www.adobe.com/", "https://web.archive.org/web/20170401212838if_/https://www.aliexpress.com/", "https://web.archive.org/web/20170402if_/http://www.amazon.com/",
			"https://web.archive.org/web/20171002003641if_/https://www.apple.com/", "https://web.archive.org/web/20180401if_/https://www.bestbuy.com/", "https://web.archive.org/web/20191201234246if_/http://www.bing.com/", "https://web.archive.org/web/20181202045722if_/https://www.chase.com/",
			"https://web.archive.org/web/20180402002511if_/https://www.cnn.com/", "https://web.archive.org/web/20160331202313if_/http://sfbay.craigslist.org/", "https://web.archive.org/web/20191202001310if_/https://www.dropbox.com/?landing=dbv2",
			"https://web.archive.org/web/20180601235610if_/https://www.ebay.com/", "https://web.archive.org/web/20191201233248if_/https://www.espn.com/", "https://web.archive.org/web/20191002005309if_/https://www.etsy.com/",
			"https://web.archive.org/web/20161101030329/https://www.facebook.com/", "https://web.archive.org/web/20180102000534if_/https://www.fidelity.com/", "https://web.archive.org/web/20190201214503if_/https://www.salesforce.com/products/platform/products/force/?d=70130000000f27V&internal=true", "https://web.archive.org/web/20180801233843if_/http://www.foxnews.com/",
			"https://web.archive.org/web/20180802000242if_/https://www.google.com/", "https://web.archive.org/web/20191201225319if_/https://www.hulu.com/welcome", "https://web.archive.org/web/20190602000208if_/https://www.imdb.com/", "https://web.archive.org/web/20151202004335if_/http://www.indeed.com/",
			"https://web.archive.org/web/20180602003550if_/https://www.instagram.com/", "https://web.archive.org/web/20171101205846if_/https://www.instructure.com/", "https://web.archive.org/web/20190401223517if_/https://www.intuit.com/",
			"https://web.archive.org/web/20170802004444if_/https://www.linkedin.com/", "https://web.archive.org/web/20170801235424if_/https://outlook.live.com/owa/", "https://web.archive.org/web/20160602021830if_/http://www.microsoft.com/en-us/",
			"https://web.archive.org/web/201802if_/https://login.microsoftonline.com/", "https://web.archive.org/web/20160101232822if_/https://www.shopify.com/", "https://web.archive.org/web/20161003001630if_/https://www.netflix.com/ca/",
			"https://web.archive.org/web/20181201235804if_/https://www.nytimes.com/", "https://web.archive.org/web/20190301225930if_/https://www.office.com/", "https://web.archive.org/web/20171102031615if_/https://www.okta.com/", "https://web.archive.org/web/20190702000032if_/https://www.paypal.com/us/home",
			"https://web.archive.org/web/20170304235734if_/https://www.reddit.com/", "https://web.archive.org/web/20171001232623if_/https://www.spotify.com/us/", "https://web.archive.org/web/20170602003201if_/http://www.target.com/", "https://web.archive.org/web/20160301234356if_/http://www.twitch.tv/", "https://web.archive.org/web/20170702000250if_/https://twitter.com/", "https://web.archive.org/web/20170629135919if_/https://www.ups.com/us/en/Home.page", "https://web.archive.org/web/20171202144652/https://www.usps.com/",
			"https://web.archive.org/web/20170902000248if_/https://www.walmart.com/", "https://web.archive.org/web/20191002055021if_/https://www.wellsfargo.com/", "https://web.archive.org/web/20170901235350if_/https://www.wikipedia.org/",
			"https://web.archive.org/web/20181101/https://www.yahoo.com/", "https://web.archive.org/web/20190801/https://www.youtube.com/", "https://web.archive.org/web/20170801211941if_/https://www.zillow.com/",
			"https://web.archive.org/web/20160501084828/http://zoom.us/"};

	private static String[] NEW_WEB_SITES = {"https://web.archive.org/web/20201102003024if_/https://www.adobe.com/", "https://web.archive.org/web/20201201235538if_/https://www.aliexpress.com/", "https://web.archive.org/web/20201201if_/https://www.amazon.com/",
			"https://web.archive.org/web/20201201235612if_/https://www.apple.com/", "https://web.archive.org/web/20201201233637if_/https://www.bestbuy.com/", "https://web.archive.org/web/20201201234200if_/https://www.bing.com/", "https://web.archive.org/web/20201202004756if_/https://www.chase.com/",
			"https://web.archive.org/web/20201201235755if_/https://www.cnn.com/", "https://web.archive.org/web/20201202081744if_/https://sfbay.craigslist.org/", "https://web.archive.org/web/20201204000601if_/https://www.dropbox.com/?landing=dbv2",
			"https://web.archive.org/web/20201202000703if_/https://www.ebay.com/", "https://web.archive.org/web/20201202000942if_/https://www.espn.com/", "https://web.archive.org/web/20201201233425if_/https://www.etsy.com/",
			"https://web.archive.org/web/20201201011205/https://www.facebook.com/", "https://web.archive.org/web/20201201211643if_/https://www.fidelity.com/", "https://web.archive.org/web/20201201203858if_/https://www.salesforce.com/products/platform/products/force/?sfdc-redirect=300&bc=WA", "https://web.archive.org/web/20201201235925if_/https://www.foxnews.com/",
			"https://web.archive.org/web/20201201235949if_/https://www.google.com/", "https://web.archive.org/web/20201202000152if_/https://www.hulu.com/welcome", "https://web.archive.org/web/20201201233544if_/https://www.imdb.com/", "https://web.archive.org/web/20201201225703if_/https://www.indeed.com/",
			"https://web.archive.org/web/20201202000011if_/https://www.instagram.com/", "https://web.archive.org/web/20201202000839if_/https://www.instructure.com/", "https://web.archive.org/web/20201202032948if_/https://www.intuit.com/",
			"https://web.archive.org/web/20201202011337if_/https://www.linkedin.com/", "https://web.archive.org/web/20201201235603if_/https://outlook.live.com/owa/", "https://web.archive.org/web/20201201234606if_/https://www.microsoft.com/en-us/",
			"https://web.archive.org/web/20201201if_/https://login.microsoftonline.com/", "https://web.archive.org/web/20201202023130if_/http://myshopify.com/", "https://web.archive.org/web/20201201180555if_/https://www.netflix.com/",
			"https://web.archive.org/web/20201202001128if_/https://www.nytimes.com/", "https://web.archive.org/web/20201201232252if_/https://www.office.com/", "https://web.archive.org/web/20201201231944if_/https://www.okta.com/", "https://web.archive.org/web/20201202000650if_/https://www.paypal.com/us/home",
			"https://web.archive.org/web/20201201132702if_/https://www.reddit.com/", "https://web.archive.org/web/20201201235821if_/https://www.spotify.com/us/", "https://web.archive.org/web/20201201233605if_/https://www.target.com/", "https://web.archive.org/web/20201202000056if_/https://www.twitch.tv/", "https://web.archive.org/web/20201201235946if_/https://twitter.com/", "https://web.archive.org/web/20201127140803if_/https://www.ups.com/us/en/Home.page", "https://web.archive.org/web/20201201112228/https://www.usps.com/",
			"https://web.archive.org/web/20201201235513if_/https://www.walmart.com/", "https://web.archive.org/web/20201201235604if_/https://www.wellsfargo.com/", "https://web.archive.org/web/20201202002020if_/https://www.wikipedia.org/",
			"https://web.archive.org/web/20201202/https://www.yahoo.com/", "https://web.archive.org/web/20201201/https://www.youtube.com/", "https://web.archive.org/web/20201201214009if_/https://www.zillow.com/",
			"https://web.archive.org/web/20201202004452/https://zoom.us/"};

	private boolean logOn = true;
	private long highestDuration = 0;

	public WidgetLocator(String[] args)
	{
		if(args.length>0)
		{
			FIRST_APP_INDEX = string2Int(args[0]);
			END_APP_INDEX = FIRST_APP_INDEX;
		}
		if(args.length>1)
		{
			END_APP_INDEX = string2Int(args[1]);
		}
//		convertToProperties("apps/Ups");
//		logWebElements();
//		double similarity=((double)compareDistance("OK Cancel", "Cancel Help OK", 100))/100;
//		double similarity2=((double)compareNeighborText("OK Cancel", "Cancel Help OK", 100))/100;
		locateWebElements();
	}

	private int getLocatorIndex(String locatorName)
	{
		int i=0;
		for (String locator : LOCATORS)
		{
			if(locatorName.equals(locator))
			{
				return i;
			}
			i++;
		}
		return 0;
	}

	private String fromIdeToXPath(String ide)
	{
		if(ide==null)
		{
			return null;
		}
		if(ide.startsWith("xpath:"))
		{
			return ide.substring(6);
		}
		else if(ide.startsWith("id:"))
		{
			String id = ide.substring(3);
			return "//*[@id='"+id+"']";
		}
		else if(ide.startsWith("name:"))
		{
			String name = ide.substring(5);
			return "//*[@id='"+name+"']";
		}
		else if(ide.startsWith("linkText:"))
		{
			String linkText = ide.substring(9);
			return "//*[contains(text(),'"+linkText+"')]";
		}
		else
		{
			return ide;
		}
	}

	private String pathToIndexPath(String path)
	{
		StringBuffer buf = new StringBuffer();
		char previousChar = ']';
		for(int i=0; i<path.length(); i++)
		{
			char c = path.charAt(i);
			if(c == '/' && previousChar != ']')
			{
				buf.append("[1]");
			}
			buf.append(c);
			previousChar = c;
		}
		if(previousChar != ']')
		{
			buf.append("[1]");
		}
		return buf.toString();
	}

	private void convertToProperties(String folderPath)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(folderPath+"/data/xpaths.txt"));
			int count=0;
			try
			{
			    String line1 = br.readLine();
			    String line2 = br.readLine();
			    String line3 = br.readLine();
			    while (line1 != null && line2 != null) {
						String filename = folderPath+"/"+count+".properties";
						log(filename, "old_xpath="+pathToIndexPath(line1));
						log(filename, "new_xpath="+pathToIndexPath(line2));
						count++;
		        line1 = br.readLine();
		        line2 = br.readLine();
		        line3 = br.readLine();
			    }
			}
			finally
			{
			    br.close();
			}
		}
		catch (Exception e)
		{
			log("Error: " + e.toString());
		}
	}

	private long logWebElements()
	{
		try
		{
			List<File> apps = getFolders("apps");
			int startAppNo = FIRST_APP_INDEX;
			int endAppNo = apps.size();
			endAppNo = END_APP_INDEX;
			for(int appNo=startAppNo; appNo<=endAppNo; appNo++)
			{
				File app = apps.get(appNo);
				
				logTable("##### Application: " + app.getName());

				List<Locator> targetLocators = new ArrayList<Locator>();
				File targetWidgetsFolder = new File(app, "target_widgets");
				List<File> targetFolderFiles = getFolderFiles(targetWidgetsFolder);

				for (File targetWidget : targetFolderFiles)
				{
					Properties targetWidgetProperty = loadProperties(targetWidget);
					String xPath = targetWidgetProperty.getProperty("xpath", null);
					Locator locator = new Locator();
					String widgetId = targetWidgetProperty.getProperty("widget_id", null);
					addMetadata(locator, "xpath", xPath);
					addMetadata(locator, "widget_id", widgetId);
					targetLocators.add(locator);
				}

				File candidateWidgetsFolder = new File(app, "candidate_widgets");
				List<File> candidateFolderFiles = getFolderFiles(candidateWidgetsFolder);
				List<Properties> candidateWidgetProperties = new ArrayList<Properties>();
				for (File candidateWidget : candidateFolderFiles)
				{
					Properties candidateWidgetProperty = loadProperties(candidateWidget);
					candidateWidgetProperties.add(candidateWidgetProperty);
				}

				int count=0;
				for(Locator targetLocator:targetLocators)
				{
					String targetXPath = targetLocator.getMetadata("xpath");
					String widgetId = targetLocator.getMetadata("widget_id");

					for(Properties candidateWidgetProperty:candidateWidgetProperties)
					{
						String candidateWidgetId = candidateWidgetProperty.getProperty("widget_id");
						if(candidateWidgetId!=null && widgetId.equals(candidateWidgetId))
						{
							String candidateWidgetXPath = candidateWidgetProperty.getProperty("xpath");
							String filename = app.getAbsolutePath()+"/"+count+".properties";
							log(filename, "old_xpath="+targetXPath);
							log(filename, "new_xpath="+candidateWidgetXPath);
							count++;
						}
					}
				}
			}
			return 0;
		}
		catch (Exception e)
		{
			log("Error: " + e.toString());
			return 0;
		}
	}

	private long locateWebElements()
	{
		System.setProperty("webdriver.chrome.driver", (new File("bin/drivers/chromedriver.exe")).getAbsolutePath());
		webDriver=new ChromeDriver();
		webDriver.manage().timeouts().setScriptTimeout(300, TimeUnit.SECONDS);

		List<File> apps = getFolders("apps");
		int startAppNo = FIRST_APP_INDEX;
		int endAppNo = apps.size();
		endAppNo = END_APP_INDEX;
		for(int appNo=startAppNo; appNo<=endAppNo; appNo++)
		{
			try
			{
				File app = apps.get(appNo);
				
				logTable("##### Application: " + app.getName() + "(" + appNo + ")");

				List<Locator> targetLocators = new ArrayList<Locator>();
				List<File> targetFolderFiles = getFolderFiles(app);

				String url=OLD_WEB_SITES[appNo];
				webDriver.get(url);
				delay(20000);

				for (File targetWidget : targetFolderFiles)
				{
					Properties targetWidgetProperty = loadProperties(targetWidget);
					String xPath = targetWidgetProperty.getProperty("old_xpath", null);
					Locator locator = getLocatorForElement(xPath);
					if(locator!=null)
					{
						addMetadata(locator, "old_xpath", xPath);
						String newXPath = targetWidgetProperty.getProperty("new_xpath", null);
						addMetadata(locator, "new_xpath", newXPath);
						targetLocators.add(locator);
					}
					else
					{
						log("No locator for xpath: " + xPath);
					}
				}

				url=NEW_WEB_SITES[appNo];
				webDriver.get(url);
				delay(10000);

				Boolean[] locatedByOne = new Boolean[targetLocators.size()];
				for(int i=0; i<targetLocators.size(); i++)
				{
					locatedByOne[i] = false;
				}
				String[] xPathLocators = {"xpath", "idxpath", "ide", "robula", "montoto"};
				for(String xPathLocator:xPathLocators)
				{
					int located = 0;
					int notLocated = 0;
					int incorrectlyLocated = 0;
					int targetLocatorNo = 0;
					for(Locator targetLocator:targetLocators)
					{
						String targetXPath = targetLocator.getMetadata(xPathLocator);
						if(xPathLocator.equals("ide"))
						{
							targetXPath = fromIdeToXPath(targetXPath);
						}
						Locator candidateLocator = getXPathLocatorForElement(targetXPath);
						if(candidateLocator!=null)
						{
							// Found a candidate
							String elementXPath = candidateLocator.getMetadata("xpath");
							String candidateXPath = targetLocator.getMetadata("new_xpath");
							if(almostIdenticalXPaths(elementXPath, candidateXPath))
							{
								// Found the correct candidate
								located++;
								locatedByOne[targetLocatorNo] = true;
							}
							else
							{
								incorrectlyLocated++;
							}
						}
						else
						{
							// Did not find a candidate
							notLocated++;
						}
						targetLocatorNo++;
					}
					logTable(xPathLocator+":\t"+located+"\t"+notLocated+"\t"+incorrectlyLocated);
				}
				
				int located = 0;
				int notLocated = 0;
				for(Boolean oneLocated:locatedByOne)
				{
					if(oneLocated)
					{
						located++;
					}
					else
					{
						notLocated++;
					}
				}

				logTable("Multilocator:\t"+located+"\t"+notLocated+"\t0");

				List<Locator> candidateLocators = getLocators();

				if(targetLocators.size()>0)
				{
					// Load classes
					similo(targetLocators.get(0), candidateLocators);
				}

				long timeLocateAllTargets = 0;

				located = 0;
				notLocated = 0;
				int incorrectlyLocated = 0;
				for(Locator targetLocator:targetLocators)
				{
					Locator bestCandidate = similo(targetLocator, candidateLocators);
					timeLocateAllTargets += highestDuration;
					if(bestCandidate==null)
					{
						notLocated++;
					}
					else
					{
						String bestCandidateXPath = bestCandidate.getMetadata("xpath");
						String correctCandidateXPath = targetLocator.getMetadata("new_xpath");
						if(almostIdenticalXPaths(bestCandidateXPath, correctCandidateXPath))
						{
							located++;
						}
						else
						{
							incorrectlyLocated++;
						}
					}
				}
				if(targetLocators.size()>0)
				{
					long timePerTarget = timeLocateAllTargets / targetLocators.size();
					logPerformance(app.getName()+"\t"+timeLocateAllTargets+"\t"+targetLocators.size()+"\t"+candidateLocators.size()+"\t"+timePerTarget);
				}
				logTable("Similo:\t"+located+"\t"+notLocated+"\t"+incorrectlyLocated);
				
				if(webDriver!=null)
				{
					webDriver.close();
				}
			}
			catch (Exception e)
			{
				log("Error: " + e.toString());
				if(webDriver!=null)
				{
					webDriver.close();
				}
			}
		}
		webDriver.quit();
		return 0;
	}

	private String removeLastElement(String xpath)
	{
		int lastIndex = xpath.lastIndexOf('/');
		if(lastIndex > 0)
		{
			return xpath.substring(0, lastIndex);
		}
		return xpath;
	}

	private boolean almostIdenticalXPaths(String xpath1, String xpath2)
	{
		if(xpath1 == null || xpath2 == null)
		{
			return false;
		}
		int length1 = xpath1.length();
		int length2 = xpath2.length();
		if(length1 == length2)
		{
			return xpath1.equalsIgnoreCase(xpath2);
		}
		else if(length1 < length2)
		{
			xpath2 = removeLastElement(xpath2);
			return xpath1.equalsIgnoreCase(xpath2);
		}
		else
		{
			xpath1 = removeLastElement(xpath1);
			return xpath1.equalsIgnoreCase(xpath2);
		}
	}

	private String getXPathFromWidgetId(String widgetId, List<Properties> candidateWidgetProperties)
	{
		for(Properties widget:candidateWidgetProperties)
		{
			String candidateWidgetId = widget.getProperty("widget_id", "-1");
			if(widgetId.equals(candidateWidgetId))
			{
				String xpath = widget.getProperty("xpath", null);
				return xpath;
			}
		}
		return null;
	}

	private class FindWebElementsThread extends Thread
	{
		private Locator targetWidget;
		private List<Locator> candidateWidgets;

		public FindWebElementsThread(Locator targetWidget, List<Locator> candidateWidgets)
		{
			this.targetWidget = targetWidget;
			this.candidateWidgets = candidateWidgets;
		}

		public void run()
		{
			similoCalculation(targetWidget, candidateWidgets);
		}
	}

	private Locator similo(Locator targetWidget, List<Locator> candidateWidgets)
	{
//		similoCalculation(targetWidget, candidateWidgets);

		for(Locator candidateWidget:candidateWidgets)
		{
			candidateWidget.setDuration(0);
		}

		// Split candidateWidgets for each thread
		List<List<Locator>> candidateWidgetsList = new ArrayList<List<Locator>>();
		int candidatesPerThread = candidateWidgets.size() / NO_THREADS + 1;
		List<Locator> candidateWidgetsToAdd = new ArrayList<Locator>();
		for(Locator candidateWidget:candidateWidgets)
		{
			candidateWidgetsToAdd.add(candidateWidget);
			if(candidateWidgetsToAdd.size() >= candidatesPerThread)
			{
				candidateWidgetsList.add(candidateWidgetsToAdd);
				candidateWidgetsToAdd = new ArrayList<Locator>();
			}
		}
		if(candidateWidgetsToAdd.size() > 0)
		{
			// Add the reminders
			candidateWidgetsList.add(candidateWidgetsToAdd);
		}
		
		// Create and start threads
		List<Thread> threads = new ArrayList<Thread>();
		for(List<Locator> candidateWidgetsInThread:candidateWidgetsList)
		{
			Locator targetWidgetClone = targetWidget.clone();
			Thread thread=new FindWebElementsThread(targetWidgetClone, candidateWidgetsInThread);
			threads.add(thread);
			thread.start();
		}
		
		// Wait for the threads to complete
		try
		{
			for(Thread thread:threads)
			{
				thread.join();
			}
		}
		catch (InterruptedException e)
		{
		}

		highestDuration = 0;
		for(Locator candidateWidget:candidateWidgets)
		{
			if(candidateWidget.getDuration() > highestDuration)
			{
				highestDuration = candidateWidget.getDuration();
			}
		}
		
		Collections.sort(candidateWidgets);
		Locator bestCandidateWidget = candidateWidgets.get(0);
		return bestCandidateWidget;
	}

	private void similoCalculation(Locator targetWidget, List<Locator> candidateWidgets)
	{
		long startTime = System.currentTimeMillis();
		double bestSimilarityScore = 0;
		for (Locator candidateWidget : candidateWidgets)
		{
			double similarityScore = 0;
			if(candidateWidget.getMaxScore() > bestSimilarityScore)
			{
				similarityScore = calcSimilarityScore(targetWidget, candidateWidget);
			}
			candidateWidget.setScore(similarityScore);
			if(similarityScore > bestSimilarityScore)
			{
				bestSimilarityScore = similarityScore;
			}
			long duration = System.currentTimeMillis() - startTime;
			candidateWidget.setDuration(duration);
		}
	}

	private double calcMaxSimilarityScore(Locator candidateWidget)
	{
		double similarityScore=0;
		int index = 0;
		for (String locator : LOCATORS)
		{
			double weight=WEIGHTS[index];
			String candidateValue = candidateWidget.getMetadata(locator);
			if(candidateValue != null)
			{
				similarityScore += weight;
			}
			index++;
		}
		return similarityScore;
	}

	private double calcSimilarityScore(Locator targetWidget, Locator candidateWidget)
	{
		double similarityScore=0;
		int index = 0;
		for (String locator : LOCATORS)
		{
			double weight=WEIGHTS[index];
			double similarity=0;

			String targetValue = targetWidget.getMetadata(locator);
			String candidateValue = candidateWidget.getMetadata(locator);

			if(targetValue != null && candidateValue != null)
			{
				int similarityFunction = SIMILARITY_FUNCTION[index];
				if(similarityFunction == 1)
				{
					similarity=((double)stringSimilarity(targetValue, candidateValue, 100))/100;
				}
				else if(similarityFunction == 2)
				{
					similarity=((double)integerSimilarity(targetValue, candidateValue, 100))/100;
				}
				else if(similarityFunction == 3)
				{
					// Use 2D distance

					int x = string2Int(targetWidget.getMetadata("x"));
					int y = string2Int(targetWidget.getMetadata("y"));
					int xc = string2Int(candidateWidget.getMetadata("x"));
					int yc = string2Int(candidateWidget.getMetadata("y"));

					int dx = x - xc;
					int dy = y - yc;
					int pixelDistance = (int)Math.sqrt(dx*dx + dy*dy);
					similarity = ((double)Math.max(100 - pixelDistance, 0))/100;
				}
				else if(similarityFunction == 4)
				{
					similarity=((double)neighborTextSimilarity(targetValue, candidateValue, 100))/100;
				}
				else
				{
					similarity=(double)equalSimilarity(targetValue, candidateValue, 1);
				}
			}
			
			similarityScore += similarity * weight;
			index++;
		}
		return similarityScore;
	}

	private int equalSimilarity(String t1, String t2, int maxScore)
	{
		if (t1 != null && t2 != null)
		{
			if (t1.equalsIgnoreCase(t2))
			{
				return maxScore;
			}
		}
		return 0;
	}

	private int integerSimilarity(String t1, String t2, int maxScore)
	{
		int value1 = string2Int(t1);
		int value2 = string2Int(t2);
		return integerSimilarity(value1, value2, maxScore);
	}

	private int integerSimilarity(int value1, int value2, int maxScore)
	{
		int distance = Math.abs(value1 - value2);
		int max = Math.max(value1, value2);
		int score = (max - distance) * maxScore / max;
		return score;
	}

	private int stringSimilarity(String s1, String s2, int maxScore)
	{
		if(s1.length()==0 || s2.length()==0)
		{
			return 0;
		}

		if(s1.equals(s2))
		{
			return maxScore;
		}

		// Make sure s1 is longer (or equal)
		if(s1.length() < s2.length())
		{
			// Swap
			String swap = s1;
			s1 = s2;
			s2 = swap;
		}

		int distance = computeLevenshteinDistance(s1, s2);
		return (s1.length() - distance) * maxScore / s1.length();
	}

	private int computeLevenshteinDistance(String s1, String s2)
	{
		s1 = stripString(s1);
		s2 = stripString(s2);

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++)
		{
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++)
			{
				if (i == 0)
				{
					costs[j] = j;
				}
				else
				{
					if (j > 0)
					{
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
						{
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						}
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
			{
				costs[s2.length()] = lastValue;
			}
		}
		return costs[s2.length()];
	}

	private String stripString(String s)
	{
		StringBuffer stripped = new StringBuffer();
		for(int i=0; i<s.length(); i++)
		{
			char c = s.charAt(i);
			if(Character.isAlphabetic(c) || Character.isDigit(c))
			{
				stripped.append(c);
			}
		}
		String strippedString = stripped.toString();
		return strippedString;
	}
	
	private Properties loadProperties(File file)
	{
		if(propertiesHash.containsKey(file.getName()))
		{
			// File is cached
			return propertiesHash.get(file.getName());
		}
		Properties properties = new Properties();
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String str;
			while ((str = in.readLine()) != null)
			{
				String[] split = str.split("=", 2);
				if (split.length == 2)
				{
					properties.put(split[0], split[1]);
				}
			}
			properties.put("filename", file.getName());
			in.close();
			propertiesHash.put(file.getName(), properties);
			return properties;
		}
		catch (Exception e)
		{
			// File not found
			return null;
		}
	}

	private List<File> getFolders(String folderName)
	{
		if(folderHash.containsKey(folderName))
		{
			// File is cached
			return folderHash.get(folderName);
		}
		List<File> files = new ArrayList<File>();
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles)
		{
			if (file.isDirectory())
			{
				files.add(file);
			}
		}

		folderHash.put(folderName, files);
		return files;
	}

	private List<File> getFolderFiles(File folder)
	{
		List<File> files = new ArrayList<File>();
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles)
		{
			if (file.isFile())
			{
				files.add(file);
			}
		}

		return files;
	}

	public static void main(String[] args)
	{
		new WidgetLocator(args);
	}

	private void log(String text)
	{
		log("WidgetLocatorResults.txt", text);
	}

	private void log(String filename, String text)
	{
		if(!logOn)
		{
			return;
		}
		System.out.println(text);
		writeLine(filename, text);
	}

	private void logTable(String text)
	{
		if(!logOn)
		{
			return;
		}
		writeLine("WidgetLocatorResultsTable.txt", text);
	}

	private void logPerformance(String text)
	{
		if(!logOn)
		{
			return;
		}
		writeLine("Performance.txt", text);
	}

	private void writeLine(String filename, String text)
	{
		String logMessage = text + "\r\n";
		File file = new File(filename);
		try
		{
			FileOutputStream o = new FileOutputStream(file, true);
			o.write(logMessage.getBytes());
			o.close();
		}
		catch (Exception e) {}
	}

	/**
	 * Get all locators that belong to any of the tags in elementsToExtract
	 * @return A list of locators to web elements
	 */
	public List<Locator> getLocators()
	{
		List<Locator> locators=new ArrayList<Locator>();
		
		if(webDriver!=null)
		{
			try
			{
				String javascript = loadTextFile("javascript.js");
				webDriver.manage().timeouts().setScriptTimeout(300, TimeUnit.SECONDS);
				JavascriptExecutor executor = (JavascriptExecutor) webDriver;
				Object object=executor.executeScript(javascript +
					"var result = []; " +
					"var all = document.querySelectorAll('"+elementsToExtract+"'); " +
					"for (var i=0, max=all.length; i < max; i++) { " +
					"    if (elementIsVisible(all[i])) result.push({'tag': all[i].tagName, 'class': all[i].className, 'type': all[i].type, 'name': all[i].name, 'id': all[i].id, 'value': all[i].value, 'href': all[i].href, 'text': all[i].textContent, 'placeholder': all[i].placeholder, 'title': all[i].title, 'alt': all[i].alt, 'x': getXPosition(all[i]), 'y': getYPosition(all[i]), 'width': getMaxWidth(all[i]), 'height': getMaxHeight(all[i]), 'children': all[i].children.length, 'xpath': getXPath(all[i]), 'idxpath': getIdXPath(all[i])}); " +
					"} " +
					" return JSON.stringify(result); ");				
				
				
				String json=object.toString();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(json);

				for(int i=0; i<jsonArray.size(); i++)
				{
					JSONObject jsonObject=(JSONObject)jsonArray.get(i);

					String tag=object2String(jsonObject.get("tag"));
					if(tag!=null)
					{
						tag=tag.toLowerCase();
					}
					String className=object2String(jsonObject.get("class"));
					String type=object2String(jsonObject.get("type"));
					String name=object2String(jsonObject.get("name"));
					String id=object2String(jsonObject.get("id"));
					String value=object2String(jsonObject.get("value"));
					String href=object2String(jsonObject.get("href"));
					String text=object2String(jsonObject.get("text"));
					String placeholder=object2String(jsonObject.get("placeholder"));
					String title=object2String(jsonObject.get("title"));
					String alt=object2String(jsonObject.get("alt"));
					String xpath=object2String(jsonObject.get("xpath"));
					String idxpath=object2String(jsonObject.get("idxpath"));
					String xStr=object2String(jsonObject.get("x"));
					String yStr=object2String(jsonObject.get("y"));
					String widthStr=object2String(jsonObject.get("width"));
					String heightStr=object2String(jsonObject.get("height"));

					int x=string2Int(xStr);
					int y=string2Int(yStr);
					int width=string2Int(widthStr);
					int height=string2Int(heightStr);

					if(width>0 && height>0)
					{
						Locator locator=new Locator();

						locator.setLocationArea(new Rectangle(x, y, width, height));
						locator.setX(x);
						locator.setY(y);
						locator.setWidth(width);
						locator.setHeight(height);

						addMetadata(locator, "tag", tag);
						addMetadata(locator, "class", className);
						addMetadata(locator, "type", type);
						addMetadata(locator, "name", name);
						addMetadata(locator, "id", id);
						addMetadata(locator, "value", value);
						addMetadata(locator, "href", href);
						if(isValidText(text))
						{
							addMetadata(locator, "text", truncate(text));
						}
						addMetadata(locator, "placeholder", placeholder);
						addMetadata(locator, "title", title);
						addMetadata(locator, "alt", alt);
						addMetadata(locator, "xpath", xpath);
						addMetadata(locator, "idxpath", idxpath);
						addMetadata(locator, "x", xStr);
						addMetadata(locator, "y", yStr);
						addMetadata(locator, "height", heightStr);
						addMetadata(locator, "width", widthStr);

						int area = width * height;
						int shape = (width * 100) / height;
						addMetadata(locator, "area", ""+area);
 						addMetadata(locator, "shape", ""+shape);

						String visibleText=locator.getVisibleText();
						if(visibleText!=null)
						{
							locator.putMetadata("visible_text", visibleText);
						}
						String isButton=isButton(tag, type, className)?"yes":"no";;
						locator.putMetadata("is_button", isButton);

						locators.add(locator);
					}
				}
				
				for(Locator locator:locators)
				{
					addNeighborTexts(locator, locators);
					double maxScore = calcMaxSimilarityScore(locator);
					locator.setMaxScore(maxScore);
				}
				
				return locators;
			}
			catch (Exception e)
			{
				return null;
			}
		}

		return null;
	}

	private String truncate(String text)
	{
		if(text==null)
		{
			return null;
		}
		if(text.length()>50)
		{
			return text.substring(0, 49);
		}
		return text;
	}
	
	public Locator getLocatorForElement(String elementXPath)
	{
		List<Locator> locators=getLocators();
		if(locators!=null)
		{
			for(Locator locator:locators)
			{
				String xpath=locator.getMetadata("xpath");
				if(xpath.equals(elementXPath))
				{
					Locator locatorAll = getAllLocatorsForElement(elementXPath);
					if(locatorAll!=null)
					{
						locator.putMetadata("ide", locatorAll.getMetadata("ide"));
						locator.putMetadata("robula", locatorAll.getMetadata("robula"));
						locator.putMetadata("montoto", locatorAll.getMetadata("montoto"));
					}
					return locator;
				}
			}
		}
		return null;
	}
	
	public Locator getAllLocatorsForElement(String elementXPath)
	{
		List<Locator> locators=new ArrayList<Locator>();
		
		if(webDriver!=null)
		{
			try
			{
				String javascript = loadTextFile("javascript.js");
				webDriver.manage().timeouts().setScriptTimeout(300, TimeUnit.SECONDS);
				JavascriptExecutor executor = (JavascriptExecutor) webDriver;
				Object object=executor.executeScript(javascript +
					"var result = []; " +
					"var all = []; " +
					"var element = locateElementByXPath('"+elementXPath+"'); " +
					"if (element!=null) all.push(element); " +
					"for (var i=0, max=all.length; i < max; i++) { " +
					"    result.push({'tag': all[i].tagName, 'class': all[i].className, 'type': all[i].type, 'name': all[i].name, 'id': all[i].id, 'value': all[i].value, 'href': all[i].href, 'text': all[i].textContent, 'placeholder': all[i].placeholder, 'title': all[i].title, 'alt': all[i].alt, 'x': getXPosition(all[i]), 'y': getYPosition(all[i]), 'width': getMaxWidth(all[i]), 'height': getMaxHeight(all[i]), 'children': all[i].children.length, 'robula': getRobulaPlusXPath(all[i]), 'montoto': getMonotoXPath(all[i]), 'ide': getSeleniumIDELocator(all[i]), 'xpath': getXPath(all[i]), 'idxpath': getIdXPath(all[i])}); " +
					"} " +
					" return JSON.stringify(result); ");				
				
				
				String json=object.toString();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(json);

				if(jsonArray.size()<1)
				{
					return null;
				}
				
				for(int i=0; i<jsonArray.size(); i++)
				{
					JSONObject jsonObject=(JSONObject)jsonArray.get(i);

					String tag=object2String(jsonObject.get("tag"));
					if(tag!=null)
					{
						tag=tag.toLowerCase();
					}
					String className=object2String(jsonObject.get("class"));
					String type=object2String(jsonObject.get("type"));
					String name=object2String(jsonObject.get("name"));
					String id=object2String(jsonObject.get("id"));
					String value=object2String(jsonObject.get("value"));
					String href=object2String(jsonObject.get("href"));
					String text=object2String(jsonObject.get("text"));
					String placeholder=object2String(jsonObject.get("placeholder"));
					String title=object2String(jsonObject.get("title"));
					String alt=object2String(jsonObject.get("alt"));
					String xpath=object2String(jsonObject.get("xpath"));
					String idxpath=object2String(jsonObject.get("idxpath"));
					String ide=object2String(jsonObject.get("ide"));
					String robula=object2String(jsonObject.get("robula"));
					String montoto=object2String(jsonObject.get("montoto"));
					String xStr=object2String(jsonObject.get("x"));
					String yStr=object2String(jsonObject.get("y"));
					String widthStr=object2String(jsonObject.get("width"));
					String heightStr=object2String(jsonObject.get("height"));

					int x=string2Int(xStr);
					int y=string2Int(yStr);
					int width=string2Int(widthStr);
					int height=string2Int(heightStr);

					if(width>0 && height>0)
					{
						Locator locator=new Locator();

						locator.setLocationArea(new Rectangle(x, y, width, height));
						locator.setX(x);
						locator.setY(y);
						locator.setWidth(width);
						locator.setHeight(height);

						addMetadata(locator, "tag", tag);
						addMetadata(locator, "class", className);
						addMetadata(locator, "type", type);
						addMetadata(locator, "name", name);
						addMetadata(locator, "id", id);
						addMetadata(locator, "value", value);
						addMetadata(locator, "href", href);
						if(isValidText(text))
						{
							addMetadata(locator, "text", text);
						}
						addMetadata(locator, "placeholder", placeholder);
						addMetadata(locator, "title", title);
						addMetadata(locator, "alt", alt);
						addMetadata(locator, "xpath", xpath);
						addMetadata(locator, "idxpath", idxpath);
						addMetadata(locator, "x", xStr);
						addMetadata(locator, "y", yStr);
						addMetadata(locator, "height", heightStr);
						addMetadata(locator, "width", widthStr);

						int area = width * height;
						int shape = (width * 100) / height;
						addMetadata(locator, "area", ""+area);
 						addMetadata(locator, "shape", ""+shape);

						addMetadata(locator, "ide", ide);
						addMetadata(locator, "robula", robula);
						addMetadata(locator, "montoto", montoto);

						String visibleText=locator.getVisibleText();
						if(visibleText!=null)
						{
							locator.putMetadata("visible_text", visibleText);
						}
						String isButton=isButton(tag, type, className)?"yes":"no";;
						locator.putMetadata("is_button", isButton);
						
						locators.add(locator);
					}
				}

				if(locators.size()!=1)
				{
					return null;
				}
				return locators.get(0);
			}
			catch (Exception e)
			{
				return null;
			}
		}

		return null;
	}

	public Locator getXPathLocatorForElement(String elementXPath)
	{
		List<Locator> locators=new ArrayList<Locator>();
		
		if(webDriver!=null)
		{
			try
			{
				String javascript = loadTextFile("javascript.js");
				webDriver.manage().timeouts().setScriptTimeout(300, TimeUnit.SECONDS);
				JavascriptExecutor executor = (JavascriptExecutor) webDriver;
				Object object=executor.executeScript(javascript +
					"var result = []; " +
					"var all = []; " +
					"var element = locateElementByXPath(\""+elementXPath+"\"); " +
					"if (element!=null) all.push(element); " +
					"for (var i=0, max=all.length; i < max; i++) { " +
					"    result.push({'tag': all[i].tagName, 'class': all[i].className, 'type': all[i].type, 'name': all[i].name, 'id': all[i].id, 'value': all[i].value, 'href': all[i].href, 'text': all[i].textContent, 'placeholder': all[i].placeholder, 'title': all[i].title, 'alt': all[i].alt, 'x': getXPosition(all[i]), 'y': getYPosition(all[i]), 'width': getMaxWidth(all[i]), 'height': getMaxHeight(all[i]), 'children': all[i].children.length, 'xpath': getXPath(all[i]), 'idxpath': getIdXPath(all[i])}); " +
					"} " +
					" return JSON.stringify(result); ");		

				String json=object.toString();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(json);

				if(jsonArray.size()<1)
				{
					return null;
				}
				
				for(int i=0; i<jsonArray.size(); i++)
				{
					JSONObject jsonObject=(JSONObject)jsonArray.get(i);

					String tag=object2String(jsonObject.get("tag"));
					if(tag!=null)
					{
						tag=tag.toLowerCase();
					}
					String className=object2String(jsonObject.get("class"));
					String type=object2String(jsonObject.get("type"));
					String name=object2String(jsonObject.get("name"));
					String id=object2String(jsonObject.get("id"));
					String value=object2String(jsonObject.get("value"));
					String href=object2String(jsonObject.get("href"));
					String text=object2String(jsonObject.get("text"));
					String placeholder=object2String(jsonObject.get("placeholder"));
					String title=object2String(jsonObject.get("title"));
					String alt=object2String(jsonObject.get("alt"));
					String xpath=object2String(jsonObject.get("xpath"));
					String idxpath=object2String(jsonObject.get("idxpath"));
					String xStr=object2String(jsonObject.get("x"));
					String yStr=object2String(jsonObject.get("y"));
					String widthStr=object2String(jsonObject.get("width"));
					String heightStr=object2String(jsonObject.get("height"));

					int x=string2Int(xStr);
					int y=string2Int(yStr);
					int width=string2Int(widthStr);
					int height=string2Int(heightStr);

					if(width>0 && height>0)
					{
						Locator locator=new Locator();

						locator.setLocationArea(new Rectangle(x, y, width, height));
						locator.setX(x);
						locator.setY(y);
						locator.setWidth(width);
						locator.setHeight(height);

						addMetadata(locator, "tag", tag);
						addMetadata(locator, "class", className);
						addMetadata(locator, "type", type);
						addMetadata(locator, "name", name);
						addMetadata(locator, "id", id);
						addMetadata(locator, "value", value);
						addMetadata(locator, "href", href);
						if(isValidText(text))
						{
							addMetadata(locator, "text", text);
						}
						addMetadata(locator, "placeholder", placeholder);
						addMetadata(locator, "title", title);
						addMetadata(locator, "alt", alt);
						addMetadata(locator, "xpath", xpath);
						addMetadata(locator, "idxpath", idxpath);
						addMetadata(locator, "x", xStr);
						addMetadata(locator, "y", yStr);
						addMetadata(locator, "height", heightStr);
						addMetadata(locator, "width", widthStr);

						int area = width * height;
						int shape = (width * 100) / height;
						addMetadata(locator, "area", ""+area);
 						addMetadata(locator, "shape", ""+shape);

						String visibleText=locator.getVisibleText();
						if(visibleText!=null)
						{
							locator.putMetadata("visible_text", visibleText);
						}
						String isButton=isButton(tag, type, className)?"yes":"no";;
						locator.putMetadata("is_button", isButton);
						
						locators.add(locator);
					}
				}

				if(locators.size()!=1)
				{
					return null;
				}
				return locators.get(0);
			}
			catch (Exception e)
			{
				return null;
			}
		}

		return null;
	}

	private void addMetadata(Locator locator, String key, String value)
	{
		if(value!=null && value.length()>0)
		{
			String lowercaseValue = value.toLowerCase();
			locator.putMetadata(key, lowercaseValue);
		}
	}

	private void addNeighborTexts(Locator locator, List<Locator> availableLocators)
	{
		if(locator.getLocationArea()==null)
		{
			return;
		}
		Rectangle r = locator.getLocationArea();
		if(r.height>100 || r.width > 600)
		{
			return;
		}
		Rectangle largerRectangle = new Rectangle(r.x-50, r.y-50, r.width+100, r.height+100);

		List<Locator> neighbors = new ArrayList<Locator>();
		for(Locator available:availableLocators)
		{
			if(locator!=available && available.getLocationArea()!=null)
			{
				Rectangle rect = available.getLocationArea();
				if(rect.getHeight()<=100 && largerRectangle.intersects(rect))
				{
			  	neighbors.add(available);
				}
			}
		}
		
		List<String> words = new ArrayList<String>();
		Properties wordHash = new Properties();
		for(Locator neighbor:neighbors)
		{
			String visibleText=neighbor.getVisibleText();
			if(visibleText != null)
			{
				String[] visibleWords = visibleText.split("\\s+");
				for(String visibleWord:visibleWords)
				{
					String visibleWordLower = visibleWord.toLowerCase();
					if(!wordHash.containsKey(visibleWordLower))
					{
						wordHash.put(visibleWordLower, true);
						words.add(visibleWordLower);
					}
				}
			}
		}

		StringBuffer wordString = new StringBuffer();
		for(String word:words)
		{
			if(wordString.length()>0)
			{
				wordString.append(" ");
			}
			wordString.append(word);
		}

		if(wordString.length()>0)
		{
			String text = wordString.toString();
			locator.putMetadata("neighbor_text", text);
		}
	}
	
	private boolean containsWord(String containsWord, String[] words)
	{
		for(String word:words)
		{
			if(containsWord.length() < word.length() && (word.startsWith(containsWord) || word.endsWith(containsWord)))
			{
				return true;
			}
			else if(word.length() < containsWord.length() && (containsWord.startsWith(word) || containsWord.endsWith(word)))
			{
				return true;
			}
			else if(containsWord.equals(word))
			{
				return true;
			}
		}
		return false;
	}

	private int neighborTextSimilarity(String text1, String text2, int maxScore)
	{
		if(text1.length()==0 || text2.length()==0)
		{
			return 0;
		}

		String[] words1 = text1.split("\\s+");
		String[] words2 = text2.split("\\s+");
		
		int existsCount = 0;
		int wordCount = Math.max(text1.length() - words1.length + 1, text2.length() - words2.length + 1);
		for(String word1:words1)
		{
			if(containsWord(word1, words2))
			{
				existsCount += word1.length();
			}
		}
		int score = Math.min((existsCount * maxScore) / wordCount, 100);
		return score;
	}

	private String object2String(Object o)
	{
		if(o==null)
		{
			return null;
		}
		if(o instanceof String)
		{
			String s=(String)o;
			return s.trim();
		}
		else if(o instanceof Integer)
		{
			Integer i=(Integer)o;
			return i.toString();
		}
		if(o instanceof Double)
		{
			Double d=(Double)o;
			int i=d.intValue();
			return ""+i;
		}
		else if(o instanceof Long)
		{
			Long l=(Long)o;
			return l.toString();
		}
		return null;
	}

	private int string2Int(String text)
	{
		try
		{
			return Integer.parseInt(text);
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	private boolean isValidText(String text)
	{
		if(text==null)
		{
			return false;
		}
		String trimmedText=text.trim();
		if(trimmedText.length()<3 || trimmedText.length()>50)
		{
			// Too short or too long
			return false;
		}
		if(trimmedText.indexOf('\n')>=0)
		{
			// Contains newline
			return false;
		}
		if(trimmedText.indexOf('\t')>=0)
		{
			// Contains tab
			return false;
		}
		return true;
	}

	private boolean isButton(String tag, String type, String className)
	{
		if(tag==null)
		{
			return false;
		}
		if(tag.equalsIgnoreCase("a") && className!=null && className.indexOf("btn")>=0)
		{
			return true;
		}
		if(tag.equalsIgnoreCase("button"))
		{
			return true;
		}
		if(tag.equalsIgnoreCase("input") && ("button".equalsIgnoreCase(type) || "submit".equalsIgnoreCase(type) || "reset".equalsIgnoreCase(type)))
		{
			return true;
		}
		return false;
	}

	private List<String> readLines(File file)
	{
		List<String> lines=new ArrayList<String>();
		try
		{
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine())
			{
				String line=scanner.nextLine().trim();
				if(line.length()>0)
				{
					lines.add(line);
				}
			}
			scanner.close();
		}
		catch (Exception e)
		{
		}
		return lines;
	}

	private String loadTextFile(String filename)
	{
		File file=new File(filename);
		if(file.exists())
		{
			List<String> lines=readLines(file);
			StringBuffer buf=new StringBuffer();
			for(String line:lines)
			{
				buf.append(line);
				buf.append("\n");
			}
			return buf.toString();
		}
		return null;
	}

	/**
	 * Delay the thread a number of milliseconds
	 * @param milliseconds
	 */
	public void delay(int milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e)
		{
		}
	}
}
