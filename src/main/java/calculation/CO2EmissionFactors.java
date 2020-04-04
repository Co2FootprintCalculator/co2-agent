package calculation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CO2EmissionFactors {

	/********** EMISSION FACTORS **********/

	/* Well-to-Tank (WtT) */

	// in kg CO2/kg fuel:
	private static final double WTT_PETROL 	= 0.46;
	private static final double WTT_DIESEL	= 0.48;
	private static final double WTT_LPG 	= 0.62;
	private static final double WTT_CNG 	= 0.69;

	// in kg CO2/kWh:
	private static final double WTT_ELECTRICITY_DE 		= 0.548;
	private static final double WTT_ELECTRICITY_DE_ECO 	= 0.019;
	private static final double WTT_ELECTRICITY_DB_FERN = 0.003;
	private static final double WTT_ELECTRICITY_DB_NAH 	= 0.556;

	/* Tank-to-Wheel (TtW) */

	// in kg CO2/kg fuel:
	private static final double TTW_PETROL	= 3.183;
	private static final double TTW_DIESEL	= 3.167;
	private static final double TTW_LPG		= 3.024;
	private static final double TTW_CNG 	= 2.786;


	/********** METHODS **********/

	public static ObjectNode getMixesAsJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode objectNode = objectMapper.createObjectNode();
		objectNode.putArray("mixes")
				.add("de")
				.add("de_eco");

		return objectNode;
	}

	public static double combinedEmissionFactor(String engine) {
		return combinedEmissionFactor(engine, null);
	}

	public static double combinedEmissionFactor(String engine, String mix) {
		return emissionFactor("wtt", engine, mix) + emissionFactor("ttw", engine, mix);
	}

	public static double emissionFactor(String type, String engine) throws IllegalArgumentException {
		return emissionFactor(type, engine, null);
	}

	/**
	 * @param type 'wtt' (Well-to-Tank) or 'ttw' (Tank-to-Wheel)
	 * @param engine What powers the engine? 'petrol', 'diesel', 'lpg', 'cng' or 'electricity'
	 * @param mix Which electricity mix is used? 'de' (Germany), 'de_eco' (green electricity Germany), 'db_fern' (Deutsche Bahn long-distance) or 'db_nah' (Deutsche Bahn short-distance)
	 * @return CO2 Emission Factor depending on type, engine and mix
	 * @throws IllegalArgumentException If at least one argument doesn't match the parameters specification
	 */
	public static double emissionFactor(String type, String engine, String mix) throws IllegalArgumentException {
		checkArguments(type, engine, mix);

		if (type.equals("wtt")) return getWttEmissionFactor(engine, mix);
		return getTtwEmissionFactor(engine);
	}

	private static void checkArguments(String type, String engine, String mix) {
		// check for wrong arguments
		if (!type.equals("wtt") && !type.equals("ttw")) {
			throw new IllegalArgumentException("Parameter 'type' must be 'wtt' or 'ttw'");
		}

		if (!engine.equals("petrol") && !engine.equals("diesel") && !engine.equals("lpg") && !engine.equals("cng") && !engine.equals("electricity")) {
			throw new IllegalArgumentException("Parameter 'engine' must be 'petrol', 'diesel', 'lpg', 'cng' or 'electricity'");
		}

		if (mix != null && !mix.equals("de") && !mix.equals("de_eco") && !mix.equals("db_fern") && !mix.equals("db_nah")) {
			throw new IllegalArgumentException("Parameter 'mix' must be 'de', 'de_eco', 'db_fern' or 'db_nah'");
		}

		if (engine.equals("electricity") && mix == null) {
			throw new IllegalArgumentException("No argument 'mix' for electric engine");
		}

	}

	private static double getWttEmissionFactor(String engine, String mix) {
		switch (engine) {
			case "petrol":
				return WTT_PETROL;
			case "diesel":
				return WTT_DIESEL;
			case "lpg":
				return WTT_LPG;
			case "cng":
				return WTT_CNG;
			case "electricity":
				switch (mix) {
					case "de":
						return WTT_ELECTRICITY_DE;
					case "de_eco":
						return WTT_ELECTRICITY_DE_ECO;
					case "db_fern":
						return WTT_ELECTRICITY_DB_FERN;
					case "db_nah":
						return WTT_ELECTRICITY_DB_NAH;
				}
		}

		return 0;
	}

	private static double getTtwEmissionFactor(String engine) {
		switch (engine) {
			case "petrol":
				return TTW_PETROL;
			case "diesel":
				return TTW_DIESEL;
			case "lpg":
				return TTW_LPG;
			case "cng":
				return TTW_CNG;
			case "electricity":
				return 0.0;
		}

		return 0;
	}

}
