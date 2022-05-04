package widgetlocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class WidgetLocatorOld
{
	private static String[] LOCATORS = { "tag", "class", "name", "id", "text", "xpath", "idxpath" };
	private static int FIRST_LOCATOR_NO = 0;
	private static Boolean[] USE_LEVENSHTEIN = { false, true, false, false, true, true, true };
	private static String[] TAGS = { "A", "SPAN", "LI", "P", "H1", "H2", "H3", "H4", "H5", "INPUT", "IMG", "BUTTON", "SELECT", "TD", "SVG", "TR", "LABEL", "TH" };
	private enum SimiloVariant {EQUALS, LEVENSHTEIN, WEIGHTED_LEVENSHTEIN};
	private static Hashtable<String, List<File>> folderHash = new Hashtable<String, List<File>>();
	private static Hashtable<String, Properties> propertiesHash = new Hashtable<String, Properties>();

	// Initial weights and threshold
	private Integer[] WEIGHTS = { 30, 50, 100, 100, 100, 30, 30 };
	private int SIMILO_THRESHOLD = 100;

	// After Hill-Climbing (100 iterations)
//	private Integer[] WEIGHTS = { 24, 46, 89, 90, 96, 34, 21 };
//	private int SIMILO_THRESHOLD = 108;

	private boolean logOn = true;
	private static boolean ITERATE_WEIGHTS = false;

	public WidgetLocatorOld(String[] args)
	{
		if(ITERATE_WEIGHTS)
		{
			iterateWeights(100, 11);
		}
		else
		{
			locateWebElements(FIRST_LOCATOR_NO);
		}
	}
	
	private void iterateWeights(int noIterations, int optimizeLocatorNo)
	{
		Random rand = new Random();
		logOn = false;
		long lowestLocated = 1000000;
		for(int iteration=0; iteration<noIterations; iteration++)
		{
			// Copy weights and threshold
			if(iteration%10==0)
			{
				System.out.print(iteration+" ");
			}
			Integer[] previousWeights = new Integer[7];
			for(int i=0; i<WEIGHTS.length; i++)
			{
				previousWeights[i] = WEIGHTS[i];
			}
			int oldThreshold = SIMILO_THRESHOLD;

			if(iteration>0)
			{
				// Mutate weights and threshold (only calculate the weights the first iteration)

				for(int i=0; i<WEIGHTS.length; i++)
				{
					if(WEIGHTS[i]<=5)
					{
						WEIGHTS[i] += rand.nextInt(5);
					}
					else
					{
						WEIGHTS[i] += (rand.nextInt(10)-5);
					}
				}

				if(SIMILO_THRESHOLD<=5)
				{
					SIMILO_THRESHOLD += rand.nextInt(5);
				}
				else
				{
					SIMILO_THRESHOLD += (rand.nextInt(10)-5);
				}
			}

			long nonLocated = locateWebElements(optimizeLocatorNo);
			if(nonLocated<lowestLocated)
			{
				// Keep the weights
				lowestLocated = nonLocated;
			}
			else
			{
				// Restore the weights and threshold
				for(int i=0; i<WEIGHTS.length; i++)
				{
					WEIGHTS[i] = previousWeights[i];
				}
				SIMILO_THRESHOLD = oldThreshold;
			}
		}
		System.out.println("");

		// Log the resulting weights
		logOn = true;
		String weightText="";
		for(int i=0; i<WEIGHTS.length; i++)
		{
			if(i>0)
			{
				weightText+=", ";
			}
			weightText+=WEIGHTS[i];
		}
		log("weights: "+weightText);
		log("threshold: "+SIMILO_THRESHOLD);
	}

	private long locateWebElements(int startLocatorNo)
	{
		try
		{
			logAllTags();
			for (int locatorType = startLocatorNo; locatorType <= 11; locatorType++)
			{
				log("");
				logTable("");
				if (locatorType < 7)
				{
					log("***** Locator Type: "+LOCATORS[locatorType]+" *****");
					logTable("Locator Type: "+LOCATORS[locatorType]);
					logPerformance("Locator Type: "+LOCATORS[locatorType]);
				}
				else if (locatorType == 7)
				{
					log("***** Locator Type: Multi Locator 1 (weighted) *****");
					logTable("Locator Type: Multi Locator 1 (weighted)");
					logPerformance("Locator Type: Multi Locator 1 (weighted)");
				}
				else if (locatorType == 8)
				{
					log("***** Locator Type: Multi Locator 2 (theoretical limit) *****");
					logTable("Locator Type: Multi Locator 2 (theoretical limit)");
					logPerformance("Locator Type: Multi Locator 2 (theoretical limit)");
				}
				else if (locatorType == 9)
				{
					log("***** Locator Type: Similo 1 (equalsIgnoreCase) *****");
					logTable("Locator Type: Similo 1 (equalsIgnoreCase)");
					logPerformance("Locator Type: Similo 1 (equalsIgnoreCase)");
				}
				else if (locatorType == 10)
				{
					log("***** Locator Type: Similo 2 (Levenshtein) *****");
					logTable("Locator Type: Similo 2 (Levenshtein)");
					logPerformance("Locator Type: Similo 2 (Levenshtein)");
				}
				else if (locatorType == 11)
				{
					log("***** Locator Type: Similo 3 (weighted Levenshtein) *****");
					logTable("Locator Type: Similo 3 (weighted Levenshtein)");
					logPerformance("Locator Type: Similo 3 (weighted Levenshtein)");
				}

				int totalLocated = 0;
				int totalNotLocated = 0;
				int totalIncorrectlyLocated = 0;
				long totalLocationTime = 0;
				String tableRowApp = "";
				String tableRowLocated = "";
				String tableRowNotLocated = "";
				String tableRowIncorrectlyLocated = "";
				Hashtable<String, Integer> tagCount=new Hashtable<String, Integer>();

				List<File> apps = getFolders("apps");
				for (File app : apps)
				{
					int located = 0;
					int notLocated = 0;
					int incorrectlyLocated = 0;
					long locationTime = 0;
					Hashtable<String, Integer> tagCountApp=new Hashtable<String, Integer>();
					
					log("Application: " + app.getName());
					File targetWidgetsFolder = new File(app, "target_widgets");
					File candidateWidgetsFolder = new File(app, "candidate_widgets");
					List<File> targetFolderFiles = getFolderFiles(targetWidgetsFolder);
					List<File> candidateFolderFiles = getFolderFiles(candidateWidgetsFolder);
					for (File targetWidget : targetFolderFiles)
					{
						Properties targetWidgetProperty = loadProperties(targetWidget);
						List<Properties> candidateWidgetProperties = new ArrayList<Properties>();
						for (File candidateWidget : candidateFolderFiles)
						{
							Properties candidateWidgetProperty = loadProperties(candidateWidget);
							candidateWidgetProperties.add(candidateWidgetProperty);

							if (locatorType == 0)
							{
								String tag = candidateWidgetProperty.getProperty("tag", null);
								if(tag!=null)
								{
									countTag(tagCount, tag);
									countTag(tagCountApp, tag);
								}
							}
						}
						long startTime = System.currentTimeMillis();

						Properties bestCandidate=null;
						if (locatorType < 7)
						{
							bestCandidate = singleLocator(LOCATORS[locatorType], targetWidgetProperty, candidateWidgetProperties);
						}
						else if (locatorType == 7)
						{
							bestCandidate = multiLocator1(targetWidgetProperty, candidateWidgetProperties);
						}
						else if (locatorType == 8)
						{
							bestCandidate = multiLocator2(targetWidgetProperty, candidateWidgetProperties);
						}
						else if (locatorType == 9)
						{
							bestCandidate = similo(targetWidgetProperty, candidateWidgetProperties, SimiloVariant.EQUALS);
						}
						else if (locatorType == 10)
						{
							bestCandidate = similo(targetWidgetProperty, candidateWidgetProperties, SimiloVariant.LEVENSHTEIN);
						}
						else if (locatorType == 11)
						{
							bestCandidate = similo(targetWidgetProperty, candidateWidgetProperties, SimiloVariant.WEIGHTED_LEVENSHTEIN);
						}

						long deltaTime = System.currentTimeMillis() - startTime;
						locationTime += deltaTime;
						totalLocationTime += deltaTime;
						if (bestCandidate == null)
						{
							// Not located
							log("No candidate for target widget: " + targetWidget.getName());
							if(locatorType < 7)
							{
								// Single-locator - only count if property exists
								String propertyValue = targetWidgetProperty.getProperty(LOCATORS[locatorType], null);
								if(propertyValue!=null)
								{
									// Property exists
									notLocated++;
									totalNotLocated++;
								}
							}
							else
							{
								notLocated++;
								totalNotLocated++;
							}
						}
						else
						{
							// There is a candidate - check if the algorithm found the correct one
							String widgetId = targetWidgetProperty.getProperty("widget_id", "0");
							String bestWidgetId = bestCandidate.getProperty("widget_id", "0");
							if (widgetId.equals(bestWidgetId))
							{
								// Correctly located
								log("Target widget: " + targetWidget.getName() + " (id: " + widgetId + ") located candidate widget: " + bestCandidate.getProperty("filename", "No filename") + " (id: " + bestWidgetId + ")");
								located++;
								totalLocated++;
							}
							else
							{
								// Incorrectly located
								log("Target widget: " + targetWidget.getName() + " (id: " + widgetId + ") incorrectly located candidate widget: " + bestCandidate.getProperty("filename", "No filename") + " (id: " + bestWidgetId + ")");
								incorrectlyLocated++;
								totalIncorrectlyLocated++;

								if (locatorType == 11 && app.getName().equalsIgnoreCase("Aliexpress"))
								{
									log("Hello");
								}

							}
						}
					}
					log("Located: " + located + ", not located: " + notLocated + ", incorrectly located: " + incorrectlyLocated + " (time in ms: " + locationTime + ")");
					tableRowApp += ""+app.getName()+"\t";
					tableRowLocated += ""+located+"\t";
					tableRowNotLocated += ""+notLocated+"\t";
					tableRowIncorrectlyLocated += ""+incorrectlyLocated+"\t";
					if (locatorType == 0)
					{
						logTagCount(app.getName(), tagCountApp);
					}
				}

				log("Total results; located: " + totalLocated + ", not located: " + totalNotLocated + ", incorrectly located: " + totalIncorrectlyLocated + " (time in ms: " + totalLocationTime + ")");
				logPerformance("Total time in ms: " + totalLocationTime);
				logTable("App:\t"+tableRowApp+"Total");
				logTable("Located (TP):\t"+tableRowLocated+totalLocated);
				logTable("Not located (FN):\t"+tableRowNotLocated+totalNotLocated);
				logTable("Incorrectly located (FP):\t"+tableRowIncorrectlyLocated+totalIncorrectlyLocated);
				if (locatorType == 0)
				{
					logTagCount("Total", tagCount);
				}

				if(startLocatorNo>7)
				{
					return totalNotLocated + totalIncorrectlyLocated * 2;
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

	/**
	 * Multi-Locator (weighted)
	 * @param targetWidget
	 * @param candidateWidgets
	 * @return The best match
	 */
	private Properties multiLocator1(Properties targetWidget, List<Properties> candidateWidgets)
	{
		List<Properties> candidates = new ArrayList<Properties>();

		// Try to locate widget using all locators
		int i=0;
		for (String locator : LOCATORS)
		{
			if(i>=FIRST_LOCATOR_NO)
			{
				Properties candidate = singleLocator(locator, targetWidget, candidateWidgets);
				if (candidate != null)
				{
					addVote(candidate, WEIGHTS[i]);
					if (!candidates.contains(candidate))
					{
						candidates.add(candidate);
					}
				}
			}
			i++;
		}

		// Pick the widget with most votes (or the first of the most voted in case of a tie)
		Properties bestCandidateWidget = null;
		int bestCandidateVotes = 0;
		for (Properties candidateWidget : candidates)
		{
			int votes = getVotes(candidateWidget);
			if (votes > bestCandidateVotes)
			{
				bestCandidateVotes = votes;
				bestCandidateWidget = candidateWidget;
			}
		}

		return bestCandidateWidget;
	}

	/**
	 * Multi-Locator (theoretical limit)
	 * @param targetWidget
	 * @param candidateWidgets
	 * @return The best match
	 */
	private Properties multiLocator2(Properties targetWidget, List<Properties> candidateWidgets)
	{
		List<Properties> candidates = new ArrayList<Properties>();

		// Try to locate widget using all locators
		int i=0;
		for (String locator : LOCATORS)
		{
			if(i>=FIRST_LOCATOR_NO)
			{
				Properties candidate = singleLocator(locator, targetWidget, candidateWidgets);
				if (candidate != null)
				{
					if (!candidates.contains(candidate))
					{
						candidates.add(candidate);
					}
				}
			}
			i++;
		}

		// Determine if the correct one exists among the candidates
		for (Properties candidateWidget : candidates)
		{
			String widgetId = targetWidget.getProperty("widget_id", "-1");
			String bestWidgetId = candidateWidget.getProperty("widget_id", "-2");
			if (widgetId.equals(bestWidgetId))
			{
				return candidateWidget;
			}
		}
		
		if(candidates.size()>0)
		{
			// None of the candidates are correct - just pick the first
			return candidates.get(0);
		}

		return null;
	}

	/**
	 * @param locator
	 * @param targetWidget
	 * @param candidateWidgets
	 * @return One unique match or null if none or more than one match
	 */
	private Properties singleLocator(String locator, Properties targetWidget, List<Properties> candidateWidgets)
	{
		List<Properties> matches = new ArrayList<Properties>();
		for (Properties candidateWidget : candidateWidgets)
		{
			if(compareEqual(locator, targetWidget, candidateWidget, 100)==100)
			{
				// Found a match
				matches.add(candidateWidget);
			}
		}

		if (matches.size() == 0 || matches.size() > 1)
		{
			// Zero or more than one match - not unique
			return null;
		}

		return matches.get(0);
	}

	private void addVote(Properties widget, int weight)
	{
		int votes = Integer.parseInt(widget.getProperty("votes", "0"));
		votes += weight;
		widget.setProperty("votes", ""+votes);
	}

	private int getVotes(Properties widget)
	{
		int votes = Integer.parseInt(widget.getProperty("votes", "0"));
		return votes;
	}

	private Properties similo(Properties targetWidget, List<Properties> candidateWidgets, SimiloVariant similoVariant)
	{
		Properties bestCandidateWidget = null;
		int bestSimilarityScore = 0;
		for (Properties candidateWidget : candidateWidgets)
		{
			int similarityScore = 0;
			if(similoVariant == SimiloVariant.EQUALS)
			{
				similarityScore = calcScore1(targetWidget, candidateWidget);
			}
			else if(similoVariant == SimiloVariant.LEVENSHTEIN)
			{
				similarityScore = calcScore2(targetWidget, candidateWidget);
			}
			else
			{
				similarityScore = calcScore3(targetWidget, candidateWidget);
			}
			if (similarityScore > bestSimilarityScore)
			{
				bestCandidateWidget = candidateWidget;
				bestSimilarityScore = similarityScore;
			}
		}
		return bestCandidateWidget;
	}

	private int calcScore1(Properties targetWidget, Properties candidateWidget)
	{
		int similarityScore = 0;
		for (int i=0; i<LOCATORS.length; i++)
		{
			if(i>=FIRST_LOCATOR_NO)
			{
				similarityScore += compareEqual(LOCATORS[i], targetWidget, candidateWidget, 100);
			}
		}
		return similarityScore;
	}

	private int calcScore2(Properties targetWidget, Properties candidateWidget)
	{
		int similarityScore = 0;
		for (int i=0; i<LOCATORS.length; i++)
		{
			if(i>=FIRST_LOCATOR_NO)
			{
				if(USE_LEVENSHTEIN[i])
				{
					similarityScore += compareDistance(LOCATORS[i], targetWidget, candidateWidget, 100);
				}
				else
				{
					similarityScore += compareEqual(LOCATORS[i], targetWidget, candidateWidget, 100);
				}
			}
		}
		return similarityScore;
	}

	private int calcScore3(Properties targetWidget, Properties candidateWidget)
	{
		int similarityScore = 0;
		for (int i=0; i<LOCATORS.length; i++)
		{
			if(i>=FIRST_LOCATOR_NO)
			{
				if(USE_LEVENSHTEIN[i])
				{
					similarityScore += compareDistance(LOCATORS[i], targetWidget, candidateWidget, WEIGHTS[i]);
				}
				else
				{
					similarityScore += compareEqual(LOCATORS[i], targetWidget, candidateWidget, WEIGHTS[i]);
				}
			}
		}
		if(similarityScore < SIMILO_THRESHOLD)
		{
			// Below threshold
			return 0;
		}
		return similarityScore;
	}

	private int compareEqual(String propertyName, Properties targetWidgetProperty, Properties candidateWidgetProperty, int maxScore)
	{
		String target = targetWidgetProperty.getProperty(propertyName, null);
		String candidate = candidateWidgetProperty.getProperty(propertyName, null);
		if (target != null && candidate != null)
		{
			if (target.equalsIgnoreCase(candidate))
			{
				return maxScore;
			}
		}
		return 0;
	}

	private int compareDistance(String propertyName, Properties targetWidgetProperty, Properties candidateWidgetProperty, int maxScore)
	{
		String target = targetWidgetProperty.getProperty(propertyName, null);
		String candidate = candidateWidgetProperty.getProperty(propertyName, null);
		if (target != null && candidate != null)
		{
			return compareDistance(target, candidate, maxScore);
		}
		return 0;
	}

	private int compareDistance(String t1, String t2, int maxScore)
	{
		String s1 = t1.toLowerCase();
		String s2 = t2.toLowerCase();

		if (s1.equals(s2))
		{
			return maxScore;
		}

		// Make sure s1 is longer (or equal)
		if (s1.length() < s2.length())
		{
			String swap = s1;
			s1 = s2;
			s2 = swap;
		}

		int editDistance = 0;
		int bigLen = s1.length();
		editDistance = computeDistance(s1, s2);
		if (bigLen == 0)
		{
			return maxScore;
		}
		else
		{
			int score = (bigLen - editDistance) * maxScore / bigLen;
			return score;
		}

	}

	private int computeDistance(String s1, String s2)
	{
		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++)
		{
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++)
			{
				if (i == 0)
				{
					costs[j] = j;
				} else
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
		new WidgetLocatorOld(args);
	}

	private void log(String text)
	{
		if(!logOn)
		{
			return;
		}
		System.out.println(text);
		writeLine("WidgetLocatorResults.txt", text);
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
		writeLine("WidgetLocatorPerformance.txt", text);
	}

	private void logTags(String text)
	{
		if(!logOn)
		{
			return;
		}
		System.out.println(text);
		writeLine("TagResults.txt", text);
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
	
	private void countTag(Hashtable<String, Integer> tagCount, String tag)
	{
		tag = tag.toUpperCase();
		Integer count=tagCount.get(tag);
		if(count==null)
		{
			tagCount.put(tag, 1);
		}
		else
		{
			tagCount.put(tag, count+1);
		}
	}

	private void logAllTags()
	{
		String tags = "\t";
		for(String tag:TAGS)
		{
			tags += tag + "\t";
		}
		logTags(tags);
	}

	private void logTagCount(String app, Hashtable<String, Integer> tagCount)
	{
		String values = app+"\t";
		for(String tag:TAGS)
		{
			Integer count=tagCount.get(tag);
			if(count==null)
			{
				values += "0\t";
			}
			else
			{
				values += count + "\t";
			}
		}
		logTags(values);
	}
}
