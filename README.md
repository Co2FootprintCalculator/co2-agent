# CO2 Footprint Agent

This project implements a CO2 calculator for cars and public transport as an agent using the JIAC agent framework. The
agent offers a REST API for access.

## Getting started

### Prerequisites

In order to build and execute the project, you will need Java 11, Maven and MySQL. Additionally, the agent uses the OpenRouteService
API, which requires an API key. You can sign up and create one [here](https://openrouteservice.org/dev/).

### Database

The agent caches a remote, open database into its own MySQL database for performance reasons. The database needs to have
the following structure: [TODO]

### Configuration

A sample configuration file can be found at `src/main/resources/config.sample.properties`. Add a local, untracked file
called `config.properties` in the same folder with the same structure as in the sample file. This is where you put your
OpenRouteService API key and the database properties. For testing purposes, you can also change the number of rows the
agent should download from the remote, open database.

The caching action is performed every 24 hours, but you can change the interval manually in `src/main/resources/Agent.xml`
by modifying the `executionInterval` property.

### Build

Simply run `mvn package`. Afterwards execute the `.jar` file.

## REST API

You can find the available REST methods in `src/main/java/beans/RESTfulBean.java` with details about their purposes,
their input parameters and the response. Every response is a Json formatted result.

The most important methods (calculating the CO2 emissions) are `/v1/calculation/emissions/car`, `/v2/calculation/length/emissions/car`, 
`/v2/calculation/locations/emissions/car` and `/calculation/emissions/publictransport`. You can use them without any of
the other methods, however you'll need to know the car ID from the database and - for electricity powered cars - the available
electricity mixes. These can be retrieved using the other methods. The agent also provides a way to find out the coordinates
of places using OpenRouteService.

Note that `/v2/calculation/locations/emissions/car` calculates the emissions based on the *shortest* route between the
given start and end coordinates (according to the OpenRouteService API).
