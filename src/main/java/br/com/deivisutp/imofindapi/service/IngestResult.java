package br.com.deivisutp.imofindapi.service;

public class IngestResult {

    private final String externalId;
    private final boolean observationCreated;
    private final int eventsCreated;

    public IngestResult(String externalId, boolean observationCreated, int eventsCreated) {
        this.externalId = externalId;
        this.observationCreated = observationCreated;
        this.eventsCreated = eventsCreated;
    }

    public String getExternalId() {
        return externalId;
    }

    public boolean isObservationCreated() {
        return observationCreated;
    }

    public int getEventsCreated() {
        return eventsCreated;
    }
}
