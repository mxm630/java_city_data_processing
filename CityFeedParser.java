import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CityFeedParser --- parses CSV data into City models
 * @author    Maksim Rudenko
 */
public class CityFeedParser {

	public static final String INTERSTATE_PREFIX = "I-";

	public static HashSet<City> parseCsvFile(String path) {

		//reading file line by line in Java using BufferedReader 
		HashSet<City> cities = new HashSet<City>();
		FileInputStream fis = null;
		BufferedReader reader = null;

		try {
			fis = new FileInputStream(path);
			reader = new BufferedReader(new InputStreamReader(fis));

			String line = reader.readLine();
			int lineNum = 1;
			while(line != null){

				String[] tokens = line.split("[|]");
				if(tokens.length != 4) {
					Logger.getLogger(CityFeedParser.class.getName()).log(Level.WARNING, "Line " + lineNum + " of " + path + " has unexpected number of fields");					
				}
				else {
					Integer population = Integer.valueOf(tokens[0]);
					String name = tokens[1];
					String state = tokens[2];
					HashSet<Integer> highways = new HashSet<Integer>();
					for (String str: tokens[3].split(";")){
						Integer highwayID = Integer.valueOf(str.substring(INTERSTATE_PREFIX.length()));
						// no need to check for duplicate numbers since we're using a set
						highways.add(highwayID);
					}
					cities.add(new City(name, state, population, highways));

				}

				line = reader.readLine();
				lineNum++;
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				reader.close();
				fis.close();
			} catch (IOException ex) {
				Logger.getLogger(CityFeedParser.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return cities;
	}
}