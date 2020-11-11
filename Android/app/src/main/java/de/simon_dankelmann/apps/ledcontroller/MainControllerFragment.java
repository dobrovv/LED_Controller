package de.simon_dankelmann.apps.ledcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainControllerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainControllerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainControllerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SettingsManager settings = new SettingsManager();
    private ColorPicker picker;
    private OpacityBar opacityBar;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MainActivity mMainActivity;

    private OnFragmentInteractionListener mListener;

    public MainControllerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainControllerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainControllerFragment newInstance(String param1, String param2) {
        MainControllerFragment fragment = new MainControllerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainActivity = ((MainActivity)getActivity());

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        requireActivity().registerReceiver(mGattUpdateReceiver, MainActivity.makeGattUpdateIntentFilter());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_controller, container, false);

        int prefBrightness = settings.getInt("PREF_BRIGHTNESS", 50);

        final Switch onOffSwitch = (Switch)view.findViewById(R.id.onOffSwitchMainController);


        // CONNECT COLORPICKER AND OPACITYBAR
        picker = (ColorPicker) view.findViewById(R.id.picker);
        picker.setShowOldCenterColor(false);
        opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
        opacityBar.setColor(picker.getColor());
        opacityBar.setOpacity(prefBrightness);
        //picker.addOpacityBar(opacityBar);
        //SET COLORPICKER LISTENER


        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                onOffSwitch.setChecked(true);
                mMainActivity.getBlunoService().clearCommandQuery();
                mMainActivity.getBlunoService().fillColor(color);
                opacityBar.setColor(color);

            }
        });

        opacityBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                onOffSwitch.setChecked(true);
                if (opacity > 100) {
                    opacityBar.setOpacity(100);
                } else {
                    mMainActivity.getBlunoService().setBrightness(opacity);
                }
            }
        });


        if (mMainActivity != null && mMainActivity.getBlunoService() != null ) {
            onOffSwitch.setChecked(mMainActivity.getBlunoService().getConnectionState() == BlunoBLE.STATE_CONNECTED);
            if(mMainActivity.getBlunoService().getConnectionState() == BlunoBLE.STATE_CONNECTED) {
                onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#2ECC71"), PorterDuff.Mode.MULTIPLY);
                onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#2ECC71"), PorterDuff.Mode.MULTIPLY);
            } else {
                onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
                onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
            }
        }  else {
            onOffSwitch.setChecked(false);
            onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
            onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
        }

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(mMainActivity.getBlunoService().getConnectionState() != BlunoBLE.STATE_CONNECTED)
                        mMainActivity.connectToBluno();
                    //ledController.changeColor(picker.getColor());

                } else {
                    mMainActivity.disconnectBluno();
                    //ledController.switchOff();
                }
            }
        });

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_main_controller, container, false);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BlunoBLE.ACTION_DEVICE_FOUND.equals(action)) {
                //Toast.makeText(getApplicationContext(), "Controller Found", Toast.LENGTH_SHORT).show();
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BlunoBLE.ACTION_DEVICE_NOT_FOUND.equals(action)) {
                final Switch onOffSwitch = requireActivity().findViewById(R.id.onOffSwitchMainController);
                onOffSwitch.setChecked(false);
                onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
                onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
            } else if (BlunoBLE.ACTION_GATT_CONNECTED.equals(action)) {
                final Switch onOffSwitch = requireActivity().findViewById(R.id.onOffSwitchMainController);
                onOffSwitch.setChecked(true);
                onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#2ECC71"), PorterDuff.Mode.MULTIPLY);
                onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#2ECC71"), PorterDuff.Mode.MULTIPLY);

            } else if (BlunoBLE.ACTION_GATT_DISCONNECTED.equals(action)) {
                final Switch onOffSwitch = requireActivity().findViewById(R.id.onOffSwitchMainController);
                onOffSwitch.setChecked(false);
                onOffSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
                onOffSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.MULTIPLY);
                //Toast.makeText(getApplicationContext(), "Controller Disconnected", Toast.LENGTH_SHORT).show();
                //final Switch onOffSwitch = (Switch)findViewById(R.id.onOffSwitchMainController);
                //onOffSwitch.setChecked(false);
                //mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BlunoBLE.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());


            } else if (BlunoBLE.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BlunoBLE.EXTRA_DATA));
                //String data = intent.getStringExtra(BlunoBLE.EXTRA_DATA);
                //tv.setText(data+'\n'+tv.getText());
            }
        }
    };


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
