package de.danoeh.antennapod.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import java.util.ArrayList;
import java.util.Arrays;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;

public class CategoriesFragment extends Fragment {


    public static final String TAG = "CategoriesFragment";
    private ArrayAdapter listAdapter;
    private GridView gridView;
    private ArrayList<String>categories = new ArrayList<>(Arrays.asList("Arts", "Comedy", "Education", "Kids & family", "Health", "TV & Film", "Music", "News & Politics", "Religion & Spirituality" ,"Science & Medicine", "Sports", "Technology", "Business", "Games & Hobbies", "Society & Culture", "Government & Organizations" ));
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list, container, false);
        Log.d("lol", "in the creation of the view ");
        final MainActivity activity = (MainActivity) getActivity();
        Log.d("lol", "Main activity stuff");

        Log.d("lol", "going to gridview stuff");
        gridView = (GridView) view.findViewById(R.id.listgridview);
        Log.d("lol", "end gridview stuff");

        listAdapter = new ArrayAdapter(getActivity(),R.layout.category_list,R.id.text1, categories);
        Log.d("lol", "does it go here ");

        gridView.setAdapter(listAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                CategorySearchFragment cs = new CategorySearchFragment();
                cs.setId(categories.get(i));
                activity.loadChildFragment(cs);
            }
        });

        return view;
    }

}
