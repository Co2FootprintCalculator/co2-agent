package routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.CO2FootprintProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

class OpenRouteServiceAccessor {

	private final String API_KEY;

	public OpenRouteServiceAccessor(CO2FootprintProperties properties) {
		this.API_KEY = properties.getOpenRouteServiceAPIKey();
	}

	public Response calculateRoute(Place start, Place destination) {
		String url = "https://api.openrouteservice.org/v2/directions/driving-car/json";
		String body = "{\"coordinates\":[["
				+ start.getLongitude() + "," + start.getLatitude() + "],["
				+ destination.getLongitude() + "," + destination.getLatitude()
				+ "]],\"elevation\":\"false\",\"instructions\":\"true\",\"preference\":\"fastest\",\"units\":\"m\"}";

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(url);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder.header(HttpHeaders.AUTHORIZATION, API_KEY);

		return invocationBuilder.post(Entity.json(body));
	}

	public ArrayList<Place> searchPlace(String query) throws JsonProcessingException {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		String url = "https://api.openrouteservice.org/geocode/search?api_key="
				+ API_KEY
				+ "&text="
				+ query
				+ "&boundary.country=DE&layers=venue,address&size=20";
		url = url.replaceAll(" ", "%20");

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(url);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode responseNode = objectMapper.readTree(response.readEntity(String.class));
		JsonNode featuresNode = responseNode.path("features");

		ArrayList<Place> places = new ArrayList<>();

		for (JsonNode feature : featuresNode) {
			Label label = new Label(feature.path("properties"));
			Double latitude = feature.path("geometry").path("coordinates").get(1).asDouble();
			Double longitude = feature.path("geometry").path("coordinates").get(0).asDouble();
			Place place = new Place(label, latitude, longitude);
			places.add(place);
		}

		return places;
	}

}
