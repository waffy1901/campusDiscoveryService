package com.example.sprint1;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.sprint1.event.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RSVPInfoPage extends AppCompatActivity {

    private static User currUser;
    private static Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsvp_info);

        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");
        event = (Event) intent.getSerializableExtra("event");

        showEditTools();

        /* ------------------------------ RSVP Spinner ------------------------------ */
        Spinner rsvpTypeSpin = findViewById(R.id.rsvpTypeSpin);
        List<String> rsvpTypes = new ArrayList<>();
        rsvpTypes.add("Will attend");
        rsvpTypes.add("Might attend");
        rsvpTypes.add("Won't attend");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> rsvpTypeSpinAdapter
                = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                rsvpTypes
        ) {

            @Override
            public View getDropDownView(
                    int position, View convertView,
                    @NonNull ViewGroup parent) {

                // Get the item view
                return super.getDropDownView(position, convertView, parent);
            }
        };

        // Set the drop down view resource
        rsvpTypeSpinAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Spinner on item selected listener
        rsvpTypeSpin.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        populateRSVPList();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
        rsvpTypeSpin.setAdapter(rsvpTypeSpinAdapter);
        rsvpTypeSpin.setSelection(0);

        EditText removeRSVPInput = findViewById(R.id.removeRSVPInput);


        //Remove submit button
        Button removeRSVPBtn = findViewById(R.id.removeRSVPBtn);
        removeRSVPBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                  String removeRSVP = removeRSVPInput.getText().toString();
                  DBUtil.deleteRsvpStatus(currUser.getToken(), event.getEventId(), removeRSVP);
            }
        });
    }

    private void showEditTools() {
        if (!getIntent().getBooleanExtra("editTools", false)) {
            findViewById(R.id.editTools).setVisibility(View.GONE);
        }
    }

    private void populateRSVPList() {
        Spinner rsvpTypeSpin = findViewById(R.id.rsvpTypeSpin);
        JSONArray jsonList;

        if (rsvpTypeSpin.getSelectedItemPosition() == 0) {
            jsonList = DBUtil.getRsvpWillAttend(currUser.getToken(), event.getEventId());
        } else if (rsvpTypeSpin.getSelectedItemPosition() == 1) {
            jsonList = DBUtil.getRsvpMightAttend(currUser.getToken(), event.getEventId());
        } else {
            jsonList = DBUtil.getRsvpWontAttend(currUser.getToken(), event.getEventId());
        }

        StringBuilder list = new StringBuilder();
        list.append("Total: ").append(jsonList.length()).append("\n\n");
        for (int i = 0; i < jsonList.length(); i++) {
            try {
            JSONObject user = jsonList.getJSONObject(i);
                list.append(user.getString("username")).append("\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ((TextView) findViewById(R.id.rsvpList)).setText(list);
    }
}
