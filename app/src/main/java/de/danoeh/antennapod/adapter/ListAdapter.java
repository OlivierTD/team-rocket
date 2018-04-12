package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.fragment.CategorySearchFragment;


/**
 * Created by Sai Shan on 2018-04-10.
 */

public class ListAdapter extends ArrayAdapter<String> {


    private final static String TAG = "EpisodesAdapter";

    private Context context;
    private List<String> catNames;
    private static LayoutInflater inflater = null;


    public ListAdapter(Context context, List<String> categories) {
        super(context, 0, categories);
        this.context = context;
        this.catNames = categories;

    }

    @Override
    public int getCount() {
        return catNames.size();
    }


    // Method that describes the translation between the data item and the View to display
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if existing view to reuse, otherwise inflate view
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category_list, null);
        }

        TextView category = (TextView) convertView.findViewById(R.id.text1);
        ImageView cover = (ImageView) convertView.findViewById(R.id.image1);

        category.setText(catNames.get(position));
        category.setVisibility(View.VISIBLE);

        category.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View view) {
                CategorySearchFragment cs = new CategorySearchFragment();
                cs.setId(catNames.get(position));
                ((MainActivity) context).loadChildFragment(cs);
            }
        });
        return convertView;
    }


}
