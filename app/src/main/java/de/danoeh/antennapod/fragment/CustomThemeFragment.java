package de.danoeh.antennapod.fragment;

/**
 * Created by Anania on 3/26/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.dialog.HSVColorPickerDialog;

public class CustomThemeFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "CustomThemeFragment";
    //private Button firstColor, secondColor, thirdColor, setColors;
    //private Integer color1, color2, color3;
    private Button[] colorButtons;
    private Integer[] colors;
    private Button setColors;
    private Button saveColors;
    private Button resetColors;
    private static int NUMBER_OF_COLORS = 3;

    //Mandatory Constructor
    public CustomThemeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);



        //cpd.setNoColorButton( R.string.no_color );

        View v = inflater.inflate(R.layout.custom_theme_fragment, container, false);
        Button firstColor = (Button) v.findViewById(R.id.custom_theme_color_1);
        Button secondColor = (Button) v.findViewById(R.id.custom_theme_color_2);
        Button thirdColor = (Button) v.findViewById(R.id.custom_theme_color_3);
        colorButtons = new Button[]{firstColor, secondColor, thirdColor};
        colors = new Integer[NUMBER_OF_COLORS];

        setColors = (Button) v.findViewById(R.id.custom_theme_set);
        saveColors = (Button) v.findViewById(R.id.custom_theme_save);
        resetColors = (Button) v.findViewById(R.id.custom_theme_reset);

        for(int i = 0; i < NUMBER_OF_COLORS; i++){
            colorButtons[i].setOnClickListener(this);
        }
        setColors.setOnClickListener(this);
        saveColors.setOnClickListener(this);
        resetColors.setOnClickListener(this);
        return v;
    }

    private void getColorFromUser(int i){
        HSVColorPickerDialog cpd1 = new HSVColorPickerDialog(getActivity(), 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                //if a color was selected
                if(color != null){
                    // Do something with the selected color
                    colorButtons[i].setBackgroundColor(color);
                    colors[i] = color;
                }
            }
        });
        cpd1.setTitle("Pick your color " + (i+1) + "!");
        cpd1.show();
    }

    private void setThemeColor(Integer i) {
        if(colors[i] == null){
            return;
        }

        switch(i){
            case 0:
                //background
                ((MainActivity) getActivity()).setActivityBackgroundColor(colors[i]);
                break;
            case 1:
                //nav bar
                View navList = ((MainActivity) getActivity()).findViewById(R.id.nav_list);
                navList.setBackgroundColor(colors[i]);
                break;
            case 2:
                //nav layout
                View navTitle = ((MainActivity) getActivity()).findViewById(R.id.nav_layout);
                navTitle.setBackgroundColor(colors[i]);
                //nav settings
                View navSettings = ((MainActivity) getActivity()).findViewById(R.id.nav_settings);
                navSettings.setBackgroundColor(colors[i]);
                //action bar
                //Code would work in API 11
                String hexColor = String.format("#%06X", (0xFFFFFF & colors[i]));
                ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(hexColor));
                ActionBar bar = ((MainActivity) getActivity()).getSupportActionBar();
                bar.setBackgroundDrawable(colorDrawable);
                break;
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_theme_color_1:
                getColorFromUser(0);

                break;
            case R.id.custom_theme_color_2:
                getColorFromUser(1);
                break;
            case R.id.custom_theme_color_3:
                getColorFromUser(2);
                break;
            case R.id.custom_theme_set:
                for(int i = 0; i < NUMBER_OF_COLORS; i++){
                    setThemeColor(i);
                }
                break;
            case R.id.custom_theme_reset:
                //get saved color scheme if there is
                SharedPreferences sharedPref =((MainActivity) getActivity()).getPreferences(Context.MODE_PRIVATE);


                //int color1 = getResources().getInteger(R.integer.saved_high_score_default_key);
                int color1 = sharedPref.getInt("color1", 1);
                int color2 = sharedPref.getInt("color2", 1);
                int color3 = sharedPref.getInt("color3", 1);

                if (color1==1 || color1==0) {


                } else {
                    ((MainActivity) getActivity()).setActivityBackgroundColor(color1);
                }

                if (color2==1 || color2==0) {


                } else {

                    View navList = ((MainActivity) getActivity()).findViewById(R.id.nav_list);
                    navList.setBackgroundColor((Integer) color2);

                }

                if(color3==1 || color3==0) {


                } else {
                    View navTitle = ((MainActivity) getActivity()).findViewById(R.id.nav_layout);
                    navTitle.setBackgroundColor(color3);
                    View navSettings = ((MainActivity) getActivity()).findViewById(R.id.nav_settings);
                    navSettings.setBackgroundColor(color3);

                    String hexColor = String.format("#%06X", (0xFFFFFF &color3));
                    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(hexColor));
                    ActionBar bar =((MainActivity) getActivity()).getSupportActionBar();
                    bar.setBackgroundDrawable(colorDrawable);
                }

                break;
            case R.id.custom_theme_save:
                sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (colors[0]==null){
                    editor.putInt("color1", 0);
                    editor.commit();
                } else {
                    editor.putInt("color1", colors[0]);
                    editor.commit();
                }
                if (colors[1]==null){
                    editor.putInt("color2", 0);
                    editor.commit();
                } else {
                    editor.putInt("color2", colors[1]);
                    editor.commit();
                }
                if (colors[2]==null){
                    editor.putInt("color3", 0);
                    editor.commit();
                } else {
                    editor.putInt("color3", colors[2]);
                    editor.commit();
                }
                break;
        }
    }












    @Override
    public void onStart(){
        super.onStart();
    }


    @Override
    public void onDestroyView(){
        super.onDestroyView();

    }


}
