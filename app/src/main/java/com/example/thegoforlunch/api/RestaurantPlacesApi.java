package com.example.thegoforlunch.api;


import com.example.thegoforlunch.model.DetailsPOJO;
import com.example.thegoforlunch.model.Restaurant;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestaurantPlacesApi
{
    /**
     * Request HTTP in Json to have nearby Restaurants
     * @param location String with latitude and longitude of the current User
     * @param radius double to define the distance around the current User
     * @param type String to matches with Restaurant
     * @param key String API key
     * @return an Observable<RestaurantPOJO>
     */
    @GET("nearbysearch/json?")
    Observable<Restaurant> getNearbyRestaurants (@Query("location") String location,
                                                 @Query("radius") int radius,
                                                 @Query ("type") String type,
                                                 @Query("opening_hours") Boolean openingHours,
                                                 @Query("key") String key);


    /**
     * Request HTTP in Json to have the Restaurant's Details
     * @param placeId String provide by API Google
     * @param key String API key
     * @return an Observable<DetailPOJO>
     */
    @GET("details/json?")
    Observable<DetailsPOJO> getDetailRestaurants (@Query("place_id") String placeId,
                                                  @Query("key") String key);


    /**
     * Request HTTP in Json to have nearby Restaurants
     * @param key String API key
     * @param input String from EditText
     * @return an Observable<RestaurantPOJO>
     */
    @GET("queryautocomplete/json?")
    Observable<Restaurant> getAutocompleteRestaurants (@Query("key") String key,
                                                       @Query("input") String input,
                                                       @Query("location") String location,
                                                       @Query("radius") int radius);

    /**
     * Create an instance of Retrofit with the base url of API Google
     */
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
}
