package com.example.parcheggiami;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Park {
    private String latitude;
    private String longitude;
    private String libero;
    private String user;
    public Park(){

    }
    public Park(String vlat,String vlongitude,String vLibero,String vUser){
        latitude=vlat;
        longitude=vlongitude;
        libero=vLibero;
        user=vUser;
    }
    public String getLatitude(){
        return latitude;
    }
    public String getLongitude(){
        return longitude;
    }
    public String getLibero(){
        return libero;
    }
    public String getUser(){
        return user;
    }
    public void SetCord(String vlat,String vlongitude,String vLibero,String vUser){
        latitude=vlat;
        longitude=vlongitude;
        libero=vLibero;
        user=vUser;
    }
    public int libera(){

        libero="1";
        user="";
        return 1;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("libero", libero);
        result.put("user", user);
        return result;
    }

}
