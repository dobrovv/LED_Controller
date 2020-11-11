package de.simon_dankelmann.apps.ledcontroller;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EffectsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EffectsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EffectsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SettingsManager settings = new SettingsManager();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static MainActivity mMainActivity;
    static SoundMeter sm = new SoundMeter();

    static volatile Boolean mVisualSoundEffectPaused = true;
    private static Thread soundMeterThread = new Thread() {
        @Override
        public void run() {
            SettingsManager set = new SettingsManager();
            int LEDS_NUM = set.getInt("PREF_PIXELS_NUM", 12);
            int ledCnt = LEDS_NUM/2;
            double amp = 0;
            double curMax = 1;
            double MAX_AMP = 32767;
            while(true) {
                if (mVisualSoundEffectPaused) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            while(!mVisualSoundEffectPaused) {
                if(mMainActivity == null || mMainActivity.getBlunoService() == null) continue;
                //mMainActivity.getBlunoService().clearCommandQuery();
            try {
                amp = sm.getAmplitude();
                curMax = Math.max(curMax, amp);
                int leds = (int)( Math.sqrt(amp)/Math.sqrt(curMax) * (ledCnt-2) ) + 2;
                Log.w(TAG, "Amp: " + amp + " Leds: " + leds + " Max: " + (int)(curMax) + " Delay: " + (20 * Math.abs(leds)+100));
                curMax = Math.max(curMax * 0.99, 1.0);
                mMainActivity.getBlunoService().clearPixels();
                for (int i = 0; i < leds; i++) { // For each pixel in strip...
                    int pixelHue = (int)( (360.0 - i * 360.0 / (ledCnt-1)) / 3.5  / (0.9*amp/curMax) );
                    //int pixelHue = (int)( (360.0 - i * 360.0 / (ledCnt-1)) / (5 - 3 * (amp/curMax)) );
                    float[] hsv = {(float) pixelHue, 1.0f, (float)Math.min(Math.max(amp/curMax, 0.7), 1.0)};
                    mMainActivity.getBlunoService().memSetPixelColor(ledCnt-i-1, Color.HSVToColor(hsv));
                    mMainActivity.getBlunoService().memSetPixelColor(ledCnt+i, Color.HSVToColor(hsv));
                }
                mMainActivity.getBlunoService().showPixels();


                    Thread.sleep(20*Math.abs(leds)+100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }}
    };



    private OnFragmentInteractionListener mListener;

    public EffectsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EffectsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EffectsFragment newInstance(String param1, String param2) {
        EffectsFragment fragment = new EffectsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mMainActivity = ((MainActivity)getActivity());

    }

    @Override
    public void onPause() {
        sm.stop();
        mVisualSoundEffectPaused = true;
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_effects, container, false);

        // COP EFFECT
        Switch effect_switch_cops = (Switch) view.findViewById(R.id.switch_effect_cops);
        effect_switch_cops.setChecked(!mVisualSoundEffectPaused);
        effect_switch_cops.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                synchronized (mVisualSoundEffectPaused) {
                    if (isChecked) {
                        sm.start();
                        mVisualSoundEffectPaused = false;
                        if(!soundMeterThread.isAlive())
                            soundMeterThread.start();
                    } else {
                        //sm.stop();
                        mVisualSoundEffectPaused = true;
                    }
                }
            }
        });
        // Inflate the layout for this fragment
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
