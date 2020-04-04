package util;

public class UnitConversion {

	// Density in kg/l
	private static final double DENSITY_PETROL	= 0.742;
	private static final double DENSITY_DIESEL 	= 0.832;
	private static final double DENSITY_LPG 	= 0.600;

	/**
	 * @param fuel 'petrol', 'diesel' or 'lpg'
	 * @param liter amount of fuel in liters
	 * @return amount of fuel in kilograms
	 */
	public static double literToKilogram(String fuel, double liter) {
		checkArguments(fuel, liter);

		double result = -1;

		switch (fuel) {
			case "petrol":
				result = liter * DENSITY_PETROL;
				break;
			case "diesel":
				result = liter * DENSITY_DIESEL;
				break;
			case "lpg":
				result = liter * DENSITY_LPG;
				break;
		}

		return result;
	}

	/**
	 * @param fuel 'petrol', 'diesel' or 'lpg'
	 * @param kilogram amount of fuel in kilograms
	 * @return amount of fuel in liters
	 */
	public static double kilogramToLiter(String fuel, double kilogram) {
		checkArguments(fuel, kilogram);

		double result = -1;

		switch (fuel) {
			case "petrol":
				result = kilogram / DENSITY_PETROL;
				break;
			case "diesel":
				result = kilogram / DENSITY_DIESEL;
				break;
			case "lpg":
				result = kilogram / DENSITY_LPG;
				break;
		}

		return result;
	}

	private static void checkArguments(String fuel, double amount) {
		if (!fuel.equals("petrol") && !fuel.equals("diesel") && !fuel.equals("lpg")) {
			throw new IllegalArgumentException("Parameter 'fuel' must be 'petrol', 'diesel' or 'lpg'");
		}

		if (amount < 0) {
			throw new IllegalArgumentException("number must be >= 0");
		}
	}

}
