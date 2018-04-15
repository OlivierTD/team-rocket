package de.danoeh.antennapod.fragment;

/**
 * Created by Anania on 3/26/2018.
 */

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

        for(int i = 0; i < NUMBER_OF_COLORS; i++){
            colorButtons[i].setOnClickListener(this);
        }
        setColors.setOnClickListener(this);

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
