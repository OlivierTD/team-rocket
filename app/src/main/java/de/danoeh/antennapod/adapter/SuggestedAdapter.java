package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.fragment.AddFeedFragment;
import de.danoeh.antennapod.fragment.ItemlistFragment;
import jp.shts.android.library.TriangleLabelView;

/**
 * Created by Sai Shan on 2018-03-22.
 */

public class SuggestedAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    /** placeholder object that indicates item should be added */
    public static final Object ADD_ITEM_OBJ = new Object();

    /** the position in the view that holds the add item; 0 is the first, -1 is the last position */
    private static final int ADD_POSITION = -1;
    private static final String TAG = "SuggestedAdapter";

    private final WeakReference<MainActivity> mainActivityRef;
    private final SuggestedAdapter.ItemAccess itemAccess;

    private TextView txt;

    public SuggestedAdapter(MainActivity mainActivity, SuggestedAdapter.ItemAccess itemAccess) {
        this.mainActivityRef = new WeakReference<>(mainActivity);
        this.itemAccess = itemAccess;
    }

    private int getAddTilePosition() {
        if(ADD_POSITION < 0) {
            return ADD_POSITION + getCount();
        }
        return ADD_POSITION;
    }

    private int getAdjustedPosition(int origPosition) {
        assert(origPosition != getAddTilePosition());
        return origPosition < getAddTilePosition() ? origPosition : origPosition - 1;
    }

    @Override
    public int getCount() {
        return 1 + itemAccess.getCount();
    }

    @Override
    public Object getItem(int position) {
        if (position == getAddTilePosition()) {
            return ADD_ITEM_OBJ;
        }
        return itemAccess.getItem(getAdjustedPosition(position));
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        if (position == getAddTilePosition()) {
            return 0;
        }
        return itemAccess.getItem(getAdjustedPosition(position)).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SuggestedAdapter.Holder holder;

        if (convertView == null) {
            holder = new SuggestedAdapter.Holder();

            LayoutInflater layoutInflater =
                    (LayoutInflater) mainActivityRef.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.sug_pod, parent, false);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.theTitle);
            holder.imageView = (ImageView) convertView.findViewById(R.id.theImage);
            holder.count = (TriangleLabelView) convertView.findViewById(R.id.triangleCountView);
            txt = (TextView)convertView.findViewById(R.id.title2);
            convertView.setTag(holder);
        } else {
            holder = (SuggestedAdapter.Holder) convertView.getTag();
        }

        if (position == getAddTilePosition()) {
            holder.feedTitle.setText("");
            holder.feedTitle.setVisibility(View.INVISIBLE);

            holder.count.setPrimaryText("");
            holder.count.setVisibility(View.INVISIBLE);

            txt.setText(" ");
            txt.setVisibility(View.INVISIBLE);
            Glide.clear(holder.imageView);

            return convertView;
            //i do not want to display anything when this position is loaded
        }

        final Feed feed = (Feed) getItem(position);
        if (feed == null) return null;

        String title;
        holder.feedTitle.setText(feed.getTitle());
        title = feed.getTitle();
        Log.d("testing", "just wondering if the title is displayed properly"+title);
        txt.setText(title);
        txt.setVisibility(View.VISIBLE);
       // holder.feedTitle.setVisibility(View.VISIBLE);

        holder.count.setVisibility(View.GONE);

        Glide.with(mainActivityRef.get())
                .load(feed.getImageLocation())
                .error(R.color.light_gray)
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .fitCenter()
                .dontAnimate()
                .into(new CoverTarget(null, holder.feedTitle, holder.imageView, mainActivityRef.get()));


        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == getAddTilePosition()) {
        } else {
            Fragment fragment = ItemlistFragment.newInstance(getItemId(position));
            mainActivityRef.get().loadChildFragment(fragment);
        }
    }

    static class Holder {
        public TextView feedTitle;
        public ImageView imageView;
        public TriangleLabelView count;
    }

    public interface ItemAccess {
        int getCount();
        Feed getItem(int position);
        int getFeedCounter(long feedId);
    }
}
