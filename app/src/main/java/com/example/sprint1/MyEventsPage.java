package com.example.sprint1;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprint1.event.Event;
import com.example.sprint1.event.EventAdapter;

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

public class MyEventsPage extends AppCompatActivity {

    public static HttpURLConnection connection;
    private static User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        // Gets extra info from intent
        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");

        populateEventList();
    }

    public static List<JSONObject> sendGETRequest() {
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        List<JSONObject> myList = null;
        try {
            URL url = new URL("https://campusdiscovery.herokuapp.com/events");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + currUser.getToken());
            int status = connection.getResponseCode();
            System.out.println(status + " myEvents");
            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();


            System.out.println(responseContent);
            JSONArray myArray = new JSONArray(responseContent.toString());
            myList = new ArrayList<>();
            for (int i = 0; i < myArray.length(); i++) {
                JSONObject curr = myArray.getJSONObject(i);
                myList.add(curr);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return myList;
    }

    private void populateEventList() {
        // Construct the data source
        List<Event> eventList = Event.generateEventList(sendGETRequest());

        // Create the adapter to convert the array to views
        EventAdapter adapter = new EventAdapter(this, eventList, currUser);

        // Attach the adapter to a RecyclerView
        RecyclerView recycler = findViewById(R.id.rcyEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(layoutManager);
    }
}