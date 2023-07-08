package com.example.sprint1;

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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DBUtil {

    private static final String BASE_URL = "https://campusdiscovery.herokuapp.com/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static List<JSONObject> getEventList(String token) {
        List<JSONObject> eventList = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "events/all")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            JSONArray jsonArray = new JSONArray(response.body().string());
            eventList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject curr = jsonArray.getJSONObject(i);
                eventList.add(curr);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return eventList;
    }

    public static String getRsvpStatus(String token, String eventId) {
        String rsvpStatus = null;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        try {
            URL url = new URL(BASE_URL + "eventsRSVP/" + eventId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            if (connection.getResponseCode() > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            JSONObject dbObject = new JSONObject(responseContent.toString());
            if (dbObject.getBoolean("hasRespond") && !dbObject.isNull("rsvpMsg")) {
                rsvpStatus = dbObject.getString("rsvpMsg");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return rsvpStatus;
    }

    public static void postRsvpStatus(String token, String eventId, String status) {
        JSONObject newStatus = new JSONObject();
        try {
            newStatus.put("responseType", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(newStatus.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "eventsRSVP/" + eventId)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void patchRsvpStatus(String token, String eventId, String status) {
        JSONObject newStatus = new JSONObject();
        try {
            newStatus.put("responseType", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(newStatus.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "eventsRSVP/" + eventId)
                .addHeader("Authorization", "Bearer " + token)
                .patch(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRsvpStatus(String token, String eventId, String accountName) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "eventsRSVP/" + eventId + "/" + accountName;
        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getRsvpWillAttend(String token, String eventId) {
        JSONArray ans = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "eventsRSVP/" + eventId + "/willAttend")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            ans = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static JSONArray getRsvpMightAttend(String token, String eventId) {
        JSONArray ans = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "eventsRSVP/" + eventId + "/mightAttend")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            ans = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static JSONArray getRsvpWontAttend(String token, String eventId) {
        JSONArray ans = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "eventsRSVP/" + eventId + "/wontAttend")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            ans = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static JSONArray getMyRSVPs(String token) {
        JSONArray ans = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "events/myRSVP")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            ans = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ans;
    }
}
