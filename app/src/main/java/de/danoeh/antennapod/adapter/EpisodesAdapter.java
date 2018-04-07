package de.danoeh.antennapod.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.Iconify;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.core.util.Converter;
import de.danoeh.antennapod.core.util.DateUtils;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.core.util.LongList;
import de.danoeh.antennapod.core.util.NetworkUtils;

/**
 * Created by olitr on 2018-03-28.
 */

public class EpisodesAdapter extends BaseAdapter {

    private final static String TAG = "EpisodesAdapter";

    private Context context;
    private List<FeedItem> episodesList;
    private static LayoutInflater inflater = null;

    private final ActionButtonUtils actionButtonUtils;
    private final ActionButtonCallback actionButtonCallback;

    public EpisodesAdapter(Context context, List<FeedItem> episodesList) {
        this.context = context;
        this.episodesList = episodesList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.actionButtonUtils = new ActionButtonUtils(context);
        this.actionButtonCallback = new DefaultActionButtonCallback(context);
    }

    @Override
    public int getCount() {
        return episodesList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Method that describes the translation between the data item and the View to display
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if existing view to reuse, otherwise inflate view
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.episodes_list, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.txtvTitle);
        TextView placeholder = (TextView) convertView.findViewById(R.id.txtvPlaceholder);
        TextView pubDate = (TextView) convertView.findViewById(R.id.txtvPubDate);
        TextView progressLeft = (TextView) convertView.findViewById(R.id.txtvProgressLeft);
        TextView progressRight = (TextView) convertView.findViewById(R.id.txtvProgressRight);
        //ImageView dragHandle = (ImageView) convertView.findViewById(R.id.drag_handle);
        ImageView cover = (ImageView) convertView.findViewById(R.id.imgvCover);
        ImageButton butSecondary = (ImageButton) convertView.findViewById(R.id.butSecondaryAction);
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

        FeedItem feedItem = episodesList.get(position);
        FeedMedia media = feedItem.getMedia();

        title.setText(feedItem.getTitle());
        placeholder.setText(feedItem.getFeed().getTitle());
        pubDate.setText(DateUtils.formatAbbrev(context, feedItem.getPubDate()));

        if (media != null) {
            FeedItem.State state = feedItem.getState();
            if (state == FeedItem.State.PLAYING || state == FeedItem.State.IN_PROGRESS) {
                if (media.getDuration() > 0) {
                    int progress = (int) (100.0 * media.getPosition() / media.getDuration());
                    progressBar.setProgress(progress);
                    progressBar.setVisibility(View.VISIBLE);
                    progressLeft.setText(Converter.getDurationStringLong(media.getPosition()));
                    progressRight.setText(Converter.getDurationStringLong(media.getDuration()));
                } else {
                    if (media.getSize() > 0) {
                        progressLeft.setText(Converter.byteToString(media.getSize()));
                    } else if (NetworkUtils.isDownloadAllowed() && !media.checkedOnSizeButUnknown()) {
                        progressLeft.setText("{fa-spinner}");
                        Iconify.addIcons(progressLeft);
                        NetworkUtils.getFeedMediaSizeObservable(media)
                                .subscribe(
                                        size -> {
                                            if (size > 0) {
                                                progressLeft.setText(Converter.byteToString(size));
                                            } else {
                                                progressLeft.setText("");
                                            }
                                        }, error -> {
                                                progressLeft.setText("");
                                                Log.e(TAG, Log.getStackTraceString(error));
                                        });
                    } else {
                        progressLeft.setText("");
                    }
                    progressRight.setText(Converter.getDurationStringLong(media.getDuration()));
                    progressBar.setVisibility(View.GONE);
                }
            }


            Glide.with(context)
                    .load(feedItem.getImageLocation())
                    .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                    .fitCenter()
                    .dontAnimate()
                    .into(new CoverTarget(feedItem.getFeed().getImageLocation(), placeholder, cover, (MainActivity) context));

        }

        actionButtonUtils.configureActionButton(butSecondary, feedItem, true);
        butSecondary.setFocusable(false);
        butSecondary.setTag(feedItem);
        butSecondary.setOnClickListener(secondaryActionListener);

        return convertView;
    }

    private View.OnClickListener secondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FeedItem feedItem = (FeedItem) v.getTag();
            actionButtonCallback.onActionButtonPressed(feedItem, LongList.of(FeedItemUtil.getIds(episodesList)));
        }
    };

}
