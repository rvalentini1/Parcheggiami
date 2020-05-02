package com.example.parcheggiami;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Park {
    private String latitude;
    private String longitude;
    private String libero;
    public Park(){

    }
    public Park(String vlat,String vlongitude,String vLibero){
        latitude=vlat;
        longitude=vlongitude;
        libero=vLibero;
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
    public void SetCord(String vlat,String vlongitude,String vLibero){
        latitude=vlat;
        longitude=vlongitude;
        libero=vLibero;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("libero", libero);
        return result;
    }

}
