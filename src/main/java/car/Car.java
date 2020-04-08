package car;

import car.database.RestConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import util.CO2FootprintProperties;

import java.util.ArrayList;
import java.util.HashMap;

import static util.UnitConversion.literToKilogram;

public class Car {

	private final String id;

	private final String brand;
	private final String model;
	private final String fuel;

	private final Double urbanConsumption;
	private final Double nonUrbanConsumption;
	private final Double autobahnConsumption;
	private final Double co2Emissions;

	public Car(String id, CO2FootprintProperties properties) {
		// get car data from data base by id
		RestConsumer restConsumer = new RestConsumer(properties);
		HashMap<String, String> data = new HashMap<>();
		try {
			data = restConsumer.getCarData(id);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		restConsumer.close();

		// set local variables
		this.id = id;
		this.brand = null;
		this.model = null;
		this.fuel = data.get("fuel");
		if (this.fuel.equals("electricity") || this.fuel.equals("cng")) {
			this.urbanConsumption = Double.parseDouble(data.get("urbanConsumption"));
			this.nonUrbanConsumption = Double.parseDouble(data.get("nonUrbanConsumption"));
			this.autobahnConsumption = Double.parseDouble(data.get("autobahnConsumption"));
		} else {
			this.urbanConsumption = literToKilogram(this.fuel, Double.parseDouble(data.get("urbanConsumption")));
			this.nonUrbanConsumption = literToKilogram(this.fuel, Double.parseDouble(data.get("nonUrbanConsumption")));
			this.autobahnConsumption = literToKilogram(this.fuel, Double.parseDouble(data.get("autobahnConsumption")));
		}
		this.co2Emissions = Double.parseDouble(data.get("officialCO2"));

	}

	public Car(String id, String brand, String model, String fuel, String urbanConsumption,
			   String nonUrbanConsumption, String autobahnConsumption, String co2) {
		this.id = id;
		this.brand = brand;
		this.model = model;
		this.fuel = fuel;
		if (this.fuel.equals("electricity") || this.fuel.equals("cng")) {
			this.urbanConsumption = Double.parseDouble(urbanConsumption);
			this.nonUrbanConsumption = Double.parseDouble(nonUrbanConsumption);
			this.autobahnConsumption = Double.parseDouble(autobahnConsumption);
		} else {
			this.urbanConsumption = literToKilogram(this.fuel, Double.parseDouble(urbanConsumption));
			this.nonUrbanConsumption = literToKilogram(this.fuel, Double.parseDouble(nonUrbanConsumption));
			this.autobahnConsumption = literToKilogram(this.fuel, Double.parseDouble(autobahnConsumption));

		}
		this.co2Emissions = Double.parseDouble(co2);
	}

	public String getId() {
		return id;
	}

	public String getBrand() {
		return brand;
	}

	public String getModel() {
		return model;
	}

	public String getFuel() {
		return fuel;
	}

	public Double getUrbanConsumption() {
		return urbanConsumption;
	}

	public Double getNonUrbanConsumption() {
		return nonUrbanConsumption;
	}

	public Double getAutobahnConsumption() {
		return autobahnConsumption;
	}

	public Double getCo2Emissions() {
		return co2Emissions;
	}

	public Boolean isElectric() {
		return this.fuel.equals("electricity");
	}

	public Boolean isCng() {
		return this.fuel.equals("cng");
	}

	@Override
	public String toString() {
		return "Car{" +
				"id='" + id + '\'' +
				", brand='" + brand + '\'' +
				", model='" + model + '\'' +
				", fuel='" + fuel + '\'' +
				", urbanConsumption=" + urbanConsumption +
				", nonUrbanConsumption=" + nonUrbanConsumption +
				", autobahnConsumption=" + autobahnConsumption +
				", co2Emissions=" + co2Emissions +
				'}';
	}

	public static ArrayList<Car> getGenericCars() {
		ArrayList<Car> genericCars = new ArrayList<>();

		genericCars.add(new Car("GenericPetrolSmall", "GENERIC", "small", "petrol", "8.8", "5.6", "5.6", "158.4"));
		genericCars.add(new Car("GenericPetrolMedium", "GENERIC", "medium", "petrol", "15.4", "8.6", "8.6", "260.1"));
		genericCars.add(new Car("GenericPetrolBig", "GENERIC", "big", "petrol", "24.3", "11.6", "11.6", "359.5"));

		genericCars.add(new Car("GenericDieselSmall", "GENERIC", "small", "diesel", "6.0", "4.3", "4.3", "132.7"));
		genericCars.add(new Car("GenericDieselMedium", "GENERIC", "medium", "diesel", "9.3", "6.6", "6.6", "206.5"));
		genericCars.add(new Car("GenericDieselBig", "GENERIC", "big", "diesel", "11.8", "7.9", "7.9", "253.1"));

		genericCars.add(new Car("GenericCngSmall", "GENERIC", "small", "cng", "9.0", "5.3", "5.3", "119.3"));
		genericCars.add(new Car("GenericCngMedium", "GENERIC", "medium", "cng", "12.6", "7.2", "7.2", "167.6"));
		genericCars.add(new Car("GenericCngBig", "GENERIC", "big", "cng", "17.5", "10.1", "10.1", "229.5"));

		genericCars.add(new Car("GenericElectricitySmall", "GENERIC", "small", "electricity", "15.7", "15.7", "15.7", "0.0"));
		genericCars.add(new Car("GenericElectricityMedium", "GENERIC", "medium", "electricity", "18.6", "18.6", "18.6", "0.0"));
		genericCars.add(new Car("GenericElectricityBig", "GENERIC", "big", "electricity", "24.6", "24.1", "24.1", "0.0"));

		return genericCars;
	}
}
