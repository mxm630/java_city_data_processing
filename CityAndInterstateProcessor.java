import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CityAndInterstateProcessor --- program compiles reports on population and interstate data for give cities
 * @author    Maksim Rudenko
 */
public class CityAndInterstateProcessor {
	public static final String POPULATION_OUTPUT_FILE_NAME = "Cities_By_Population.txt";
	public static final String INTERSTATES_OUTPUT_FILE_NAME = "Interstates_By_City.txt";

	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("Usage: " + CityAndInterstateProcessor.class.getName() + " <command> <feed filename>");				
		}
		else {
			// Parsing the input
			HashSet<City> cities = CityFeedParser.parseCsvFile(args[0]);				
			HashMap<Integer, HashSet<City>> citiesByPopulatonMap = new HashMap<Integer, HashSet<City>>();
			TreeMap<Integer, Integer> countersByInterstatesMap = new TreeMap<Integer, Integer>();

			// Processing data (separated from parsing so it can be reused for Option 2)
			for (City city: cities){
				HashSet<City> cityList = citiesByPopulatonMap.get(city.getPopulation());
				if(cityList == null) {
					cityList = new HashSet<City>();
					citiesByPopulatonMap.put(city.getPopulation(), cityList);
				}
				cityList.add(city);
				for(Integer hID: city.getHighwayIDs()) {
					Integer counter = countersByInterstatesMap.get(hID);
					if (counter == null) {
						countersByInterstatesMap.put(hID, new Integer(1));
					}
					else {
						countersByInterstatesMap.put(hID, counter + 1);
					}
				}
			}

			// Sorting and formating the output
			producePopulationFile(citiesByPopulatonMap);
			produceInterstatesFile(countersByInterstatesMap);
		}
	}

	/**
	 * Creates output file of the following format:
	 *     [Interstate] [Number of cities]
	 * The output is sorted by interstate number ascending.
	 * 
	 * @param countersByInterstatesMap interstate occurrences counts indexed by interstate IDs
	 */
	private static void produceInterstatesFile(TreeMap<Integer, Integer> countersByInterstatesMap) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(INTERSTATES_OUTPUT_FILE_NAME), "utf-8"));
			// highwayCounters are iterated in ascending order due to the chosen TreeMap implementation
			for(Integer key: countersByInterstatesMap.keySet()) {
				writer.write(CityFeedParser.INTERSTATE_PREFIX + key + " " + countersByInterstatesMap.get(key) + "\n");				
			}	
		} catch (IOException ex) {
			Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Creates output file of the following format:
	 *     [Population]
	 *     (newline)
	 *     [City], [State]
	 *     Interstates: <Comma-separated list of interstates, sorted by interstate number ascending>
	 *     (newline)
	 * The output is sorted from highest population to lowest. If there are multiple cities with the same population, 
	 * they are groupped them under a single [Population] heading and sorted alphabetically by state and then city.
	 * 
	 * @param citiesByPopulatonMap map of city objects grouped and indexed by population
	 */
	private static void producePopulationFile(HashMap<Integer, HashSet<City>> citiesByPopulatonMap) {

		// output-specific sorting mechanism
		class CityComparator implements Comparator<City> {
			public int compare(City c1, City c2) {
				int rtn = c1.getState().compareTo(c2.getState());
				if(rtn == 0) {
					rtn = c1.getName().compareTo(c2.getName());
				}
				return rtn;
			}
		}
		LinkedList<City> citySortedList;
		CityComparator cityComparator = new CityComparator ();
		LinkedList<Integer> pList = new LinkedList<Integer>(citiesByPopulatonMap.keySet());
		Collections.sort(pList);

		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(POPULATION_OUTPUT_FILE_NAME), "utf-8"));

			while(!pList.isEmpty()) {
				Integer key = pList.removeLast();
				citySortedList = new LinkedList<City>(citiesByPopulatonMap.get(key));
				Collections.sort(citySortedList, cityComparator);
				writer.write(key.toString() + "\n\n");
				for(City city: citySortedList) {
					writer.write(city.getName() + ", " + city.getState() + "\n");
					StringBuilder sb = new StringBuilder("Interstates: ");
					LinkedList<Integer> cityHighways = new LinkedList<Integer>(city.getHighwayIDs());
					Collections.sort(cityHighways);
					for(Integer hID: cityHighways) {
						sb.append(CityFeedParser.INTERSTATE_PREFIX + hID + ", ");
					}
					sb.setLength(sb.length() - 2);
					writer.write(sb + "\n\n");
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

} 

