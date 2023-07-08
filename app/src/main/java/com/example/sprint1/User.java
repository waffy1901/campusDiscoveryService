package com.example.sprint1;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    private final String id;
    private final String token;
    private final boolean moderator;

    public User(String id, String token, String userType) {
        this.id = id;
        this.token = token;
        this.moderator = !userType.equals("Student");
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", token='" + token + '\'' +
                ", moderator=" + moderator +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public boolean isModerator() {
        return moderator;
    }
}
