package com.example.sprint1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial login screen class
 */
public class LoginPage extends AppCompatActivity {

    public static HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        Spinner spinner = findViewById(R.id.userType);

        // Convert array to a list
        List<String> usersList = new ArrayList<>();
        usersList.add("Select a User Type...");
        usersList.add("Student");
        usersList.add("Teacher");
        usersList.add("Organizer");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> spinnerArrayAdapter
                = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                usersList
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
        spinnerArrayAdapter.setDropDownViewResource(
                android.R.layout.simple_dropdown_item_1line
        );

        // Spinner on item selected listener
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view,
                            int position, long id) {

                        // Get the spinner selected item text
                        String selectedItemText = (String) parent
                                .getItemAtPosition(position);

                        // If user change the default selection
                        // First item is disable and
                        // it is used for hint
                        if (position > 0) {
                            // Notify the selected item text
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Selected : "
                                            + selectedItemText,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                });

        View loginBtn = findViewById(R.id.loginBtn);
        View createAccountBtn = findViewById(R.id.createAccountBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText etUserName = findViewById(R.id.userEmail);
                        EditText etUserName2 = findViewById(R.id.userPassword);
                        String strUserName = etUserName.getText().toString();
                        String strUserName2 = etUserName2.getText().toString();
                        if (TextUtils.isEmpty(strUserName) ||
                                spinner.getSelectedItem().toString().equals("Select a User Type...") ||
                                TextUtils.getTrimmedLength(strUserName) == 0 ||
                                TextUtils.getTrimmedLength(strUserName2) == 0) {
                            etUserName.setError("Cannot proceed without a valid email, password and User Type.");
                        } else {
                            BufferedReader reader;
                            String line;
                            StringBuilder responseContent = new StringBuilder();
                            JSONObject curr = new JSONObject();
                            try {
                                curr.put("email", strUserName);
                                curr.put("password", strUserName2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                URL url = new URL("https://campusdiscovery.herokuapp.com/users/login");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setDoOutput(true);
                                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                                wr.write(curr.toString().getBytes());
                                int status = connection.getResponseCode();
                                System.out.println(status);
                                if (status > 299) {
                                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                                    while ((line = reader.readLine()) != null) {
                                        responseContent.append(line);
                                    }
                                    reader.close();
                                    etUserName.setError("Invalid Username/Password");
                                } else {
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    while ((line = reader.readLine()) != null) {
                                        responseContent.append(line);
                                    }
                                    reader.close();

                                    JSONObject userJSON = new JSONObject(responseContent.toString());
                                    JSONObject userInfoJSON = new JSONObject(userJSON.getString("user"));
                                    User currUser = new User(userInfoJSON.getString("_id"),
                                            userJSON.getString("token"),
                                            userInfoJSON.getString("userType"));
                                    Intent i = new Intent(LoginPage.this, HomePage.class);
                                    i.putExtra("currUser", currUser);
                                    startActivity(i);
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText etUserName = findViewById(R.id.userEmail);
                        EditText etUserName2 = findViewById(R.id.userPassword);
                        String strUserName = etUserName.getText().toString();
                        String strUserName2 = etUserName2.getText().toString();
                        String strUserType = spinner.getSelectedItem().toString();
                        if (TextUtils.isEmpty(strUserName) ||
                                spinner.getSelectedItem().toString().equals("Select a User Type...") ||
                                TextUtils.getTrimmedLength(strUserName) == 0 ||
                                TextUtils.getTrimmedLength(strUserName2) == 0) {
                            etUserName.setError("Cannot proceed without a valid email, password and User Type.");
                        } else {
                            BufferedReader reader;
                            String line;
                            StringBuffer responseContent = new StringBuffer();
                            JSONObject curr = new JSONObject();
                            try {
                                curr.put("email", strUserName);
                                curr.put("password", strUserName2);
                                curr.put("userType", strUserType);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                URL url = new URL("https://campusdiscovery.herokuapp.com/users");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setDoOutput(true);
                                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                                wr.write(curr.toString().getBytes());
                                int status = connection.getResponseCode();
                                System.out.println(status);
                                if (status > 299) {
                                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                                    while ((line = reader.readLine()) != null) {
                                        responseContent.append(line);
                                    }
                                    reader.close();
                                    etUserName.setError("Email is already in use");
                                    System.out.println(responseContent);
                                } else {
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    while ((line = reader.readLine()) != null) {
                                        responseContent.append(line);
                                    }
                                    reader.close();

                                    JSONObject userJSON = new JSONObject(responseContent.toString());
                                    JSONObject userInfoJSON = new JSONObject(userJSON.getString("user"));
                                    User currUser = new User(userInfoJSON.getString("_id"),
                                            userJSON.getString("token"),
                                            userInfoJSON.getString("userType"));
                                    Intent i = new Intent(LoginPage.this, HomePage.class);
                                    i.putExtra("currUser", currUser);
                                }
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }
        );
        spinner.setAdapter(spinnerArrayAdapter);
    }
}
