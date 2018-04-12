package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.Arrays;
import java.util.List;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;

/**
 * Created by Sai Shan on 2018-04-10.
 */

public class CategoriesFragment extends ListFragment{


    public static final String TAG = "CategoriesFragment";
    private ListAdapter listAdapter;
    private ListView list;
    private String []categories = { "Arts", "Comedy", "Education", "Kids & family", "Health", "TV & Film", "Music", "News & Politics", "Religion & Spirituality" ,"Science & Medicine", "Sports", "Technology", "Business", "Games & Hobbies", "Society & Culture", "Government & Organizations" };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list, container, false);

        final MainActivity activity = (MainActivity) getActivity();
        List<String> catList = Arrays.asList(categories);

        list = (ListView) view.findViewById(R.id.list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),R.layout.category_list, catList);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CategorySearchFragment cs = new CategorySearchFragment();
                cs.setId(categories[i]);
                activity.loadChildFragment(cs);
            }
        });

        return view;
    }

}
