package com.gve.proiectdsdm;

import android.net.Uri;

public class SearchItem {

    private Uri photo;
    private String name;
    private String date;

    public SearchItem(Uri photo, String name, String date) {
        this.photo = photo;
        this.name = name;
        this.date = date;
    }

    public Uri getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}
