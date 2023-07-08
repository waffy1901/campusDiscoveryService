package com.example.sprint1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprint1.event.Event;
import com.example.sprint1.event.EventAdapter;
import com.example.sprint1.event.Locations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    public static User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Gets extra info from intent
        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");

        String[] days = {"Day: All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        Spinner daySpin = findViewById(R.id.daySpinFilter);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> dayAdapter
                = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                days
        );
        // Specify the layout to use when the list of choices appears
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        daySpin.setAdapter(dayAdapter);

        List<String> locations = new ArrayList<>();
        for (Locations location : Locations.values()) {
            locations.add(location.toString());
        }
        locations.add(0, "Location: All");

        Spinner locationSpin = (Spinner) findViewById(R.id.locationSpinFilter);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> locationAdapter
                = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locations
        );
        // Specify the layout to use when the list of choices appears
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        locationSpin.setAdapter(locationAdapter);

        populateEventList();

        Button filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                populateEventList();
            }
        });

        Button addEventBtn = findViewById(R.id.addEventBtn);
        addEventBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, AddEventPage.class);
                intent.putExtra("currUser", currUser);
                startActivity(intent);
            }
        });

        Button myEventsBtn = findViewById(R.id.myEventsBtn);
        myEventsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MyEventsPage.class);
                intent.putExtra("currUser", currUser);
                startActivity(intent);
            }
        });

        Button mapBtn = findViewById(R.id.MapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MapPage.class);
                intent.putExtra("currUser", currUser);
                intent.putExtra("dayFilter", ((Spinner) findViewById(R.id.daySpinFilter)).getSelectedItem().toString());
                intent.putExtra("locationFilter", ((Spinner) findViewById(R.id.locationSpinFilter)).getSelectedItem().toString());
                intent.putExtra("capacityFilter", ((CheckBox) findViewById(R.id.capacityFilter)).isChecked());
                startActivity(intent);
            }
        });

        Button my_rsvp_btn = findViewById(R.id.my_rsvp_btn);
        my_rsvp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, MyRSVP.class);
                intent.putExtra("currUser", currUser);
                startActivity(intent);
            }
        });
    }

    private void populateEventList() {
        // Construct the data source
        List<Event> eventList = Event.generateEventList(DBUtil.getEventList(currUser.getToken()));

        // Filter events
        Spinner daySpinFilter = findViewById(R.id.daySpinFilter);
        Spinner locationSpinFilter = findViewById(R.id.locationSpinFilter);
        if (daySpinFilter.getSelectedItemPosition() != 0) {
            eventList.removeIf(event -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
                DayOfWeek day = LocalDate.parse(event.getDay(), formatter).getDayOfWeek();
                return !day.toString().equals(daySpinFilter.getSelectedItem().toString().toUpperCase());
            });
        }
        if (locationSpinFilter.getSelectedItemPosition() != 0) {
            eventList.removeIf(event ->
                    event.getLocation().toString() != locationSpinFilter.getSelectedItem()
            );
        }
        if (((CheckBox) findViewById(R.id.capacityFilter)).isChecked()) {
            eventList.removeIf(event ->
                    event.getCapacity() == DBUtil.getRsvpWillAttend(currUser.getToken(), event.getEventId()).length()
            );
        }

        // Sort event list by date
        eventList.sort(Event.eventComparator);

        // Create the adapter to convert the array to views
        EventAdapter adapter = new EventAdapter(this, eventList, currUser);

        // Attach the adapter to a RecyclerView
        RecyclerView recycler = findViewById(R.id.rcyEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(layoutManager);
    }
}
