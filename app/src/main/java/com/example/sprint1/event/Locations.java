package com.example.sprint1.event;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public enum Locations {
    CCB("College of Computing", new LatLng(33.77712998488034, -84.39745910054616)),
    CRC("Campus Recreation Center", new LatLng(33.775452837880074, -84.40310145622968)),
    CULC("Clough Commons", new LatLng(33.774955254248766, -84.39624750430211)),
    STUDENT_CENTER("Student Center", new LatLng(33.77377565805104, -84.39851083563092)),
    SUBLIME("Sublime Doughnuts", new LatLng(33.781881745304716, -84.40491016895514)),
    TECH_SQUARE("Tech Square", new LatLng(33.77684804988302, -84.38882545418105));

    private final String name;
    private final LatLng latLng;

    Locations(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return this.latLng;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public static Locations getLocationByName(String name) {
        for (Locations location : Locations.values()) {
            if (location.name.equals(name)) {
                return location;
            }
        }
        return null;
    }
}
