package beans;

import calculation.CO2Calculator;
import calculation.CO2EmissionFactors;
import car.Car;
import car.database.Driver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.rsga.beans.AbstractRESTfulAgentBean;
import routing.CarRoute;
import routing.Place;
import routing.PublicTransportRoute;
import util.CO2FootprintProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("DanglingJavadoc")
public class VehicleCO2Calculator extends AbstractRESTfulAgentBean {

	private CO2FootprintProperties properties;

	public void doStart() throws Exception {
		super.doStart();
		log.info("VehicleCO2Calculator - starting");
		this.properties = new CO2FootprintProperties();
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();
		log.info("VehicleCO2Calculator - stopping");
	}


	/**********************************************************/
	/************************** UTIL **************************/
	/**********************************************************/

	/**
	 * Gets all the available electricity mixes.
	 *  <p>
	 *  Needed for calculating the CO2 emissions of cars powered by electricity, as different electricity mixes provide for
	 *  different levels of emissions.
	 * @return All implemented electricity mixes as a JSON formatted list. Currently {@code de} for the general electricity
	 *  mix of germany and {@code de_eco} for the german electricity mix with a higher share of renewable energy.
	 */
	@GET
	@Path("/util/electricity/mixes")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public ObjectNode getElectricityMixes() {
		log.info("New method invocation - getElectricityMixes() called");
		return CO2EmissionFactors.getMixesAsJson();
	}


	/***************************************************/
	/******************* CAR-RELATED *******************/
	/***************************************************/

	/**
	 * Retrieves all available brands from the underlying database.
	 * @return JSON formatted list of brands
	 */
	@GET
	@Path("/v2/cars/brands")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getBrands() {
		log.info("New method invocation - getBrands() called");

		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getBrands");
		IActionDescription act = memory.read(template);

		Serializable[] brands = invokeAndWaitForResult(act, new Serializable[]{}).getResults();

		return brands[0];
	}

	/**
	 * Retrieves all available models for the specified brand from the underlying database.
	 * @param brand One of the brands returned by the {@code getBrands} method
	 * @return JSON formatted list of available models for {@code brand} parameter
	 */
	@GET
	@Path("/v2/cars/brand/models")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getModelsByBrand(@QueryParam("brand") String brand) {
		log.info("New method invocation - getModelsByBrand(...) called");
		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getModels");
		IActionDescription act = memory.read(template);

		Serializable[] models = invokeAndWaitForResult(act, new Serializable[]{brand}).getResults();

		return models[0];
	}

	/**
	 * Retrieves all available models for the specified brand and fuel type from the underlying database.
	 * @param brand One of the brands returned by the {@code brands} method
	 * @param fuel One of the fuel types returned by the {@code mixes} method
	 * @return JSON formatted list of available models for {@code brand} and {@code fuel} parameter
	 */
	@GET
	@Path("/v2/cars/brand/fuel/models")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getModelsByBrandAndFuel(@QueryParam("brand") String brand,
										   @QueryParam("fuel") String fuel) {
		log.info("New method invocation - getModelsByBrandAndFuel(...) called");
		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getModelsByFuel");
		IActionDescription act = memory.read(template);

		Serializable[] models = invokeAndWaitForResult(act, new Serializable[]{brand, fuel}).getResults();

		return models[0];
	}

	/**
	 * Gets all available drive configurations (fuel to power the car) for the specified brand and model.
	 * @param brand One of the brands returned by the {@code brands} method
	 * @param model One of the models returned by the {@code models} method
	 * @return JSON formatted list of available drive configurations for the {@code brand} and {@code model} parameters.
	 *  Returns a subset or all of the following: {@code {petrol, diesel, cng, electricity}}
	 */
	@GET
	@Path("/v2/cars/brand/model/fuel")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getFuelByBrandAndModel(@QueryParam("brand") String brand,
								   @QueryParam("model") String model) {
		log.info("New method invocation - getFuelByBrandAndModel(...) called");
		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getFuel");
		IActionDescription act = memory.read(template);

		Serializable[] fuel = invokeAndWaitForResult(act, new Serializable[]{brand, model}).getResults();

		return fuel[0];
	}

	/**
	 * Gets all available drive configurations (fuel to power the car) for the specified brand.
	 * @param brand One of the brands returned by the {@code brands} method
	 * @return JSON formatted list of available drive configurations for the {@code brand} parameter.
	 *  Returns a subset or all of the following: {@code {petrol, diesel, cng, electricity}}
	 */
	@GET
	@Path("/v2/cars/brand/fuel")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getFuelByBrand(@QueryParam("brand") String brand) {
		log.info("New method invocation - getFuelByBrand(...) called");
		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getFuelByBrand");
		IActionDescription act = memory.read(template);

		Serializable[] fuel = invokeAndWaitForResult(act, new Serializable[]{brand}).getResults();

		return fuel[0];
	}

	/**
	 * Get the unique ID of the car specified by brand, model and fuel.
	 * @param brand One of the brands returned by the {@code brands} method
	 * @param model One of the models returned by the {@code models} method
	 * @param fuel One of the drive configurations returned by the {@code fuel} method
	 * @return JSON formatted id field
	 */
	@GET
	@Path("/v2/cars/brand/model/fuel/id")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public Serializable getCarId(@QueryParam("brand") String brand,
								 @QueryParam("model") String model,
								 @QueryParam("fuel") String fuel) {

		log.info("New method invocation - getCarId(...) called");

		IActionDescription template = new Action("ACTION#beans.CarDatabaseBean.getCarID");
		IActionDescription act = memory.read(template);

		Serializable[] carId = invokeAndWaitForResult(act, new Serializable[]{brand, model, fuel}).getResults();

		return carId[0];
	}


	/*************************************************************/
	/************************ CALCULATION ************************/
	/*************************************************************/

	/**
	 * Calculate the CO2 emissions of a car specified by {@code carID} on a route that is given by the {@code urbanKM},
	 * {@code nonUrbanKM} and {@code autobahnKM} parameters.
	 *  If the car is powered by electricity, the electricity mix ({@code mix}) is needed. Otherwise this parameter is
	 *  simply ignored and can be {@code null}.
	 * @param carID ID as returned by the {@code getCar} method
	 * @param mix Used electricity mix if the car is powered by electricity. Otherwise {@code null}.
	 * @param urbanKM Travel distance in kilometers within urban areas. Note that this includes all parts of the route
	 *                where the maximum speed is lower than 50 km/h.
	 * @param nonUrbanKM Travel distance in kilometers within non-urban areas. Note that this includes all parts of the
	 *                   route where the maximum speed is between 50 and 100 km/h.
	 * @param autobahnKM Travel distance in kilometers on highways. Note that this includes all parts of the route where
	 *                   the maximum speed is above 100 km/h.
	 * @return JSON formatted field containing the estimated CO2 emissions for the given car and the given start/end point.
	 */
	@GET
	@Path("/v2/calculation/length/emissions/car")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public ObjectNode calculateCarEmissionsByRouteLength(@QueryParam("carID") String carID,
														@QueryParam("mix") String mix,
														@QueryParam("urbanKM") double urbanKM,
														@QueryParam("nonUrbanKM") double nonUrbanKM,
														@QueryParam("autobahnKM") double autobahnKM)
	{
		log.info("New method invocation - calculateCarEmissionsByRouteLength(...) called");

		Driver driver = new Driver(properties);

		Car car = null;

		ArrayList<Car> genericCars = Car.getGenericCars();
		for (Car genericCar : genericCars) {
			if (genericCar.getId().equals(carID)) {
				car = genericCar;
				break;
			}
		}

		try {
			if (car == null) car = driver.getCar(carID);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		CarRoute carRoute = new CarRoute(urbanKM, nonUrbanKM, autobahnKM);
		Double emissions = CO2Calculator.calculateCarEmissions(Objects.requireNonNull(car), carRoute, mix);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode result = objectMapper.createObjectNode();
		result.put("carEmissions", emissions);

		return result;
	}

	/**
	 * Calculate the CO2 emissions of a car specified by {@code carID} on a route that is identified by the latitude and
	 *  longitude of its starting point ({@code startLatitude}, {@code startLongitude}) and its destination
	 *  ({@code destinationLatitude}, {@code destinationLongitude}).
	 *  If the car is powered by electricity, the electricity mix ({@code mix}) is needed. Otherwise this parameter is
	 *  simply ignored and can be {@code null}.
	 * @implSpec This method uses information about the shortest route that is found by the Open Route Service API between
	 *  the start and the destination.
	 * @param carID ID as returned by the {@code getCar} method
	 * @param mix Used electricity mix if the car is powered by electricity. Otherwise {@code null}.
	 * @param startLatitude Latitude of the routes starting point
	 * @param startLongitude Longitude of the routes starting point
	 * @param destinationLatitude Latitude of the routes destination
	 * @param destinationLongitude Longitude of the routes destination
	 * @return JSON formatted field containing the estimated CO2 emissions for the given car and the given start/end point
	 *  and a very rough estimate of the corresponding emissions using public transport
	 */
	@GET
	@Path("/v2/calculation/locations/emissions/car")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public ObjectNode calculateCarEmissionsByCoordinates(@QueryParam("carID") String carID,
											@QueryParam("mix") String mix,
											@QueryParam("startLatitude") double startLatitude,
											@QueryParam("startLongitude") double startLongitude,
											@QueryParam("destinationLatitude") double destinationLatitude,
											@QueryParam("destinationLongitude") double destinationLongitude)
	{
		log.info("New method invocation - calculateCarEmissionsByCoordinates(...) called");

		Driver driver = new Driver(properties);

		Car car = null;

		ArrayList<Car> genericCars = Car.getGenericCars();
		for (Car genericCar : genericCars) {
			if (genericCar.getId().equals(carID)) {
				car = genericCar;
				break;
			}
		}

		try {
			if (car == null) car = driver.getCar(carID);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Place start = new Place(startLatitude, startLongitude);
		Place destination = new Place(destinationLatitude, destinationLongitude);
		CarRoute carRoute = new CarRoute(start, destination, properties);

		Double emissions = CO2Calculator.calculateCarEmissions(Objects.requireNonNull(car), carRoute, mix);

		// calculate public transport emissions
		PublicTransportRoute route = new PublicTransportRoute(carRoute.getUrbanKM(), carRoute.getNonUrbanKM() + carRoute.getAutobahnKM());
		Double ptEmissions = CO2Calculator.calculatePublicTransportEmissions(route);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode result = objectMapper.createObjectNode();
		result.put("carEmissions", emissions);
		result.put("publicTransportEmissions", ptEmissions);

		return result;
	}

	/**
	 * Calculate the CO2 emissions for using public transport (excluding air traffic) on a route, that consists of
	 *  {@code shortDistanceKM} kilometers short distance transportation (local bus traffic, underground and [sub]urban railway)
	 *  and {@code longDistanceKM} kilometers long distance transportation (mainline rail services and regional trains).
	 *  Note that this method returns only a very rough estimate.
	 * @param shortDistanceKM Travel distance in kilometers using local bus traffic, underground and [sub]urban railway
	 * @param longDistanceKM Travel distance in kilometers using regional trains and mainline rail services.
	 * @return Estimated CO2 emissions for the given route information
	 */
	@GET
	@Path("/calculation/emissions/publictransport")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public ObjectNode calculatePublicTransportEmissions(@QueryParam("shortDistanceKM") double shortDistanceKM,
										   @QueryParam("longDistanceKM") double longDistanceKM)
	{
		log.info("New method invocation - calculatePublicTransportEmissions(...) called");

		PublicTransportRoute route = new PublicTransportRoute(shortDistanceKM, longDistanceKM);
		Double emissions = CO2Calculator.calculatePublicTransportEmissions(route);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode result = objectMapper.createObjectNode();
		result.put("publicTransportEmissions", emissions);

		return result;
	}

	
	/************************************************************/
	/************************* LOCATION *************************/
	/************************************************************/

	/**
	 * Search for places using an address or the name of a venue and get the latitude and longitude of this place.
	 * @param query Address or name of a venue.
	 * @return JSON formatted list of search results, each with its corresponding latitude and longitude.
	 */
	@GET
	@Path("/locations/search")
	@Produces("application/json")
	@Expose(scope = ActionScope.WEBSERVICE)
	public JsonNode getLocations(@QueryParam("query") String query) {
		log.info("New method invocation - getLocations(...) called");

		JsonNode places = null;
		try {
			places = Place.searchPlace(query, properties);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return places;
	}

}
