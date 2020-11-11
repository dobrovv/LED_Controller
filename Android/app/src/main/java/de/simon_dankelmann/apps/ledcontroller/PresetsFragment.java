package de.simon_dankelmann.apps.ledcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PresetsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PresetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresetsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MainActivity mMainActivity;
    ListView listViewPresets;
    ListAdapterPresets adapter;

    static int selectedPreset = -1;

    private OnFragmentInteractionListener mListener;

    public PresetsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PresetsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PresetsFragment newInstance(String param1, String param2) {
        PresetsFragment fragment = new PresetsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_presets, container, false);

        String[] maintitle ={
                "Color Wipe",
                "Theater Chase",
                "Rainbow",
                "Theater Chase Rainbow",

                "Color Loop",
                "Fade In Fade Out",
                "Strobe",
                "Halloween Eyes",
                "Cylon Bounce",
                "NewKITT",
                "Twinkle",
                "Twinkle Random",
                "Sparkle",
                "Snow Sparkle",
                "Running Lights",
                "Fire",
                "Bouncing Colored Balls",
                "Meteor Rain",
        };

        String[] subtitle ={
                "Fill along the length of the strip in various colors",
                "Do a theater marquee effect in various colors",
                "Flowing rainbow cycle along the whole strip",
                "Rainbow-enhanced theaterChase variant",
                "Color Loop",
                "Fade In Fade Out",
                "Strobe",
                "Halloween Eyes",
                "Cylon Bounce",
                "NewKITT",
                "Twinkle",
                "Twinkle Random",
                "Sparkle",
                "Snow Sparkle",
                "Running Lights",
                "Fire",
                "Bouncing Colored Balls",
                "Meteor Rain",
        };

        adapter = new ListAdapterPresets(mMainActivity, maintitle, subtitle);
        listViewPresets = (ListView)(view.findViewById(R.id.listViewPresets));
        listViewPresets.setAdapter(adapter);
        if (selectedPreset > 0)
            adapter.setRadioBtnChecked(selectedPreset, true);


        listViewPresets.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Log.i("mytag", "Preset item clicked" + position);
                if (!adapter.isRbtnChecked(position)) {
                    selectedPreset = position;
                    adapter.uncheckRadioBtns();
                    adapter.setRadioBtnChecked(position, true);
                    mMainActivity.getBlunoService().setPresetMode(position);
                } else  {
                    selectedPreset = -1;
                    adapter.uncheckRadioBtns();
                    mMainActivity.getBlunoService().setPresetMode(999);
                }
            }
        });

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
