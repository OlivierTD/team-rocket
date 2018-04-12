package de.danoeh.antennapod.fragment;

/**
 * Created by Anania on 3/26/2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.dialog.HSVColorPickerDialog;

public class CustomThemeFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "CustomThemeFragment";
    private Button firstColor, secondColor, thirdColor;

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

        firstColor.setOnClickListener(this);
        secondColor.setOnClickListener(this);
        thirdColor.setOnClickListener(this);



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
                        }
                    }
                });
                cpd3.setTitle("Pick your third color!");
                cpd3.show();


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
