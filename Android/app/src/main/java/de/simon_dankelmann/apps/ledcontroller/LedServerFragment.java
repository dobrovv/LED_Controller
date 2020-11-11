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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LedServerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LedServerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LedServerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static MainActivity mMainActivity;
    private static LedServerFragment mThis;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText cmdInput;
    private TextView execTime;
    private Button btnRun;
    private Switch loopSwitch;


    private static volatile Boolean cmdThreadActive = false; // specifies if the ServerFragmanet is currently active, if so the thread is activated on the Run button press, otherwise the server thread sleeps.
    private static volatile Boolean runOnce = false; // specifies if the thread should loop on the commands list instead of exiting

    // A new thread to run user specified commands from the Android app
    // each cmd is a <cmd>#<delay> string that is send to the bluno board 
    // the cmd command is sent using the ble serial write, and then the thread sleeps for the specified cmd.delay, ms
    
    private static Thread cmdThread = new Thread() {
        @Override
        public void run() {
            int cmdIndex = 0;
            while(true) {
                if (!cmdThreadActive) {
                    cmdIndex = 0;
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while(cmdThreadActive) {
                    if(mMainActivity == null || mMainActivity.getBlunoService() == null || cmds == null) continue;
                    try {
                        if (cmdIndex < cmds.size()) {
                            if (!cmdThreadActive) break;
                            CmdClass cmd = cmds.get(cmdIndex++);
                            Log.e("tag", "Sending cmd:" + cmd.cmd + " delay: "+ cmd.delay);
                            mMainActivity.getBlunoService().serialWrite(
                                    cmd.cmd.getBytes("utf-8"),
                                    cmd.delay);
                            Thread.sleep(cmd.delay);
                        }
                        if (runOnce && cmdIndex >= cmds.size()) {
                            cmdThreadActive = false;
                            mThis.btnRun.setText("Run");
                        }
                        if (cmdIndex >= cmds.size())
                            cmdIndex = 0;
                    } catch (Exception e) {
                        cmdIndex = 0;
                        cmds = new ArrayList<CmdClass>();
                        e.printStackTrace();
                    }
                }
            }
        }
    };



    private OnFragmentInteractionListener mListener;

    public LedServerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LedServerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LedServerFragment newInstance(String param1, String param2) {
        LedServerFragment fragment = new LedServerFragment();
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
        mThis = this;
        if(!cmdThread.isAlive())
            cmdThread.start();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_led_server, container, false);
        cmdInput = (EditText)view.findViewById(R.id.cmdInput);
        btnRun = (Button)view.findViewById(R.id.btnRun);
        loopSwitch = (Switch)view.findViewById(R.id.loopSwitch);

        btnRun.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cmdThreadActive) {
                    try {
                        mMainActivity.getBlunoService().clearCommandQuery();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    btnRun.setText("Stop");
                    cmds = parseCmdInput();
                    runOnce = !loopSwitch.isChecked();
                    cmdThreadActive = true;
                } else {
                    btnRun.setText("Run");
                    cmdThreadActive = false;
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

    private ArrayList<CmdClass> parseCmdInput(){
        ArrayList<CmdClass> cmds = new ArrayList<CmdClass>();
        String raw = cmdInput.getText().toString();
        String[] lines = raw.split("\n");

        for (String line : lines) {
            String[] tokens = line.split("#");
            if(tokens[0].startsWith("\n")) continue;
            CmdClass cmd = new CmdClass();
            cmd.cmd = tokens[0] + "\n";
            int delay;
            try {
                Log.e("Tag", "parseCmdInput(): " + line + " cmd: " + tokens[0] + " delay: " +  tokens[1]);
                delay = Integer.parseInt(tokens[1]);
                if (delay <= 20) {
                    delay = 20;
                }
            } catch (Exception e) {
                delay = 250;
            }

            cmd.delay = delay;
            cmds.add(cmd);
        }

        return cmds;

    }

    private class CmdClass {
        public String cmd;
        public int delay;
    }

    private static ArrayList<CmdClass> cmds;
}
