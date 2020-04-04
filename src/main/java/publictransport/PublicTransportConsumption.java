package publictransport;

public class PublicTransportConsumption {

	/********** DATA **********/

	private static final double PT_SD_BUS_DIESEL = 0.0044; 		// Public Transport Short-Distance Bus Diesel (kg/km per seat)
	private static final double PT_SD_TRAIN_ELECTRICITY = 23.0;	// Public Transport Short-Distance Train Electricity (Wh/km per seat)

	private static final double PT_LD_ICE_ELECTRICITY = 32.0; 	// Public Transport Long-Distance ICE Electricity (Wh/km per seat)
	private static final double PT_LD_IC_ELECTRICITY = 22.0;	// Public Transport Long-Distance IC/EC Electricity (Wh/km per seat)

	/********** METHODS **********/

	public static double publicTransportConsumption(String distanceType, String vehicleClass, String engine) {
		if (distanceType.equals("sd")) {
			if (vehicleClass.equals("bus")) return PT_SD_BUS_DIESEL;
			if (vehicleClass.equals("train")) return PT_SD_TRAIN_ELECTRICITY;
		}

		if (distanceType.equals("ld")) {
			if (vehicleClass.equals("ice")) return PT_LD_ICE_ELECTRICITY;
			if (vehicleClass.equals("ic")) {
				if (engine.equals("electricity")) return PT_LD_IC_ELECTRICITY;
			}
		}

		throw new IllegalArgumentException("illegal argument(s)");
	}

}
