package routing;

import com.fasterxml.jackson.databind.JsonNode;

class Label {

	private String label;
	private JsonNode properties;

	public Label(String label) {
		this.label = label;
	}

	public Label(JsonNode labelProperties) {
		this.properties = labelProperties;
		label = generateLabel();
	}

	public String toString() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	private String generateLabel() {
		String label = null;
		String layer = properties.get("layer").asText();

		if (layer.equals("address")) label = generateAddressLabel();
		else if (layer.equals("venue")) label = generateVenueLabel();
		else if (properties.has("label")) label = properties.get("label").asText();

		return label;
	}

	private String generateAddressLabel() {
		String street = "";
		String houseNumber = "";
		String postalCode = "";
		String locality = "";

		if (properties.has("street")) street = properties.get("street").asText();

		if (properties.has("housenumber")) houseNumber = properties.get("housenumber").asText();

		if (properties.has("postalcode")) postalCode = properties.get("postalcode").asText();

		if (properties.has("locality")) locality = properties.get("locality").asText();
		else if (properties.has("county")) locality = properties.get("county").asText();
		else if (properties.has("macrocounty")) locality = properties.get("macrocounty").asText();
		else if (properties.has("region")) locality = properties.get("region").asText();

		String addressLabel = String.format("%s %s, %s %s", street, houseNumber, postalCode, locality);
		addressLabel = addressLabel.trim().replaceAll(" +", " ");
		addressLabel = addressLabel.trim().replaceAll(" ,", " ");

		return addressLabel;
	}

	private String generateVenueLabel() {
		String name = "";
		String address = generateAddressLabel();

		if (properties.has("name")) name = properties.get("name").asText();

		String venueLabel = String.format("%s, %s", name, address);
		venueLabel = venueLabel.trim().replaceAll(" +", " ");
		venueLabel = venueLabel.trim().replaceAll(" ,", "");

		return venueLabel;
	}
}
