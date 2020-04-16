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

public class CarDatabaseBean extends AbstractMethodExposingBean {

	private CO2FootprintProperties properties;

	@Override
	public void doStart() {
		try {
			super.doStart();
			log.info("CarDatabaseBean - starting");
			this.properties = new CO2FootprintProperties();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	@Override
	public void doStop() {
		try {
			super.doStop();
			log.info("CarDatabaseBean - stopping");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
			Driver driver = new Driver(properties);
			driver.deleteAllRows();
			driver.uploadDataToDatabase(databaseInputStream);
			driver.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("CarDatabaseBean - finished database update");
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getBrands", scope = ActionScope.AGENT)
	public ObjectNode getBrands() {
		log.info("New method invocation - getBrands(...) called");
		ObjectNode brandsJson = null;
		try {
			Driver driver = new Driver(properties);
			brandsJson = driver.getBrandsAsJson();
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return brandsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getModels", scope = ActionScope.AGENT)
	public ObjectNode getModels(String brand) {
		log.info("New method invocation - getModels(...) called");
		ObjectNode modelsJson = null;
		try {
			Driver driver = new Driver(properties);
			modelsJson = driver.getModelsAsJson(brand);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return modelsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getModelsByFuel", scope = ActionScope.AGENT)
	public ObjectNode getModelsByFuel(String brand, String fuel) {
		log.info("New method invocation - getModelsByFuel(...) called");
		ObjectNode modelsJson = null;
		try {
			Driver driver = new Driver(properties);
			modelsJson = driver.getModelsByFuelAsJson(brand, fuel);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return modelsJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getFuel", scope = ActionScope.AGENT)
	public ObjectNode getFuel(String brand, String model) {
		log.info("New method invocation - getFuel(...) called");
		ObjectNode fuelJson = null;
		try {
			Driver driver = new Driver(properties);
			fuelJson = driver.getFuelAsJson(brand, model);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return fuelJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getFuelByBrand", scope = ActionScope.AGENT)
	public ObjectNode getFuelByBrand(String brand) {
		log.info("New method invocation - getFuelByBrand(...) called");
		ObjectNode fuelJson = null;
		try {
			Driver driver = new Driver(properties);
			fuelJson = driver.getFuelByBrandAsJson(brand);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return fuelJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getCarID", scope = ActionScope.AGENT)
	public ObjectNode getCarID(String brand, String model, String fuel) {
		log.info("New method invocation - getCarID(...) called");
		ObjectNode idJson = null;
		try {
			Driver driver = new Driver(properties);
			idJson = driver.getCarIdAsJson(brand, model, fuel);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return idJson;
	}

	@Expose(name = "ACTION#beans.CarDatabaseBean.getCar", scope = ActionScope.AGENT)
	public Car getCar(String carID) {
		log.info("New method invocation - getCar(...) called");
		Car car = null;
		try {
			Driver driver = new Driver(properties);
			car = driver.getCar(carID);
			driver.close();
		} catch (SQLException | ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		return car;
	}

}
