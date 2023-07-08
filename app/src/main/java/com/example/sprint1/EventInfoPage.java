package com.example.sprint1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sprint1.event.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EventInfoPage extends AppCompatActivity {

    private static Event event;
    private static HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        Intent intent = getIntent();
        User currUser = (User) intent.getSerializableExtra("currUser");
        event = (Event) intent.getSerializableExtra("event");

        showEditTools();
        populateFields();

        /* ------------------------------ RSVP Spinner ------------------------------ */
        Spinner rsvpTypeSpin = findViewById(R.id.eventInfo_rsvpTypeSpin);
        List<String> rsvpTypes = new ArrayList<>();
        rsvpTypes.add("Select RSVP status...");
        rsvpTypes.add("Will attend");
        rsvpTypes.add("Might attend");
        rsvpTypes.add("Won't attend");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> rsvpSpinnerAdapter
                = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                rsvpTypes
        ) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }

            @Override
            public View getDropDownView(
                    int position, View convertView,
                    @NonNull ViewGroup parent) {

                // Get the item view
                View view = super.getDropDownView(
                        position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Set the drop down view resource
        rsvpSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_dropdown_item_1line
        );

        // Spinner on item selected listener
        rsvpTypeSpin.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view,
                            int position, long id) {
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                });
        rsvpTypeSpin.setAdapter(rsvpSpinnerAdapter);

        String userStatus = DBUtil.getRsvpStatus(currUser.getToken(), event.getEventId());
        if (userStatus == null) {
            rsvpTypeSpin.setSelection(0);
        } else if (userStatus.equals("Will attend")) {
            rsvpTypeSpin.setSelection(1);
        } else if (userStatus.equals("Might attend")) {
            rsvpTypeSpin.setSelection(2);
        } else {
            rsvpTypeSpin.setSelection(3);
        }

        /* ------------------------------ Listeners ------------------------------ */
        Button rsvpSubmitBtn = findViewById(R.id.eventInfo_rsvpSubmitBtn);
        rsvpSubmitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Spinner rsvpTypeSpin = findViewById(R.id.eventInfo_rsvpTypeSpin);
                // Checks for invalid selection and full capacity
                if (rsvpTypeSpin.getSelectedItemPosition() == 0) {
                    setSpinError("Must select status!");
                    return;

                } else if (rsvpTypeSpin.getSelectedItemPosition() == 1 &&
                        DBUtil.getRsvpWillAttend(currUser.getToken(), event.getEventId()).length() == event.getCapacity()) {
                    setSpinError("Event capacity is full!");
                    return;
                }

                if (rsvpTypeSpin.getSelectedItemPosition() == 1) {
                    JSONArray myRSVPs = DBUtil.getMyRSVPs(currUser.getToken());
                    List<JSONObject> myList = new ArrayList<>();
                    for (int i = 0; i < myRSVPs.length(); i++) {
                        JSONObject curr = null;
                        try {
                            curr = myRSVPs.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        myList.add(curr);
                    }
                    List<Event> list = Event.generateEventList(myList);
                    for (Event e : list) {
                        if (e != event && e.getTime().equals(event.getTime()) && e.getDay().equals(event.getDay())) {
                            setSpinError("Time Conflict");
                            return;
                        }
                    }
                }

                // Posts or patches RSVP status
                if (DBUtil.getRsvpStatus(currUser.getToken(), event.getEventId()) == null) {
                    DBUtil.postRsvpStatus(currUser.getToken(), event.getEventId(), rsvpTypeSpin.getSelectedItem().toString());
                } else {
                    DBUtil.patchRsvpStatus(currUser.getToken(), event.getEventId(), rsvpTypeSpin.getSelectedItem().toString());
                }
                Toast.makeText(getApplicationContext(),"RSVP status selected", Toast.LENGTH_SHORT).show();
            }
        });

        Button rsvpInfoBtn = findViewById(R.id.eventInfo_rsvpInfoBtn);
        rsvpInfoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EventInfoPage.this, RSVPInfoPage.class);
                intent.putExtra("editTools", event.getOwner().equals(currUser.getId()) || currUser.isModerator());
                intent.putExtra("currUser", currUser);
                intent.putExtra("event", event);
                startActivity(intent);
            }
        });

        Button editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EventInfoPage.this, EditEventPage.class);
                intent.putExtra("currUser", currUser);
                intent.putExtra("event", event);
                startActivity(intent);
            }
        });

        Button deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                BufferedReader reader;
                String line;
                StringBuilder responseContent = new StringBuilder();
                try {
                    URL url = new URL("https://campusdiscovery.herokuapp.com/events/" + event.getEventId());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + currUser.getToken());
                    connection.setDoOutput(true);
                    int status = connection.getResponseCode();
                    System.out.println(status);
                    if (status > 299) {
                        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        while ((line = reader.readLine()) != null) {
                            responseContent.append(line);
                        }
                        reader.close();
                    } else {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            responseContent.append(line);
                        }
                        reader.close();

                        Intent intent = new Intent(EventInfoPage.this, HomePage.class);
                        intent.putExtra("currUser", currUser);
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showEditTools() {
        if (!getIntent().getBooleanExtra("editTools", false)) {
            findViewById(R.id.editTools).setVisibility(View.GONE);
        }
    }

    private void populateFields() {
        TextView textView;
        textView = findViewById(R.id.eventInfo_name);
        textView.setText(event.getName());
        textView = findViewById(R.id.eventInfo_dateTime);
        textView.setText(event.getDay());
        textView = findViewById(R.id.eventInfo_location);
        textView.setText(event.getLocation().toString());
        textView = findViewById(R.id.eventInfo_dateTime);
        textView.setText(event.getDay());
        textView = findViewById(R.id.eventInfo_host);
        textView.setText(event.getHostname());
        textView = findViewById(R.id.eventInfo_description);
        textView.setText(event.getDescription());
        textView = findViewById(R.id.eventInfo_capacity);
        textView.setText("Capacity: " + event.getCapacity());
        textView = findViewById(R.id.eventInfo_time);
        textView.setText(event.getTime());
    }

    private void setSpinError(String errorMessage) {
        Spinner rsvpTypeSpin = findViewById(R.id.eventInfo_rsvpTypeSpin);
        ((TextView) rsvpTypeSpin.getSelectedView()).setError(errorMessage);

        Toast.makeText(
                getApplicationContext(),
                errorMessage,
                Toast.LENGTH_SHORT).show();
    }
}
