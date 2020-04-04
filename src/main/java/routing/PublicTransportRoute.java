package routing;

public class PublicTransportRoute {

	private final Double shortDistanceKM;
	private final Double longDistanceKM;

	public PublicTransportRoute(Double shortDistanceKM, Double longDistanceKM) {
		this.shortDistanceKM = shortDistanceKM;
		this.longDistanceKM = longDistanceKM;
	}

	public Double getShortDistanceKM() {
		return shortDistanceKM;
	}

	public Double getLongDistanceKM() {
		return longDistanceKM;
	}
}
