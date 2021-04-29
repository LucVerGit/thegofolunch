package com.example.thegoforlunch.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.example.thegoforlunch.service.GetNearbyPlaces;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.thegoforlunch.R;
import com.example.thegoforlunch.databinding.FragmentMapViewBinding;
import com.example.thegoforlunch.model.Restaurant;
import com.example.thegoforlunch.util.IntentUtils;
import com.example.thegoforlunch.util.LocationUtils;
import com.example.thegoforlunch.util.PermissionsUtils;
import com.example.thegoforlunch.ui.activity.RestaurantDetailsActivity;
import com.example.thegoforlunch.viewmodel.AppViewModel;

import static com.example.thegoforlunch.util.AppConstants.DEFAULT_INTERVAL;
import static com.example.thegoforlunch.util.AppConstants.FASTEST_INTERVAL;
import static com.example.thegoforlunch.util.AppConstants.RC_CHECK_LOCATION_SETTINGS;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    // private static
    private static final String TAG = MapViewFragment.class.getSimpleName();
    public static final float DEFAULT_ZOOM = 14.5f;
    public static final LatLng CENTER_FRANCE = new LatLng(46.3432097, 2.5733245);
    public static final float INIT_ZOOM = 5f;


    // public static
    public static MapViewFragment newInstance() {
        Log.d(TAG, "newInstance");

        MapViewFragment fragment = new MapViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    // variables
    private FragmentMapViewBinding mBinding;
    private Context mContext;
    private SupportMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mDeviceLocation;
    private AppViewModel mViewModel;
    private Map<Marker, Restaurant> mRestaurants;
    private List<ListenerRegistration> mListenerRegistrations;
    private AbstractMap.SimpleEntry<Marker, Restaurant> mAutocompleteSelection;


    // inherited methods
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        init(inflater);
        buildFab();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        mViewModel = ViewModelProviders.of(requireActivity()).get(AppViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");

        mContext = null;
        for (ListenerRegistration registration : mListenerRegistrations) {
            registration.remove();
        }
        mListenerRegistrations.clear();
    }

    /**
     * Called when map is ready.
     * Is triggered when calling SupportMapFragment.getMapAsync(OnMapReadyCallback onMapReadyCallback)
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        mGoogleMap = googleMap;
        initObservers(); // here so mGoogleMap is initialized
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                CENTER_FRANCE,
                INIT_ZOOM));
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (mDeviceLocation != null) {
            if (mAutocompleteSelection != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mAutocompleteSelection.getValue().getLatitude(), mAutocompleteSelection.getValue().getLongitude()),
                        DEFAULT_ZOOM));
            } else {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()),
                        DEFAULT_ZOOM));
            }
            checkLocationPermission(mContext);
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");

        if (mAutocompleteSelection != null && mAutocompleteSelection.getKey().getTag() == marker.getTag()) {
            Log.d(TAG, "onMarkerClick: Autocomplete");
            Intent intent = IntentUtils.loadRestaurantDataIntoIntent(
                    mContext, RestaurantDetailsActivity.class, mAutocompleteSelection.getValue().getPlaceId());
            startActivity(intent);
            return true;
        }

        for (Marker key : mRestaurants.keySet()) {
            if (key.getTag() == marker.getTag()) {
                Restaurant restaurant = mRestaurants.get(key);
                Intent intent = IntentUtils.loadRestaurantDataIntoIntent(
                        mContext, RestaurantDetailsActivity.class, restaurant.getPlaceId());
                startActivity(intent);
                return true;
            }
        }

        return true;
    }


    // methods
    private void init(LayoutInflater inflater) {
        Log.d(TAG, "init");

        mBinding = FragmentMapViewBinding.inflate(inflater);
        mMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.cell_workmates_fragment_container);
        mRestaurants = new HashMap<>();
        mListenerRegistrations = new ArrayList<>();
    }

    private void buildFab() {
        mBinding.mapViewFab.setOnClickListener(v -> {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationUtils.checkLocationSettings(
                        (AppCompatActivity) mContext,
                        DEFAULT_INTERVAL,
                        FASTEST_INTERVAL,
                        RC_CHECK_LOCATION_SETTINGS);
            }
            if (mDeviceLocation != null) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()),
                        DEFAULT_ZOOM));
            }
        });
    }

    private void initObservers() {
        Log.d(TAG, "initObservers");

        // Nearby Search observer
        mViewModel.getNearbyRestaurantsLiveData().observe(getViewLifecycleOwner(), restaurants -> {
            Log.d(TAG, "getNearbyRestaurantsLiveData: onChanged");

            for (ListenerRegistration registration : mListenerRegistrations) {
                registration.remove();
            }
            mListenerRegistrations.clear();
            mGoogleMap.clear();
            mRestaurants.clear();
            for (Restaurant restaurant : restaurants) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(restaurant.getLatitude(),
                                restaurant.getLongitude()));
                Marker marker = mGoogleMap.addMarker(markerOptions);
                marker.setTag(restaurant.getPlaceId());

                ListenerRegistration registration =
                        mViewModel.loadWorkmatesInRestaurants(restaurant.getPlaceId())
                                .addSnapshotListener((snapshot, e) -> {
                                    if (snapshot != null && e == null) {
                                        Log.d(TAG, "loadWorkmatesInRestaurants: added EventListener to : " + restaurant.getName());
                                        if (snapshot.size() != 0) {
                                            marker.setIcon(getBitmapDescriptor(mContext, R.drawable.ic_restaurant_marker_cyan));
                                        } else {
                                            marker.setIcon(getBitmapDescriptor(mContext, R.drawable.ic_restaurant_marker_orange));
                                        }
                                    }
                                });
                mListenerRegistrations.add(registration);
                mRestaurants.put(marker, restaurant);
            }
        });

        // Current device's location observer
        mViewModel.getDeviceLocationLiveData().observe(getViewLifecycleOwner(), location -> {
            Log.d(TAG, "getDeviceLocationLiveData: onChanged");

            if (mDeviceLocation == null) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()),
                        DEFAULT_ZOOM));
            }
            mDeviceLocation = location;
        });

        // Location setting's observer
        mViewModel.getLocationActivatedLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            Log.d(TAG, "getLocationActivatedLiveData: onChanged");

            if (mGoogleMap != null && mAutocompleteSelection == null) {
                checkLocationPermission(mContext);
                mGoogleMap.setMyLocationEnabled(aBoolean);
                for (Marker marker : mRestaurants.keySet()) {
                    marker.setVisible(aBoolean);
                }
            } else if (mGoogleMap != null) {
                closeKeyboard();
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mAutocompleteSelection.getValue().getLatitude(), mAutocompleteSelection.getValue().getLongitude()),
                        DEFAULT_ZOOM));
            }
        });

        // Restaurant details observer
        mViewModel.getDetailsRestaurantLiveData().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                for (Marker marker : mRestaurants.keySet()) {
                    marker.setVisible(false);
                }

                MarkerOptions markerOptions = new MarkerOptions()
                        .icon(getBitmapDescriptor(mContext, R.drawable.ic_restaurant_marker_yellow))
                        .position(new LatLng(restaurant.getLatitude(),
                                restaurant.getLongitude()));
                Marker marker = mGoogleMap.addMarker(markerOptions);
                marker.setTag(restaurant.getPlaceId());
                if (mAutocompleteSelection != null) {
                    mAutocompleteSelection.getKey().remove();
                }
                mAutocompleteSelection = new AbstractMap.SimpleEntry<>(marker, restaurant);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(restaurant.getLatitude(), restaurant.getLongitude()),
                        DEFAULT_ZOOM));
            } else {
                for (Marker marker : mRestaurants.keySet()) {
                    marker.setVisible(true);
                }
                if (mAutocompleteSelection != null) {
                    mAutocompleteSelection.getKey().setVisible(false);
                    mAutocompleteSelection.getKey().remove();
                }
                mAutocompleteSelection = null;
            }
        });
    }

    private void checkLocationPermission(Context context) {
        Log.d(TAG, "checkPermissions");

        if (!PermissionsUtils.isLocationPermissionGranted(context)) {
            requireActivity().finish();
        }
    }

    private BitmapDescriptor getBitmapDescriptor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VectorDrawable vectorDrawable = (VectorDrawable) ContextCompat.getDrawable(context, id);
            if (vectorDrawable != null) {
                int h = vectorDrawable.getIntrinsicHeight();
                int w = vectorDrawable.getIntrinsicWidth();

                vectorDrawable.setBounds(0, 0, w, h);

                Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bm);
                vectorDrawable.draw(canvas);

                return BitmapDescriptorFactory.fromBitmap(bm);
            } else {
                return null;
            }
        } else {
            return BitmapDescriptorFactory.fromResource(id);
        }
    }

    private void closeKeyboard() {
        Log.d(TAG, "closeKeyboard");

        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
