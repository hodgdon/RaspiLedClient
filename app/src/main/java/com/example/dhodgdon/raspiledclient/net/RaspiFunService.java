package com.example.dhodgdon.raspiledclient.net;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

public interface RaspiFunService {
    @POST("/led_pattern")
    Call<LedPatternModel> postLedPattern(@Body LedPatternModel ledPattern);
}
