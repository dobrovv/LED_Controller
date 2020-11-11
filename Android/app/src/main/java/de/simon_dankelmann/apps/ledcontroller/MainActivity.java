package de.simon_dankelmann.apps.ledcontroller;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainControllerFragment.OnFragmentInteractionListener,
        PresetsFragment.OnFragmentInteractionListener,
        EffectsFragment.OnFragmentInteractionListener,
        LedServerFragment.OnFragmentInteractionListener{

    private SettingsManager settings = new SettingsManager();

    public BluetoothAdapter mBluetoothAdapter;
    public BlunoBLE mBlunoBLE;
    public final String TAG = "mylog";

    public int mPixelCount;
    public int mBrighness;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* DISPLAY MAINCONTROLLER ON STARTUP */
        Class fragmentClass = MainControllerFragment.class;
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        /* END DISPLAY MAINCONTROLLER ON STARTUP */

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //  keep the screen turned on

        mPixelCount = settings.getInt("PREF_PIXELS_NUM", 24);
        mBrighness = settings.getInt("PREF_BRIGHTNESS", 50);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        } else {
            Intent gattServiceIntent = new Intent(this, BlunoBLE.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==0) {
            if(resultCode != 0) {
                Intent gattServiceIntent = new Intent(this, BlunoBLE.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBlunoBLE != null &&  mBlunoBLE.getConnectionState() == BlunoBLE.STATE_CONNECTED) {
            int pixelCount = settings.getInt("PREF_PIXELS_NUM", 24);
            int brighness = settings.getInt("PREF_BRIGHTNESS", 50);

            if (pixelCount != mPixelCount) {
                mPixelCount = pixelCount;
                mBlunoBLE.setLength(mPixelCount);
            }

            if(brighness != mBrighness) {
                mBrighness = brighness;
                //mBlunoBLE.setBrightness(mBrighness);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_main_controller) {
            fragmentClass = MainControllerFragment.class;
        } else if (id == R.id.nav_presets) {
            fragmentClass = PresetsFragment.class;
        } else if(id == R.id.nav_effects){
            fragmentClass = EffectsFragment.class;
        } else if (id == R.id.nav_led_servers) {
            fragmentClass = LedServerFragment.class;
        } else if (id == R.id.nav_share) {
            // SHARE INTENT
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            //share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            // Add data to the intent, the receiving app will decide
            // what to do with it.
            share.putExtra(Intent.EXTRA_SUBJECT, "LED Controller APP");
            share.putExtra(Intent.EXTRA_TEXT, "https://github.com/dobrovv/LED_Controller");
            startActivity(Intent.createChooser(share, "Share App"));

        } else if (id == R.id.nav_send) {
            // SEND MAIL INTENT
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setData(Uri.parse("mailto:vlandobrov@gmail.com"));
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vlandobrov@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "LED Controller Contact");
            intent.putExtra(Intent.EXTRA_TEXT, "Leave me a Message");
            startActivity(Intent.createChooser(intent, "Send Email"));
        } else if(id == R.id.nav_exit){
            boolean bSwitchOff = settings.getBoolean("PREF_TURNOFFONEXIT", true);
            if(bSwitchOff && mBlunoBLE != null && mBlunoBLE.getConnectionState() == BlunoBLE.STATE_CONNECTED){
                mBlunoBLE.fillColor(Color.rgb(0,0,0));
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 300);
            } else {
                finish();
            }
        }

        if(fragmentClass != null){
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void  onFragmentInteraction(Uri uri){
        //We can keep this empty
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBlunoBLE = ((BlunoBLE.LocalBinder) service).getService();
            if (!mBlunoBLE.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBlunoBLE.startBlunoScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBlunoBLE = null;
        }
    };

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BlunoBLE.ACTION_DEVICE_FOUND);
        intentFilter.addAction(BlunoBLE.ACTION_DEVICE_NOT_FOUND);
        intentFilter.addAction(BlunoBLE.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlunoBLE.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BlunoBLE.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BlunoBLE.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BlunoBLE.ACTION_DEVICE_FOUND.equals(action)) {
                Toast.makeText(getApplicationContext(), "Controller Found", Toast.LENGTH_SHORT).show();
                settings.setString("PREF_DEVICE_ADDRESS", mBlunoBLE.getDevice().getAddress());
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BlunoBLE.ACTION_DEVICE_NOT_FOUND.equals(action)) {
                Toast.makeText(getApplicationContext(), "Controller not Found", Toast.LENGTH_SHORT).show();
            } else if (BlunoBLE.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Controller Connected", Toast.LENGTH_SHORT).show();
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BlunoBLE.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Controller Disconnected", Toast.LENGTH_SHORT).show();
                //mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BlunoBLE.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBlunoBLE.setLength(mPixelCount);
                mBlunoBLE.setBrightness(mBrighness);
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());


            } else if (BlunoBLE.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BlunoBLE.EXTRA_DATA));
                //String data = intent.getStringExtra(BlunoBLE.EXTRA_DATA);
                //tv.setText(data+'\n'+tv.getText());
            }
        }
    };

    public BlunoBLE getBlunoService() {
        return mBlunoBLE;
    }

    public void fillColor(int c) {
        if(mBlunoBLE != null)
            mBlunoBLE.fillColor(c);
    }

    public void connectToBluno() {
        if(mBlunoBLE != null) {
            Toast.makeText(getApplicationContext(), "Scanning", Toast.LENGTH_SHORT).show();
            mBlunoBLE.startBlunoScan();
        } else {
            Toast.makeText(getApplicationContext(), "No Controller Service", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectBluno() {
        if(mBlunoBLE != null) {
            if(mBlunoBLE.getConnectionState() == BlunoBLE.STATE_CONNECTED)
                Toast.makeText(getApplicationContext(), "Disconnecting Controller", Toast.LENGTH_SHORT).show();
            mBlunoBLE.disconnect();
        } else {
            Toast.makeText(getApplicationContext(), "No Controller Service", Toast.LENGTH_SHORT).show();
        }
    }

}
