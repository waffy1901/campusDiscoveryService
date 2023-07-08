package com.example.sprint1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sprint1.event.Locations;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AddEventPage extends AppCompatActivity {

    private static User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");

        /* ------------------------------ RSVP Spinner ------------------------------ */
        Spinner locationSpin = findViewById(R.id.locationSpin);
        List<String> locationList = new ArrayList<>();
        for (Locations location : Locations.values()) {
            locationList.add(location.toString());
        }
        locationList.sort(null);
        locationList.add(0, "Select Location...");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> locationSpinAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locationList
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
        locationSpinAdapter.setDropDownViewResource(
                android.R.layout.simple_dropdown_item_1line
        );

        // Spinner on item selected listener
        locationSpin.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
        locationSpin.setAdapter(locationSpinAdapter);

        /* ------------------------------ Listeners ------------------------------ */
        Button createBtn = findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText eventName = findViewById(R.id.eventName);
                        EditText date = findViewById(R.id.date);
                        EditText time = findViewById(R.id.time);
                        EditText capacity = findViewById(R.id.capacity);
                        EditText description = findViewById(R.id.description);
                        EditText host = findViewById(R.id.host);

                        String strEventName = eventName.getText().toString();
                        String strTime = time.getText().toString();
                        String strCapacity = capacity.getText().toString();
                        String strDescription = description.getText().toString();
                        String strHost = host.getText().toString();
                        String strDate;

                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
                            strDate = LocalDate.parse(date.getText().toString(), formatter).format(formatter);
                        }
                        catch (Exception e) {
                            date.setError("Invalid date! Use M/D/YY");
                            return;
                        }

                        if (TextUtils.getTrimmedLength(strEventName) == 0
                                || TextUtils.getTrimmedLength(strDate) == 0
                                || TextUtils.getTrimmedLength(strCapacity) == 0
                                || TextUtils.getTrimmedLength(strDescription) == 0
                                || TextUtils.getTrimmedLength(strHost) == 0
                                || TextUtils.getTrimmedLength(strTime) == 0
                                || locationSpin.getSelectedItemPosition() == 0
                        ) {
                            date.setError("Invalid inputs!");
                        }
                        else {
                            BufferedReader reader;
                            String line;
                            StringBuffer responseContent = new StringBuffer();
                            JSONObject curr = new JSONObject();

                            try {
                                curr.put("name", strEventName);
                                curr.put("eventDate", strDate);
                                curr.put("time", strTime);
                                curr.put("location", locationSpin.getSelectedItem());
                                curr.put("capacity", strCapacity);
                                curr.put("description", strDescription);
                                curr.put("hostname", strHost);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            System.out.println(curr);
                            try{
                                URL url = new URL("https://campusdiscovery.herokuapp.com/events");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setRequestProperty("Authorization", "Bearer " + currUser.getToken());
                                connection.setDoOutput(true);
                                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                                wr.write(curr.toString().getBytes());
                                int status = connection.getResponseCode();
                                System.out.println(status);
                                if (status > 299) {
                                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                                }
                                else {
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                }
                                while ((line = reader.readLine()) != null) {
                                    responseContent.append(line);
                                }
                                reader.close();
                                System.out.println(responseContent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Intent intent = new Intent(AddEventPage.this, HomePage.class);
                        intent.putExtra("currUser", currUser);
                        startActivity(intent);
                    }
                }
        );
    }
}