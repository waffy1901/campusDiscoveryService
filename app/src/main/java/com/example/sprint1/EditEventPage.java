package com.example.sprint1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sprint1.event.Event;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditEventPage extends AppCompatActivity {

    private static User currUser;
    private static Event event;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        //Gets token info from intent
        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("currUser");
        event = (Event) intent.getSerializableExtra("event");

        //Retrieve text fields
        EditText eventName = findViewById(R.id.eventName);
        EditText date = findViewById(R.id.editDate);
        EditText time = findViewById(R.id.editTime);
        EditText capacity = findViewById(R.id.capacity);
        EditText description = findViewById(R.id.description);
        EditText host = findViewById(R.id.host);

        //Fill in text fields with data from database for this event
        eventName.setText(event.getName());
        date.setText(event.getDay());
        time.setText(event.getTime());
        description.setText(event.getDescription());
        host.setText(event.getHostname());
        capacity.setText(String.valueOf(event.getCapacity()));

        //Create new event based on the data in the text fields after "submit changes"
        Button submitChangesBtn = findViewById(R.id.submitChangesBtn);
        submitChangesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Creates new event based on the values currently in text fields
                String strEventName = eventName.getText().toString();
                String strTime =  time.getText().toString();
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
                        || TextUtils.getTrimmedLength(strTime) == 0
                        || TextUtils.getTrimmedLength(strCapacity) == 0
                        || TextUtils.getTrimmedLength(strDescription) == 0
                        || TextUtils.getTrimmedLength(strHost) == 0
                ) {
                    eventName.setError("Cannot proceed without a valid email, password and User Type.");
                } else {
                    JSONObject curr = new JSONObject();

                    try {
                        curr.put("name", strEventName);
                        curr.put("description", strDescription);
                        curr.put("capacity", strCapacity);
                        curr.put("eventDate", strDate);
                        curr.put("hostname", strHost);
                        curr.put("time", strTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(curr);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(curr.toString(), JSON);
                    String url = "https://campusdiscovery.herokuapp.com/events/" + event.getEventId();
                    Request request = new Request.Builder()
                            .url(url)
                            .patch(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        System.out.println(response.code());
                        System.out.println(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(EditEventPage.this, HomePage.class);
                intent.putExtra("currUser", currUser);
                startActivity(intent);
            }
        });
    }
}
