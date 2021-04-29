package com.example.thegoforlunch.util;


import android.Manifest;

/**
 * Global constants for the Go4Lunch app.
 */
public abstract class AppConstants {
    public static final int RC_LOCATION_PERMISSIONS = 1234;
    public static final int RC_CALL_PHONE_PERMISSION = 2345;
    public static final int RC_CHECK_LOCATION_SETTINGS = 3456;
    public static final int RC_READ_EXTERNAL_STORAGE_PERMISSION = 7890;
    public static final int DEFAULT_INTERVAL = 10000;
    public static final int FASTEST_INTERVAL = 5000;
    public static final String RESTAURANT_ID_EXTRA = "restaurantId";
    public static final String SELECTED_RESTAURANT_ID_FIELD = "selectedRestaurantId";
    public static final String LIKED_RESTAURANTS_ID_FIELD = "likedRestaurants";
    public static final String NAME_ID_FIELD = "name";
    public static final String URL_PICTURE_ID_FIELD = "urlPicture";
    public static final String SHARED_PREFERENCES_NAME = "com.example.myfirebaseapp.go4lunch";
    public static final String NOTIFICATIONS_PREFERENCES_NAME = "com.example.myfirebaseapp.notifications";
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final int RC_GOOGLE_SIGN_IN = 100;
    public static final int RC_IMAGE_PERMS = 100;
    public static final int RC_CHOOSE_PHOTO = 200;
    public static final int REQUEST_CALL = 100;

    public static final String RESTAURANT_ID = "restaurantId";


    public static final String CHOOSEN = "CHOOSEN";
    public static final String UNCHOOSEN = "UNCHOOSEN";
}
