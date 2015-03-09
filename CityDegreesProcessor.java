import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CityDegreesProcessor --- program determines degree of separation from a given city
 * @author    Maksim Rudenko
 */
public class CityDegreesProcessor {
	public static final String OUTPUT_FILE_NAME = "Degrees_From_Chicago.txt";
	public static final String DEFAULT_CITY_NAME = "Chicago";

	public static void main(String args[]) {
		if (args.length == 0) {
			Logger.getLogger(CityDegreesProcessor.class.getName()).log(Level.SEVERE, "Usage: " + CityDegreesProcessor.class.getName() + " <feed filename> [<root city name>|Chicago]");				
		}

		else {
			// Parsing the input
			String rootCityName = DEFAULT_CITY_NAME;
			City rootCity = null;
			HashSet<City> cityNodes = CityFeedParser.parseCsvFile(args[0]);				
			HashMap<Integer, HashSet<City>> citiesByHighwayMap = new HashMap<Integer, HashSet<City>>();
			HashMap<City, HashSet<City>> connectedCitiesMap = new HashMap<City, HashSet<City>>();

			if (args.length > 1) {
				rootCityName = args[1];
			}

			// Processing data (separated from parsing so it can be reused for Option 2)
			for (City city1: cityNodes){
				if(rootCityName.equals(city1.getName())) {
					rootCity = city1;
				}

				for(Integer hID: city1.getHighwayIDs()) {

					// pull the set of cites connected through this highway
					HashSet<City>  connectedCitiesFound = citiesByHighwayMap.get(hID);
					if (connectedCitiesFound == null) {
						connectedCitiesFound = new HashSet<City>();
						citiesByHighwayMap.put(hID, connectedCitiesFound);
					}

					// mark every newly-found city as interconnected with all cities on this highway
					if(!connectedCitiesFound.contains(city1)) {
						HashSet<City> cList1 = connectedCitiesMap.get(city1);
						if(cList1 == null) {
							cList1 = new HashSet<City>();
							connectedCitiesMap.put(city1, cList1);
						}
						for(City city2: connectedCitiesFound) {
							HashSet<City> cList2 = connectedCitiesMap.get(city2);
							if(cList2 == null) {
								cList2 = new HashSet<City>();
								connectedCitiesMap.put(city1, cList2);
							}

							// existing values won't be duplicated in a set
							cList1.add(city2);
							cList2.add(city1);
						}

						// finally, add the new city the list
						connectedCitiesFound.add(city1);
					}
				}
			}

			// Fail if the root node is not found
			if(rootCity == null) {
				Logger.getLogger(CityDegreesProcessor.class.getName()).log(Level.SEVERE, rootCityName + " has not been found in the input file.");				
			}
			else {
				// Sorting by degrees of separation from rootCity
				// Traverse all graph nodes breadth-first
				// pick up only new nodes on each lower level
				// finish when there are no new nodes on lower level
				ArrayList<HashSet<City>> citiesByLevelMap = new ArrayList<HashSet<City>>();
				HashSet<City> foundConnectedNodes = connectedCitiesMap.get(rootCity);
				HashSet<City> processedNodes = new HashSet<City>();
				HashSet<City> rootNode = new HashSet<City>();
				rootNode.add(rootCity);
				processedNodes.add(rootCity);
				citiesByLevelMap.add(rootNode);				
				if(foundConnectedNodes != null) {
					processedNodes.addAll(foundConnectedNodes);
					citiesByLevelMap.add(foundConnectedNodes);
					foundConnectedNodes = getNewChildNodes(foundConnectedNodes, processedNodes, connectedCitiesMap);
					while (foundConnectedNodes.size() > 0) {
						processedNodes.addAll(foundConnectedNodes);
						citiesByLevelMap.add(foundConnectedNodes);

						foundConnectedNodes = getNewChildNodes(foundConnectedNodes, processedNodes, connectedCitiesMap);
					}
				}

				// Formating the output
				cityNodes.removeAll(processedNodes); // get the disconnected cities
				produceOutputFile(citiesByLevelMap, cityNodes);
			}
		}
	}


	/**
	 * Utility function used to generate list of all newly-found nodes at the given level
	 * 
	 * @param parentNodes set of nodes to be traversed further
	 * @param nodesAlreadyFound set of nodes that have been traversed already
	 * @param connectedNodes node connectivity graph representation
	 */
	private static HashSet<City> getNewChildNodes(HashSet<City> parentNodes, HashSet<City> nodesAlreadyFound, HashMap<City, HashSet<City>> connectedNodes) {
		HashSet<City> foundChildNodes = new HashSet<City>();
		for(City city: parentNodes) {
			foundChildNodes.addAll(connectedNodes.get(city));
		}
		foundChildNodes.removeAll(nodesAlreadyFound);
		return foundChildNodes;
	}


	/**
	 * Creates output file of the following format:
	 *     [Degrees removed from root] [City], [State]
	 * The output is sorted by degree descending and then by city and state ascending.
	 * 
	 * @param citiesByLevelMap map of connected cities sorted by degree of separation from the root;
	 * @param disconnectedCities set of disconnected cities
	 */
	private static void produceOutputFile(ArrayList<HashSet<City>> citiesByLevelMap, HashSet<City> disconnectedCities) {

		// output-specific sorting mechanism
		class CityComparator implements Comparator<City> {
			public int compare(City c1, City c2) {
				int rtn = c1.getName().compareTo(c2.getName());
				if(rtn == 0) {
					rtn = c1.getState().compareTo(c2.getState());
				}
				return rtn;
			}
		}
		LinkedList<City> citySortedList;
		CityComparator cityComparator = new CityComparator ();

		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(OUTPUT_FILE_NAME), "utf-8"));

			// list all connected cities starting with the farthest
			for(int i = citiesByLevelMap.size() - 1; i >= 0; i--) {
				citySortedList = new LinkedList<City>(citiesByLevelMap.get(i));
				Collections.sort(citySortedList, cityComparator);
				for(City city:  citySortedList) {
					writer.write("" + i + " " + city.getName() + ", " + city.getState() + "\n");										
				}
			}

			// list all unreachable cities
			citySortedList = new LinkedList<City>(disconnectedCities);
			Collections.sort(citySortedList, cityComparator);
			for(City city: disconnectedCities) {
				writer.write("-1 " + city.getName() + ", " + city.getState() + "\n");										
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

