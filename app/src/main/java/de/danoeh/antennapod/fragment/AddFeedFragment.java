package de.danoeh.antennapod.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

    private final CharSequence[] categories = { "Arts", "Comedy", "Education", "Kids & family", "Health", "TV & Film", "Music", "News & Politics", "Religion & Spirituality" ,"Science & Medicine", "Sports", "Technology", "Business", "Games & Hobbies", "Society & Culture", "Government & Organizations" };

    public CharSequence[] getCategories(){
        return categories;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.addfeed, container, false);

        final EditText etxtFeedurl = (EditText) root.findViewById(R.id.etxtFeedurl);

        Bundle args = getArguments();
        if (args != null && args.getString(ARG_FEED_URL) != null) {
            etxtFeedurl.setText(args.getString(ARG_FEED_URL));
        }

        Button butBrowserGpoddernet = (Button) root.findViewById(R.id.butBrowseGpoddernet);
        Button butCategorySearch = (Button) root.findViewById(R.id.butCategorySearch); //search button
        Button butOpmlImport = (Button) root.findViewById(R.id.butOpmlImport);
        Button butConfirm = (Button) root.findViewById(R.id.butConfirm);

        final MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.add_feed_label);


        butBrowserGpoddernet.setOnClickListener(v -> activity.loadChildFragment(new GpodnetMainFragment()));


        butOpmlImport.setOnClickListener(v -> startActivity(new Intent(getActivity(),
                OpmlImportFromPathActivity.class)));

        butConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, etxtFeedurl.getText().toString());
            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, getString(R.string.add_feed_label));
            startActivity(intent);
        });

        butCategorySearch.setOnClickListener(v -> alertSimpleListView(activity));

        return root;
    }


    public void alertSimpleListView(MainActivity activity) {
        
        //creating the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        //setting its title
        builder.setTitle("Select the category of your choice");

        //setting the categories in the dialog
        builder.setSingleChoiceItems(categories, 0, new DialogInterface.OnClickListener() {

            //handle of when clicking on the desired category
            public void onClick(DialogInterface dialog, int item) {
                String category = (categories[item].toString());
                category = String.format(category).replace('&', ' ');
                category = String.format(category).replace(' ', '+');
                CategorySearchFragment cat = new CategorySearchFragment();
                cat.setId(category);
                activity.loadChildFragment(cat);
                dialog.dismiss();
            }
        });

        builder.show();
    }


}
