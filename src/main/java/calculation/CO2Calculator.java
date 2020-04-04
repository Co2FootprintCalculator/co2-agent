package calculation;

import car.Car;
import routing.CarRoute;
import routing.PublicTransportRoute;

import static publictransport.PublicTransportConsumption.*;

public class CO2Calculator {

	private static final double IC_SHARE = 0.273; // market share of IC/EC vehicle class in total long-distance rail transport
	private static final double LOCAL_BUS_SHARE = 0.5; // market share of bus service in total short-distance public transport (random number, not based on anything)

	/**
	 * @param car Car object that includes consumption data
	 * @param route CarRoute object that includes route section lengths
	 * @param mix Electricity mix. If the car is not electric: mix = null
	 * @return Combined CO2 emissions for this car and this route
	 */
	public static double calculateCarEmissions(Car car, CarRoute route, String mix) {
		// calculate consumption for each part of the route
		Double urbanConsumption = (car.getUrbanConsumption() / 100) * route.getUrbanKM();
		Double nonUrbanConsumption = (car.getNonUrbanConsumption() / 100) * route.getNonUrbanKM();
		Double autobahnConsumption = (car.getAutobahnConsumption() / 100) * route.getAutobahnKM();

		// calculate total consumption
		double totalConsumption = urbanConsumption + nonUrbanConsumption + autobahnConsumption;

		if (car.isElectric()) return totalConsumption * CO2EmissionFactors.combinedEmissionFactor(car.getFuel(), mix);
		else return totalConsumption * CO2EmissionFactors.combinedEmissionFactor(car.getFuel());
	}

	/**
	 * @param route PublicTransportRoute object that includes route section lengths
	 * @return Combined CO2 emissions for short- and long-travel public transport
	 */
	public static double calculatePublicTransportEmissions(PublicTransportRoute route) {
		// local public transport (short-distance)
		double busConsumption = (LOCAL_BUS_SHARE * route.getShortDistanceKM()) * publicTransportConsumption("sd", "bus", "diesel");
		double busEmissions = busConsumption * CO2EmissionFactors.combinedEmissionFactor("diesel");

		double trainConsumption = ((1 - LOCAL_BUS_SHARE) * route.getShortDistanceKM()) * publicTransportConsumption("sd", "train", "electricity");
		double trainEmissions = (trainConsumption / 1000) * CO2EmissionFactors.combinedEmissionFactor("electricity", "db_nah");

		double sdEmissions = busEmissions + trainEmissions;

		// long-distance public transport
		double iceConsumption = (((1 - IC_SHARE) * route.getLongDistanceKM()) * publicTransportConsumption("ld", "ice", "electricity"));
		double icConsumption = ((IC_SHARE * route.getLongDistanceKM()) * publicTransportConsumption("ld", "ic", "electricity"));

		double ldEmissions = ((iceConsumption + icConsumption) / 1000) * CO2EmissionFactors.combinedEmissionFactor("electricity", "db_fern");

		// combine emissions
		return sdEmissions + ldEmissions;
	}

}
