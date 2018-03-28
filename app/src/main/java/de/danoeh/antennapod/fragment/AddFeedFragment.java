package de.danoeh.antennapod.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.activity.OpmlImportFromPathActivity;
import de.danoeh.antennapod.fragment.gpodnet.GpodnetMainFragment;

/**
 * Provides actions for adding new podcast subscriptions
 */
public class AddFeedFragment extends Fragment {

    public static final String TAG = "AddFeedFragment";

    /**
     * Preset value for url text field.
     */
    public static final String ARG_FEED_URL = "feedurl";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.addfeed, container, false);

        final EditText etxtFeedurl = (EditText) root.findViewById(R.id.etxtFeedurl);

        Bundle args = getArguments();
        if (args != null && args.getString(ARG_FEED_URL) != null) {
            etxtFeedurl.setText(args.getString(ARG_FEED_URL));
        }

        Button butSearchITunes = (Button) root.findViewById(R.id.butSearchItunes);
        Button butBrowserGpoddernet = (Button) root.findViewById(R.id.butBrowseGpoddernet);
        Button butSearchFyyd = (Button) root.findViewById(R.id.butSearchFyyd);
        Button butCategorySearch = (Button) root.findViewById(R.id.butCategorySearch); //search button
        Button butOpmlImport = (Button) root.findViewById(R.id.butOpmlImport);
        Button butConfirm = (Button) root.findViewById(R.id.butConfirm);

        final MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.add_feed_label);

        butSearchITunes.setOnClickListener(v -> activity.loadChildFragment(new ItunesSearchFragment()));

        butBrowserGpoddernet.setOnClickListener(v -> activity.loadChildFragment(new GpodnetMainFragment()));

        butSearchFyyd.setOnClickListener(v -> activity.loadChildFragment(new FyydSearchFragment()));

        butOpmlImport.setOnClickListener(v -> startActivity(new Intent(getActivity(),
                OpmlImportFromPathActivity.class)));

        butConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, etxtFeedurl.getText().toString());
            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, getString(R.string.add_feed_label));
            startActivity(intent);
        });

        butCategorySearch.setOnClickListener(v -> alertSimpleListView());

        return root;
    }


    public void alertSimpleListView() {

        //array holding the categories
        final CharSequence[] categories = { "Arts", "Comedy", "Education", "Kids and family", "Health", "TV and Film", "Music", "News and Politics", "Religion and Spirituality" ,"Science and Medicine", "Sports", "Technology", "Business", "Games and Hobbies", "Society and Culture", "Government and Organizations" };
        //array holding the categories IDs to search for in the itunes API
        final int[] categoriesNum = {1301, 1303, 1304, 1305, 1307, 1309, 1310, 1311, 1314, 1315, 1316, 1318, 1321, 1323, 1324, 1325};

        //creating the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        //setting its title
        builder.setTitle("Select the category of your choice");
        //setting the categories in the dialog
        builder.setItems(categories, new DialogInterface.OnClickListener() {

            //handle of when clicking on the desired category
            public void onClick(DialogInterface dialog, int item) {
                Log.d("idk","is it going here");

                CategorySearchFragment cat = new CategorySearchFragment();
                cat.setId(categoriesNum[item]);

            }
        }).show();
    }

    public void createSearchForm(int id){
        CategorySearchFragment cat = new CategorySearchFragment();
        cat.setId(id);
    }
}
