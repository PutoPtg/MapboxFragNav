package mma.example.com.mapboxfragnav;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment example based on the Support Fragment code from the mapbox library.
 */
public class MapboxMapFragment extends Fragment
        implements OnMapReadyCallback, MapboxMap.OnMapClickListener, LocationEngineListener, MapboxMap.OnMapLongClickListener {

    FragmentActivity listener;

    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private Point originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;

    private String T = "FragleRock!!";
    private View.OnLongClickListener longListener;

    /**
     * Creates a default MapFragment instance
     *
     * @return MapFragment created
     */
    public static MapboxMapFragment newInstance() {
        return new MapboxMapFragment();
    }

    /**
     * Creates a MapFragment instance
     *
     * @param mapboxMapOptions The configuration options to be used.
     * @return MapFragment created.
     */
    public static MapboxMapFragment newInstance(@Nullable MapboxMapOptions mapboxMapOptions) {
        MapboxMapFragment mapFragment = new MapboxMapFragment();
        mapFragment.setArguments(MapFragmentUtils.createFragmentArgs(mapboxMapOptions));
        return mapFragment;
    }


    /**
     * creates a listener for the fragment
     * @param context the attached activity context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    /**
     * Creates the fragment view hierarchy.
     *
     * @param inflater           Inflater used to inflate content.
     * @param container          The parent layout for the map fragment.
     * @param savedInstanceState The saved instance state for the map fragment.
     * @return The view created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = inflater.getContext();
        mapView = new MapView(context, MapFragmentUtils.resolveArgs(context, getArguments()));
        return mapView;
    }

    /**
     * Called when the fragment view hierarchy is created.
     *
     * @param view               The content view of the fragment
     * @param savedInstanceState THe saved instance state of the framgnt
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    /**
     * Called when the MapView map is ready to receive data
     *
     * @param mapboxMap The map created
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        this.map = mapboxMap;
        
        enableLocation();
        map.addOnMapClickListener(this);
        map.addOnMapLongClickListener(this);

    }

    /**
     * Enables the Fine Location.
     * Permissions must be handled in the activity before calling the fragment.
     */
    private void enableLocation(){
            initializeLocationEngine();
            initializeLocationLayer();
    }

    /**
     * Location Engine initializer.
     *
     */
    private void initializeLocationEngine(){

        try {
            locationEngine = new LocationEngineProvider(getActivity().getApplicationContext()).obtainBestLocationEngineAvailable();
            locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
            locationEngine.activate();
            Log.i(T, "LOCATION ENGINE RUNNING");
        }
        catch(Exception e){
            Log.e (T, "LOCATION ENGINE NOT RUNNING: " + e);
        }
        if(locationEngine == null)
        {
            Log.wtf (T, "LOCAL ENGINE NULL");
        }


        @SuppressLint("MissingPermission")
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }
        else{
            locationEngine.addLocationEngineListener(this);
        }
        if(locationEngine == null) {
            Log.e(T, "FAILED TO FIND A LOCATION ENGINE");
        }
    }

    /**
     * sets the camera view on the user screen.
     *
     * @param location The GPS point to center the map on.
     */
    public void setCameraPosition (Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 13.0
        ));
    }

    /**
     * Called when the fragment is visible for the users.
     */
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * Called when the fragment is ready to be interacted with.
     */
    @Override
    public void onResume() {

        super.onResume();
        mapView.onResume();

    }

    /**
     * Called when the fragment is pausing.
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Called when the fragment state needs to be saved.
     *
     * @param outState The saved state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Called when the fragment is no longer visible for the user.
     */
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * Called when the fragment receives onLowMemory call from the hosting Activity.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Called when the fragment is view hiearchy is being destroyed.
     */
    @Override
    public void onDestroyView() {

        /**
         * TODO: adequate sensor callings
         */

        super.onDestroyView();
        mapView.onDestroy();
        mapReadyCallbackList.clear();
    }


    /**
     * Called when fragment is destroyed
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationEngine != null){
            locationEngine.deactivate();
        }
        Log.i(T, "On Destroy");
        if(destinationMarker!=null){
            map.removeMarker(destinationMarker);
        }
        if(mapView != null){
            mapView.onDestroy();
        }
        if(navigationMapRoute != null){
            navigationMapRoute.removeRoute();
        }
    }


    /**
     * Called when the fragment os no longer conneted to the activity.
     *
     */
    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    /**
     * Called when the user points to something on the map.
     *
     * @param point Latitude and Longitude points.
     */
    @Override
    public void onMapClick(@NonNull LatLng point) {

        Log.i(T, "Detectou o click");
        if (destinationMarker != null) {
            Log.i(T, "Removeu Marcador Anterior");
            map.removeMarker(destinationMarker);
        }
        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Log.i(T, "Adicionou novo marcador");
        try {
            originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
            getRoute(originPosition, destinationPosition);
            Log.i(T, "Chamou o getRoute");
            //navButton.setEnabled(true);
        } catch (Exception sem_posIni) {
            Log.e(T,"NO INITIAL POSITION");
        }
    }

    /**
     * Calculates the shortest route between the user position and the point marked on the map.
     *
     * @param origin The point coordenates of the user.
     * @param destination The point marked on the map by the user.
     */
    private  void getRoute(Point origin,  Point destination){

        Log.i(T, "Dentro do GetRoute");
        NavigationRoute.builder(getActivity().getApplicationContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Log.e(T, "FAIL! NO VALID RESPONSE");
                            return;
                        }else{
                            if(response.body().routes().size() == 0){
                                Log.e(T, "NO ROUTES FOUND");
                                return;
                            }
                            try {
                                currentRoute = response.body().routes().get(0);
                            }catch(NullPointerException e){
                                Log.e(T, "BAD ROUTE, NULL POINTER EXCEPTION: " + e.getMessage() );
                            }
                            if(navigationMapRoute != null){
                                navigationMapRoute.removeRoute();
                                Log.i(T,"Old route removed");
                            }else {
                                navigationMapRoute = new NavigationMapRoute( mapView, map);
                                Log.i(T, "New route created");
                            }
                            try {
                                navigationMapRoute.addRoute(currentRoute);
                                Log.i(T, "New route added");
                            }catch(Exception e){
                                Log.e (T, "FAILED TO ADD NEW ROUTE" + e.getMessage());

                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                        Log.e(T, "ERROR: %s" + t.getMessage());
                    }
                });
    }

    /**
     * Connect function callback.
     *
     */
    @Override
    public void onConnected() {
        locationEngine.removeLocationUpdates();
    }

    /**
     * Listener to the user location.
     *
     * @param location New location data.
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            originLocation = location;
            setCameraPosition(location);
            Log.i(T, "Local change");
        }
    }

    /**
     * Location layer initializer.
     *
     */
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
    }


    /**
     * To detect the long click to show the button.
     *
     * @param point     It is obligatory but not used.
     */
    @Override
    public void onMapLongClick(@NonNull LatLng point) {

        if(currentRoute != null) {
            ((Main) getActivity()).longClick();
            Log.i(T, "Long Click");
        }
    }

    /**
     * Button to go to the user position
     *
     */
    public void flyButton(){
        if(originLocation!=null){
            setCameraPosition(originLocation);
        }else{
            CharSequence text = "À espera de GPS";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, duration);
            TextView vt = toast.getView().findViewById(android.R.id.message);
            vt.setTextColor(Color.WHITE);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * Button to start the navigation
     *
     */
    public void navigateButton(){

        NavigationLauncherOptions options = NavigationLauncherOptions.builder().directionsRoute(currentRoute).shouldSimulateRoute(true).build();
        NavigationLauncher.startNavigation(getActivity(), options);
    }


}
