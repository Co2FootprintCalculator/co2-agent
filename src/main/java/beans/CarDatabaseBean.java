package beans;

import car.Car;
import car.database.Driver;
import car.database.RestConsumer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import util.CO2FootprintProperties;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;

public class CarDatabaseBean extends AbstractMethodExposingBean {

	private CO2FootprintProperties properties;

	@Override
	public void doStart() throws Exception {
		super.doStart();
		log.info("CarDatabaseBean - starting");
		try {
			this.properties = new CO2FootprintProperties();
		} catch (InvalidPropertiesFormatException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();
		log.info("CarDatabaseBean - stopping");
	}

	/**
	 * Periodically executed method to download the car database .csv via the API and feed the data into this
	 * applications own database.
	 * <p>
	 * The interval is set in resources/Agent.xml
	 */
	public void execute() {
		log.info("CarDatabaseBean - starting database update");
		RestConsumer restConsumer = new RestConsumer(properties);
		InputStream databaseInputStream;
		try {
			databaseInputStream = restConsumer.downloadDatabase();

			restConsumer.close();

			Driver driver = new Driver(properties);
			driver.deleteAllRows();
			driver.uploadDataToDatabase(databaseInputStream);
			driver.close();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		log.info("CarDatabaseBean - finished database update");
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getBrands", scope = ActionScope.AGENT)
	public ObjectNode getBrands() {
		Driver driver = new Driver(properties);
		ObjectNode brandsJson = null;
		try {
			brandsJson = driver.getBrandsAsJson();
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return brandsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getModels", scope = ActionScope.AGENT)
	public ObjectNode getModels(String brand) {
		Driver driver = new Driver(properties);
		ObjectNode modelsJson = null;
		try {
			modelsJson = driver.getModelsAsJson(brand);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return modelsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getModelsByFuel", scope = ActionScope.AGENT)
	public ObjectNode getModelsByFuel(String brand, String fuel) {
		Driver driver = new Driver(properties);
		ObjectNode modelsJson = null;
		try {
			modelsJson = driver.getModelsByFuelAsJson(brand, fuel);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return modelsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getFuel", scope = ActionScope.AGENT)
	public ObjectNode getFuel(String brand, String model) {
		Driver driver = new Driver(properties);
		ObjectNode fuelJson = null;
		try {
			fuelJson = driver.getFuelAsJson(brand, model);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return fuelJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getFuelByBrand", scope = ActionScope.AGENT)
	public ObjectNode getFuelByBrand(String brand) {
		Driver driver = new Driver(properties);
		ObjectNode fuelJson = null;
		try {
			fuelJson = driver.getFuelByBrandAsJson(brand);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return fuelJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getCarID", scope = ActionScope.AGENT)
	public ObjectNode getCarID(String brand, String model, String fuel) {
		Driver driver = new Driver(properties);
		ObjectNode idJson = null;
		try {
			idJson = driver.getCarIdAsJson(brand, model, fuel);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return idJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getCar", scope = ActionScope.AGENT)
	public Car getCar(String carID) {
		Driver driver = new Driver(properties);
		Car car = null;
		try {
			car = driver.getCar(carID);
			driver.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return car;
	}

}
