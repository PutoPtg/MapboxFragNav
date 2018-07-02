package mma.example.com.mapboxfragnav;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

import java.util.List;

/**
 * An example full-screen activity with a mapbox map
 * 
 */
public class Main extends AppCompatActivity implements PermissionsListener {

    private MapboxMapFragment mapfrag; //fragment instance declaration

    private final String MAP_FRAG_TAG = "mapboxfrag";
    private PermissionsManager permissionsManager;
    private final String T = "SHEEEP!";
    private Button FlyToSource;
    private Button Navigate;
    private int navigate_var;
    private MapboxMapFragment mapFragment;

    /**
     * Main method called when the activity is creates
     *
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigate_var = 0;

        Mapbox.getInstance(this, getString(R.string.mapbox_key));

        // Create MapboxMapFragment


        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            if (savedInstanceState == null) {

                // Create fragment
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                LatLng coimbra = new LatLng(40.192756, -8.4143277);

                // Build mapboxMap
                MapboxMapOptions options = new MapboxMapOptions();
                options.styleUrl(Style.MAPBOX_STREETS);
                options.camera(new CameraPosition.Builder()
                        .target(coimbra)
                        .zoom(15)
                        .build());

                // Create map fragment
                mapFragment = MapboxMapFragment.newInstance(options);

                // Add map fragment to parent container
                transaction.add(R.id.frag_placeholder, mapFragment, MAP_FRAG_TAG);
                transaction.commit();

                FlyToSource = findViewById(R.id.fly_to_source);
                Navigate = findViewById(R.id.navigate_button);

                FlyToSource.setOnClickListener(view -> {
                    mapFragment.flyButton();
                });

            } else {
                mapFragment = (MapboxMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG);
            }

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Called when the activity is ready
     *
     * @param savedInstanceState Receives a bundle
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Handles the permissions for the user
     *
     * @param requestCode   code for the authorization
     * @param permissions   string with the permissions
     * @param grantResults  results of the permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    /**
     * Message for the user about the permissions
     *
     * @param permissionsToExplain  List
     */
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

        //TODO create a toast or something to the user.

    }

    /**
     * Results of the permissions, recreates the activity
     * @param granted
     */
    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            recreate();
        }
    }

    /**
     * Handles the long click
     *
     */
    public void longClick(){
        if (navigate_var == 0){
            Navigate.setVisibility(View.VISIBLE);


            Navigate.setOnClickListener(v ->{
                mapFragment.navigateButton();
            });

            navigate_var = 1;
        }else{
            Navigate.setVisibility(View.GONE);
            navigate_var = 0;
        }
        Log.i(T,"Good Communication");
    }

}
