package pl.snowdog.privparksmartblocker.db;


public interface DatabaseListener {

    void onDbStateChange(boolean value);

    void onDbSpotAvailabilityChange(boolean value);

}
