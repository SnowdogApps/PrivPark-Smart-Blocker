package pl.snowdog.privparksmartblocker.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RemoteDbProvider {

    private static final String STATE_PROPERTY = "state";
    private static final String SPOT_AVAILABILITY_PROPERTY = "spot_available";
    private static final String CAR_PLATE_PROPERTY = "car_plate";

    private DatabaseReference mStatePropertyReference;
    private DatabaseReference mSpotAvailabilityReference;
    private DatabaseReference mCarPlateReference;
    private DatabaseListener mDbListener;

    private boolean mSpotAvailability;


    private FirebaseDatabase mDatabase;

    public void initDb(DatabaseListener listener) {


        mDbListener = listener;

        mDatabase = FirebaseDatabase.getInstance();

        mStatePropertyReference = mDatabase.getReference(STATE_PROPERTY);
        mStatePropertyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value = dataSnapshot.getValue(boolean.class);
                mDbListener.onDbStateChange(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSpotAvailabilityReference = mDatabase.getReference(SPOT_AVAILABILITY_PROPERTY);
        mSpotAvailabilityReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value = dataSnapshot.getValue(boolean.class);
                mDbListener.onDbSpotAvailabilityChange(value);
                mSpotAvailability = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mCarPlateReference = mDatabase.getReference(CAR_PLATE_PROPERTY);
    }


    public void setCarPlate(String carPlate) {
        mCarPlateReference.setValue(carPlate);
    }

    public void setState(boolean value) {
        mStatePropertyReference.setValue(value);
    }

    public boolean isSpotAvailable() {
        return mSpotAvailability;
    }


}
