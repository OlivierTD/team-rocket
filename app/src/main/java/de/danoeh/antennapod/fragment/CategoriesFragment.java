package de.danoeh.antennapod.fragment;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;

public class CategoriesFragment extends Fragment {


    public static final String TAG = "CategoriesFragment";
    private ArrayAdapter listAdapter;
    private ListView listView;
    private ArrayList<String>categories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list, container, false);

        //getting the current activity
        final MainActivity activity = (MainActivity) getActivity();

        listView = (ListView) view.findViewById(R.id.cat_listview);


        //creation and initialization of the arrayadapter used
        listAdapter = new ArrayAdapter(getActivity(),R.layout.category_list,R.id.categories_name, getCategories());

        //setting the adapter
        listView.setAdapter(listAdapter);

        //defining the behavior of the app when a certain item in the list is selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                //creating the fragment
                CategorySearchFragment cs = new CategorySearchFragment();
                //setting the new fragment's attribute
                cs.setId(categories.get(i));
                activity.loadChildFragment(cs);
            }
        });
        return view;
    }

    //getting the categories from the ressource file
    public ArrayList<String> getCategories (){
        Resources res = getResources();
        String [] cat = res.getStringArray(R.array.categories_names);
        categories =  new ArrayList<>(Arrays.asList(cat));
        return categories;
    }


}
