package de.danoeh.antennapod.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.danoeh.antennapod.R;

//This fragment is the fragment under "FEATURE" tab on the home page
public class FeaturedFragment extends Fragment{

    public static final String TAG = "FeaturedFragment";
    private View root;

    public FeaturedFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            root = inflater.inflate(R.layout.featued_fragment, container, false);


        }catch (Exception e){
            e.printStackTrace();
        }
        return root;
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