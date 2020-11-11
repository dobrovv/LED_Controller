package de.simon_dankelmann.apps.ledcontroller;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterPresets extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] maintitle;
    private final String[] subtitle;
    private final RadioButton[] rbtns;

    public ListAdapterPresets(Activity context, String[] maintitle,String[] subtitle) {
        super(context, R.layout.listitem_presets, maintitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.maintitle=maintitle;
        this.subtitle=subtitle;
        this.rbtns = new RadioButton[maintitle.length];


    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listitem_presets, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);
        RadioButton rbtn = (RadioButton) rowView.findViewById(R.id.rbtnPreset);

        titleText.setText(maintitle[position]);
        subtitleText.setText(subtitle[position]);
        rbtns[position] = rbtn;

        return rowView;
    };

    public void uncheckRadioBtns() {
        for(RadioButton rbtn : rbtns) {
            try {
                rbtn.setChecked(false);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setRadioBtnChecked(int position, boolean checked) {
        try{
            rbtns[position].setChecked(checked);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRbtnChecked(int position) {
        try{
            return rbtns[position].isChecked();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}