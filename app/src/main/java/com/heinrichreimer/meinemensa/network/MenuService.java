package com.heinrichreimer.meinemensa.network;

import com.heinrichreimer.meinemensa.annotations.Location;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


interface MenuService {
    @GET("speiseplan_gadget")
    Call<ResponseBody> loadMenu(@Query("day") int day, @Query("month") int month,
                                @Query("year") int year, @Query("location_id") @Location int location);
}
