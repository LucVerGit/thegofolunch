package com.example.thegoforlunch.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.List;

import com.example.thegoforlunch.model.AutocompletePOJO;
import com.example.thegoforlunch.model.Restaurant;
import com.example.thegoforlunch.model.User;
import com.example.thegoforlunch.repository.GooglePlaceRepository;
import com.example.thegoforlunch.repository.UserRepository;

public class AppViewModel extends ViewModel {


    // private static
    private static final String TAG = AppViewModel.class.getSimpleName();


    // variables
    private final GooglePlaceRepository mGooglePlaceRepository;
    private final UserRepository mUserRepository;
    private final MutableLiveData<Location> mDeviceLocation;
    private final MutableLiveData<Boolean> mIsLocationActivated;


    // constructors
    public AppViewModel() {
        mGooglePlaceRepository = GooglePlaceRepository.getInstance();
        mUserRepository = UserRepository.getInstance();
        mDeviceLocation = new MutableLiveData<>();
        mIsLocationActivated = new MutableLiveData<>();
    }


    // methods

    /**
     * @see GooglePlaceRepository#getNearbyRestaurantsLiveData
     */
    public LiveData<List<Restaurant>> getNearbyRestaurantsLiveData() {
        return mGooglePlaceRepository.getNearbyRestaurantsLiveData();
    }

    /**
     * @see GooglePlaceRepository#setNearbyRestaurantsLiveData
     */
    public void setNearbyRestaurantsLiveData(String keyword, String type, String location, int radius) {
        mGooglePlaceRepository.setNearbyRestaurantsLiveData(keyword, type, location, radius);
    }

    /**
     * @see GooglePlaceRepository#getDetailsRestaurantLiveData
     */
    public LiveData<Restaurant> getDetailsRestaurantLiveData() {
        return mGooglePlaceRepository.getDetailsRestaurantLiveData();
    }

    /**
     * @see GooglePlaceRepository#setDetailsRestaurantLiveData
     */
    public void setDetailsRestaurantLiveData(String placeId) {
        mGooglePlaceRepository.setDetailsRestaurantLiveData(placeId);
    }

    /**
     * @see GooglePlaceRepository#getAutocompletePredictionsLiveData
     */
    public LiveData<List<AutocompletePOJO.Prediction>> getAutocompletePredictionsLiveData() {
        return mGooglePlaceRepository.getAutocompletePredictionsLiveData();
    }

    /**
     * @see GooglePlaceRepository#setAutocompletePredictionsLiveData
     */
    public void setAutocompletePredictionsLiveData(String input, String types, String location, int radius, String sessionToken) {
        mGooglePlaceRepository.setAutocompletePredictionsLiveData(input, types, location, radius, sessionToken);
    }

    /**
     * @see GooglePlaceRepository#getDetailsRestaurant
     */
    public void getRestaurantDetails(String placeId, GooglePlaceRepository.OnCompleteListener onCompleteListener) {
        mGooglePlaceRepository.getDetailsRestaurant(placeId, onCompleteListener);
    }

    /**
     * @return a LiveData holding a {@link Location} data of the current device's location.
     */
    public LiveData<Location> getDeviceLocationLiveData() {
        return mDeviceLocation;
    }

    /**
     * Sets-up {@link Location} data held by the LiveData.
     *
     * @param location the current device's location.
     */
    public void setDeviceLocationLiveData(Location location) {
        mDeviceLocation.setValue(location);
    }

    /**
     * @return a LiveData holding a {@link Boolean} data of the current device's location settings.
     */
    public LiveData<Boolean> getLocationActivatedLiveData() {
        return mIsLocationActivated;
    }

    /**
     * Sets-up {@link Boolean} data held by the LiveData.
     *
     * @param b the current device's location settings status (true if activated and false otherwise).
     */
    public void setLocationActivatedLiveData(boolean b) {
        mIsLocationActivated.setValue(b);
    }

    /**
     * @see UserRepository#createOrUpdateUser
     */
    public Task<Void> createOrUpdateUser(User user) {
        return mUserRepository.createOrUpdateUser(user);
    }

    /**
     * @see UserRepository#updateUserRestaurantChoice
     */
    public Task<Void> updateUserRestaurantChoice(User user) {
        return mUserRepository.updateUserRestaurantChoice(user);
    }

    /**
     * @see UserRepository#updateUserName
     */
    public Task<Void> updateUserName(String uid, String name) {
        return mUserRepository.updateUserName(uid, name);
    }

    /**
     * @see UserRepository#updateUserPicture
     */
    public Task<Void> updateUserPicture(String uid, String urlPicture) {
        return mUserRepository.updateUserPicture(uid, urlPicture);
    }

    /**
     * @see UserRepository#updateUserLikedRestaurant
     */
    public Task<Void> updateUserLikedRestaurant(User user) {
        return mUserRepository.updateUserLikedRestaurant(user);
    }

    /**
     * @see UserRepository#getUser
     */
    public Task<DocumentSnapshot> getUser(String uid) {
        return mUserRepository.getUser(uid);
    }

    /**
     * @see UserRepository#getUsersQuery
     */
    public Query getUsersQuery() {
        return mUserRepository.getUsersQuery();
    }

    /**
     * @see UserRepository#loadWorkmatesInRestaurants
     */
    public Query loadWorkmatesInRestaurants(String placeId) {
        return mUserRepository.loadWorkmatesInRestaurants(placeId);
    }
}
