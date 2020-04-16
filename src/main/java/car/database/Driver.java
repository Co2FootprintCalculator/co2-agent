package car.database;

import car.Car;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import util.CO2FootprintProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static car.database.RestConsumer.translateFuelToAgentRepresentation;

public class Driver {

	private final Connection myConn;

	private final CO2FootprintProperties properties;

	public Driver(CO2FootprintProperties properties) throws SQLException, ClassNotFoundException {
		this.properties = properties;

		Class.forName("com.mysql.jdbc.Driver");
		myConn = DriverManager.getConnection("jdbc:mysql://" +
						properties.getCacheDatabaseHost() +
						":" +
						properties.getCacheDatabasePort() +
						"/" +
						properties.getCacheDatabaseName() +
						"?autoReconnect=true&useSSL=" +
						properties.getCacheDatabaseUseSSL() +
						"&rewriteBatchedStatements=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin",
				properties.getCacheDatabaseUser(),
				properties.getCacheDatabasePassword());
	}

	public void close() throws SQLException {
		myConn.close();
	}

	public void deleteAllRows() throws SQLException {
		Statement myStmt = myConn.createStatement();
		myStmt.executeUpdate("delete from " + properties.getCacheDatabaseTableName() + " where id > 9999");
	}

	public void uploadDataToDatabase(InputStream dataInputStream) throws IOException, CsvException {
		Reader reader = new InputStreamReader(dataInputStream);

		CSVParser csvParser = new CSVParserBuilder()
				.withSeparator(';')
				.build();

		CSVReader csvReader = new CSVReaderBuilder(reader)
				.withSkipLines(1)
				.withCSVParser(csvParser)
				.build();

		List<String[]> records = csvReader.readAll();

		String query = "insert into " + properties.getCacheDatabaseTableName() + " (Brand, Model, Commercial_Model, Precise_Model, CNIT, TW, Fuel_Type, Hybrid, "
				+ "Fiscale_Power, `Puissance maximale`, `Boîte de vitesse`, Urban_Consumption, Extra_Urban_Consumption, Mixed_consumption, "
				+ "CO2, `CO type I`, HC, NOx, `HC+NOx`, Particules, `Masse vide euro min`, `Masse vide euro max`, `Champ v9`, Year, Style, Gamme, id) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		int newId = 10000;
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = myConn.prepareStatement(query);

			for (String[] record : records) {
				// prepare and execute only statements, that have necessary information like consumption
				if (isRecordValid(record)) {
					for (int i = 0; i < 26; i++) {
						if (record[i].equals("")) preparedStatement.setNull(i + 1, Types.VARCHAR);
						else preparedStatement.setString(i + 1, record[i]);
					}
					preparedStatement.setInt(27, newId);
					preparedStatement.addBatch();
					newId++;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				assert preparedStatement != null;
				preparedStatement.executeBatch();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private Boolean isRecordValid(String[] record) {
		if (record[0].equals("") || record[3].equals("") || record[6].equals("") || record[11].equals("")
				|| record[12].equals("") || record[14].equals("")) {
			return false;
		}

		List<String> fuelTypes = new ArrayList<>();
		fuelTypes.add(RestConsumer.translateFuelToDatabaseRepresentation("petrol"));
		fuelTypes.add(RestConsumer.translateFuelToDatabaseRepresentation("diesel"));
		fuelTypes.add(RestConsumer.translateFuelToDatabaseRepresentation("cng"));
		fuelTypes.add(RestConsumer.translateFuelToDatabaseRepresentation("electricity"));

		return fuelTypes.contains(record[6]);
	}

	private ArrayList<String> getDbResultList(PreparedStatement ps, String column) throws SQLException {
		ArrayList<String> returnList = new ArrayList<>();
		ResultSet myRs = ps.executeQuery();
		while (myRs.next()) {
			returnList.add(myRs.getString(column));
		}
		return returnList;
	}

	public ObjectNode getBrandsAsJson() throws SQLException {
		String query = "select distinct Brand from " + properties.getCacheDatabaseTableName();
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		ArrayList<String> brands = getDbResultList(preparedStatement, "Brand");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode brandsNode = resultNode.putArray("brands");

		for (String brand : brands) brandsNode.add(brand);

		return resultNode;
	}

	public ObjectNode getModelsAsJson(String brand) throws SQLException {
		String query = String.format("select distinct Precise_Model from %s where Brand=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, brand);
		ArrayList<String> models = getDbResultList(preparedStatement, "Precise_Model");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode modelsNode = resultNode.putArray("models");

		for (String model : models) modelsNode.add(model);

		return resultNode;
	}

	public ObjectNode getModelsByFuelAsJson(String brand, String fuel) throws SQLException {
		String query = String.format("select distinct Precise_Model from %s where Brand=? and Fuel_Type=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, brand);
		preparedStatement.setString(2, RestConsumer.translateFuelToDatabaseRepresentation(fuel));
		ArrayList<String> models = getDbResultList(preparedStatement, "Precise_Model");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode modelsNode = resultNode.putArray("models");

		for (String model : models) modelsNode.add(model);

		return resultNode;
	}

	public ObjectNode getFuelAsJson(String brand, String model) throws SQLException {
		String query = String.format("select distinct Fuel_Type from %s where Brand=? and Precise_Model=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, brand);
		preparedStatement.setString(2, model);
		ArrayList<String> fuelList = getDbResultList(preparedStatement, "Fuel_Type");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode fuelsNode = resultNode.putArray("fuel");

		for (String fuel : fuelList) fuelsNode.add(translateFuelToAgentRepresentation(fuel));

		return resultNode;
	}

	public ObjectNode getFuelByBrandAsJson(String brand) throws SQLException {
		String query = String.format("select distinct Fuel_Type from %s where Brand=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, brand);
		ArrayList<String> fuelList = getDbResultList(preparedStatement, "Fuel_Type");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		ArrayNode fuelsNode = resultNode.putArray("fuel");

		for (String fuel : fuelList) fuelsNode.add(translateFuelToAgentRepresentation(fuel));

		return resultNode;
	}

	public ObjectNode getCarIdAsJson(String brand, String model, String fuel) throws SQLException {
		String query = String.format("select id from %s where " +
				"Brand=? and " +
				"Precise_Model=? and " +
				"Fuel_Type=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, brand);
		preparedStatement.setString(2, model);
		preparedStatement.setString(3, RestConsumer.translateFuelToDatabaseRepresentation(fuel));
		ArrayList<String> idList = getDbResultList(preparedStatement, "id");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode resultNode = objectMapper.createObjectNode();
		resultNode.put("id", idList.get(0));

		return resultNode;
	}

	public Car getCar(String id) throws SQLException {
		String query = String.format("select * from %s where id=?", properties.getCacheDatabaseTableName());
		PreparedStatement preparedStatement = myConn.prepareStatement(query);
		preparedStatement.setString(1, id);

		ResultSet myRs = preparedStatement.executeQuery();
		if (myRs.first()) {
			return new Car(id, myRs.getString("Brand"), myRs.getString("Precise_Model"), translateFuelToAgentRepresentation(myRs.getString("Fuel_Type")), myRs.getString("Urban_Consumption"),
					myRs.getString("Extra_Urban_Consumption"), myRs.getString("Extra_Urban_Consumption"), myRs.getString("CO2"));
		}

		return null;
	}

}
