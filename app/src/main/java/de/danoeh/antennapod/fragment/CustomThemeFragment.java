package de.danoeh.antennapod.fragment;

/**
 * Created by Anania on 3/26/2018.
 */

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.dialog.HSVColorPickerDialog;
import de.danoeh.antennapod.menuhandler.NavDrawerActivity;

public class CustomThemeFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "CustomThemeFragment";
    private Button firstColor, secondColor, thirdColor, setColors;
    private Integer color1, color2, color3;

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
        firstColor = (Button) v.findViewById(R.id.custom_theme_color_1);
        secondColor = (Button) v.findViewById(R.id.custom_theme_color_2);
        thirdColor = (Button) v.findViewById(R.id.custom_theme_color_3);
        setColors = (Button) v.findViewById(R.id.custom_theme_set);

        firstColor.setOnClickListener(this);
        secondColor.setOnClickListener(this);
        thirdColor.setOnClickListener(this);
        setColors.setOnClickListener(this);



        return v;
    }



    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_theme_color_1:
                HSVColorPickerDialog cpd1 = new HSVColorPickerDialog(getActivity(), 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void colorSelected(Integer color) {
                        //if a color was selected
                        if(color != null){
                            // Do something with the selected color
                            firstColor.setBackgroundColor(color);
                            color1 = color;
                        }
                    }
                });
                cpd1.setTitle("Pick your first color!");
                cpd1.show();


                break;
            case R.id.custom_theme_color_2:
                HSVColorPickerDialog cpd2 = new HSVColorPickerDialog(getActivity(), 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void colorSelected(Integer color) {
                        //if a color was selected
                        if(color != null){
                            // Do something with the selected color
                            secondColor.setBackgroundColor(color);
                            color2 = color;
                        }
                    }
                });
                cpd2.setTitle("Pick your second color!");
                cpd2.show();


                break;
            case R.id.custom_theme_color_3:
                HSVColorPickerDialog cpd3 = new HSVColorPickerDialog(getActivity(), 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void colorSelected(Integer color) {
                        //if a color was selected
                        if(color != null){
                            // Do something with the selected color
                            thirdColor.setBackgroundColor(color);
                            color3 = color;
                        }
                    }
                });
                cpd3.setTitle("Pick your third color!");
                cpd3.show();


                break;
            case R.id.custom_theme_set:

                if(color1 != null ) {
                    //background
                    ((MainActivity) getActivity()).setActivityBackgroundColor(color1);


                }
                if(color2 != null ) {
                    //nav bar
                    View navList = ((MainActivity) getActivity()).findViewById(R.id.nav_list);
                    navList.setBackgroundColor(color2);

                }
                if(color3 != null ) {

                    //nav layout
                    View navTitle = ((MainActivity) getActivity()).findViewById(R.id.nav_layout);
                    navTitle.setBackgroundColor(color3);
                    //nav settings
                    View navSettings = ((MainActivity) getActivity()).findViewById(R.id.nav_settings);
                    navSettings.setBackgroundColor(color3);
                    //action bar
                    //Code would work in API 11
                    //ColorDrawable colorDrawable = new ColorDrawable(color3));
                    //((MainActivity) getActivity()).getActionBar().setBackgroundDrawable(colorDrawable);



                }


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
