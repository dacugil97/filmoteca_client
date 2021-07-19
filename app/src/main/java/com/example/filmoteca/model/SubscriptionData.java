package com.example.filmoteca.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionData {

    @SerializedName("subscriptions")
    @Expose
    private List<WatchListSub> subs;

    public List<WatchListSub> getSubs() {
        return subs;
    }

    public void setSubs(List<WatchListSub> subs) {
        this.subs = subs;
    }
}
