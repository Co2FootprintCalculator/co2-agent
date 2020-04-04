package routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import util.CO2FootprintProperties;

import java.util.ArrayList;

public class Place {

	private final Label label;

	private final Double latitude;
	private final Double longitude;

	public Place(Double latitude, Double longitude) {
		label = null;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Place(Label label, Double latitude, Double longitude) {
		this.label = label;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Label getLabel() {
		return label;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public static JsonNode searchPlace(String query, CO2FootprintProperties properties) throws JsonProcessingException {
		OpenRouteServiceAccessor ORSAccessor = new OpenRouteServiceAccessor(properties);
		ArrayList<Place> placesList = ORSAccessor.searchPlace(query);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode placesNode = resultNode.putArray("places");

		for (Place p : placesList) {
			ObjectNode newNode = placesNode.addObject();
			newNode.put("label", p.getLabel().toString());
			newNode.put("latitude", p.getLatitude());
			newNode.put("longitude", p.getLongitude());
		}

		return resultNode;
	}
}
