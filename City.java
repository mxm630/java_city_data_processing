import java.util.HashSet;

/**
 * City --- data model used for city/interstate data processing
 * @author    Maksim Rudenko
 */
public class City {
	private String name;
	private String state;
	private Integer population;
	private HashSet<Integer> highwayIDs;

	public City (String city, String state, Integer population, HashSet<Integer> highwayIDs) {
		this.setName(city);
		this.setState(state);
		this.setPopulation(population);
		this.setHighwayIDs(highwayIDs);
	}

	public String getName() {
		return name;
	}

	public void setName(String city) {
		this.name = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getPopulation() {
		return population;
	}

	public void setPopulation(Integer population) {
		this.population = population;
	}

	public HashSet<Integer> getHighwayIDs() {
		return highwayIDs;
	}

	public void setHighwayIDs(HashSet<Integer> highwayIDs) {
		this.highwayIDs = highwayIDs;
	}
}