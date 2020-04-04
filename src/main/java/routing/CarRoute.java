package routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.CO2FootprintProperties;

import javax.ws.rs.core.Response;

public class CarRoute {

	private Place start;
	private Place destination;

	private Double urbanKM;
	private Double nonUrbanKM;
	private Double autobahnKM;

	public CarRoute(Double urbanKM, Double nonUrbanKM, Double autobahnKM) {
		this.urbanKM = urbanKM;
		this.nonUrbanKM = nonUrbanKM;
		this.autobahnKM = autobahnKM;
	}

	public CarRoute(Place start, Place destination, CO2FootprintProperties properties) {
		this.start = start;
		this.destination = destination;
		try {
			calculateKilometers(properties);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void calculateKilometers(CO2FootprintProperties properties) throws JsonProcessingException {
		this.urbanKM = 0.0;
		this.nonUrbanKM = 0.0;
		this.autobahnKM = 0.0;

		ObjectMapper objectMapper = new ObjectMapper();

		OpenRouteServiceAccessor ORSAccessor = new OpenRouteServiceAccessor(properties);
		Response response = ORSAccessor.calculateRoute(start, destination);

		JsonNode responseNode = objectMapper.readTree(response.readEntity(String.class));
		JsonNode stepsNode = responseNode.path("routes").get(0).path("segments").get(0).path("steps");

		for (JsonNode step : stepsNode) {
			double distance = step.path("distance").asDouble() / 1000;
			double duration = step.path("duration").asDouble();

			if (distance > 0) {
				double v = distance / ((duration / 60) / 60);

				if (v < 50) urbanKM += distance;
				else if (v >= 50 && v < 100) nonUrbanKM += distance;
				else autobahnKM += distance;
			}
		}
	}

	public Double getUrbanKM() {
		return urbanKM;
	}

	public Double getNonUrbanKM() {
		return nonUrbanKM;
	}

	public Double getAutobahnKM() {
		return autobahnKM;
	}
}
