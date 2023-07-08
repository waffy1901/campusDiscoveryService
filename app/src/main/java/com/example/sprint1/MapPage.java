package com.example.sprint1;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sprint1.event.Event;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
For reference:
1. https://developers.google.com/maps/documentation/android-sdk/config
2. https://developers.google.com/maps/documentation/android-sdk/map
3. Map styling: https://mapstyle.withgoogle.com/
 */

public class MapPage extends AppCompatActivity implements OnMapReadyCallback {

    private static User currUser;
    private static Intent intent;
    private static final String TAG = MapPage.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file as the content view.
        setContentView(R.layout.activity_map_page);

        // Gets extra info from intent
        intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");

        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready for use.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        try {
            // Customise the styling of the base map using a JSON object defined in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Add event markers to map
        List<Event> eventList = Event.generateEventList(DBUtil.getEventList(currUser.getToken()));

        // Filter events
        String dayFilter = intent.getStringExtra("dayFilter");
        String locationFilter = intent.getStringExtra("locationFilter");
        boolean capacityFilter = intent.getBooleanExtra("capacityFilter", false);
        if (!dayFilter.equals("Day: All")) {
            eventList.removeIf(event -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
                DayOfWeek day = LocalDate.parse(event.getDay(), formatter).getDayOfWeek();
                return !day.toString().equals(dayFilter.toUpperCase());
            });
        }
        if (!locationFilter.equals("Location: All")) {
            eventList.removeIf(event ->
                    !event.getLocation().toString().equals(locationFilter)
            );
        }
        if (capacityFilter) {
            eventList.removeIf(event ->
                    event.getCapacity() == DBUtil.getRsvpWillAttend(currUser.getToken(), event.getEventId()).length()
            );
        }

        for (Event event : eventList) {
            googleMap.addMarker(new MarkerOptions()
                    .position(event.getLocation().getLatLng())
                    .title(event.getName()))
                    .setTag(event);
        }

        // Called when the user clicks a marker
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                // Retrieve the data from the marker.
                Event event = (Event) marker.getTag();

                // Go to event info page
                Intent intent = new Intent(MapPage.this, EventInfoPage.class);
                intent.putExtra("editTools", event.getOwner().equals(currUser.getId()) || currUser.isModerator());
                intent.putExtra("currUser", currUser);
                intent.putExtra("event", event);
                startActivity(intent);

                return false;
            }
        });
    }
}