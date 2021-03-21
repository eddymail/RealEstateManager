package com.openclassrooms.realestatemanager.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.openclassrooms.realestatemanager.Injection.Injection;
import com.openclassrooms.realestatemanager.Injection.ViewModelFactory;
import com.openclassrooms.realestatemanager.Model.House;
import com.openclassrooms.realestatemanager.R;
import com.openclassrooms.realestatemanager.Ui.HouseViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient client;
    private HouseViewModel houseViewModel;
    private List<House> houseList = new ArrayList<>();
    private House house;
    private LatLng houseLatLng;
    private LatLng currentPosition;
    private static final long HOUSE_ID = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialize view
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        //Async map
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(MapViewFragment.this);
        }
        //Initialize location client
        client = LocationServices.getFusedLocationProviderClient(getActivity());

        this.configureViewModel();
        this.getAllHousesFromDatabase();
        this.checkCondition();

        return view;
    }

    private void checkCondition() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        //Initialize location manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //When location service is enabled
            //Get Last Location
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize location
                    Location location = task.getResult();
                    //Check condition
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        currentPosition = new LatLng(lat, lng);

                        setMarker();
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void setMarker() {

        for (House house : houseList) {
            String address = house.getAddress();

            Geocoder coder = new Geocoder(getContext());
            List<Address> addresses;
            try {
                addresses = coder.getFromLocationName(address, 10);

                if (addresses == null) {
                }
                Log.e("Test", "addresse  = " + addresses);
                Address location = addresses.get(0);
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                houseLatLng = new LatLng(lat, lng);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(houseLatLng);
                if (houseLatLng != null) {
                    this.googleMap.addMarker(markerOptions);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentPosition != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 12));
        } else if (houseLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(houseLatLng, 12));
        }
    }

    private void getAllHousesFromDatabase() {
        this.houseViewModel.getAll().observe(this, this::updateList);
    }

    private void updateList(List<House> houses) {
        houseList = new ArrayList<>();
        houseList.addAll(houses);

        Log.e("Test", "houseList = " + houseList.size());
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(getContext());
        this.houseViewModel = ViewModelProviders.of(this, viewModelFactory).get(HouseViewModel.class);
        this.houseViewModel.init(HOUSE_ID);

    }
}