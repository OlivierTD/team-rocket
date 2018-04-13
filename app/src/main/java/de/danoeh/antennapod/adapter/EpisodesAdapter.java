package de.danoeh.antennapod.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.Iconify;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.storage.DownloadRequester;
import de.danoeh.antennapod.core.util.Converter;
import de.danoeh.antennapod.core.util.DateUtils;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.core.util.LongList;
import de.danoeh.antennapod.core.util.NetworkUtils;

/**
 * Created by olitr on 2018-03-28.
 */

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.ViewHolder> {

    private final static String TAG = "EpisodesAdapter";

    private WeakReference<MainActivity> mainActivity;
    private List<FeedItem> episodesList;

    private final ActionButtonUtils actionButtonUtils;
    private final ActionButtonCallback actionButtonCallback;

    private final int playingBackGroundColor;
    private final int normalBackGroundColor;

    private ItemAccess itemAccess;

    private FeedItem selectedItem;

    public EpisodesAdapter(MainActivity mainActivity, List<FeedItem> episodesList, ItemAccess itemAccess) {
        this.mainActivity = new WeakReference<>(mainActivity);
        this.episodesList = episodesList;
        this.itemAccess = itemAccess;
        this.actionButtonUtils = new ActionButtonUtils(mainActivity);
        this.actionButtonCallback = new DefaultActionButtonCallback(mainActivity);
        if(UserPreferences.getTheme() == R.style.Theme_AntennaPod_Dark) {
            playingBackGroundColor = ContextCompat.getColor(mainActivity, R.color.highlight_dark);
        } else {
            playingBackGroundColor = ContextCompat.getColor(mainActivity, R.color.highlight_light);
        }
        normalBackGroundColor = ContextCompat.getColor(mainActivity, android.R.color.transparent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        FeedItem feedItem = episodesList.get(pos);
        holder.bind(feedItem);
        holder.itemView.setOnLongClickListener(v -> {
            selectedItem = feedItem;
            return false;
        });
    }

    @Nullable
    public FeedItem getSelectedItem() {
        return selectedItem;
    }

    @Override
    public long getItemId(int position) {
        FeedItem feedItem = episodesList.get(position);
        return feedItem != null ? feedItem.getId() : RecyclerView.NO_POSITION;
    }

    public int getItemCount() {
        return episodesList.size();
    }

    /**
     * Inner class that will deal with view generation for the items in the list
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout container;
        private TextView title;
        private TextView placeholder;
        private TextView pubDate;
        private TextView progressLeft;
        private TextView progressRight;
        private ImageView cover;
        private ImageButton butSecondary;
        private ProgressBar progressBar;
        private FeedItem feedItem;

        public ViewHolder(View v) {
            super(v);
            container = (FrameLayout) v.findViewById(R.id.container);
            title = (TextView) v.findViewById(R.id.txtvTitle);
            placeholder = (TextView) v.findViewById(R.id.txtvPlaceholder);
            pubDate = (TextView) v.findViewById(R.id.txtvPubDate);
            progressLeft = (TextView) v.findViewById(R.id.txtvProgressLeft);
            progressRight = (TextView) v.findViewById(R.id.txtvProgressRight);
            cover = (ImageView) v.findViewById(R.id.imgvCover);
            butSecondary = (ImageButton) v.findViewById(R.id.butSecondaryAction);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            v.setTag(this);
        }

        /**
         * Function that binds data to every view in the list
         * @param item FeedItem object that will be used for data to bind to the view
         */
        public void bind(FeedItem item) {
            this.feedItem = item;
            FeedMedia media = feedItem.getMedia();

            this.generalBind();

            if (media != null) {
                final boolean isDownloadingMedia = DownloadRequester.getInstance().isDownloadingFile(media);
                FeedItem.State state = item.getState();
                if (isDownloadingMedia) {
                   downloadingBind(media);
                }
                else if (state == FeedItem.State.PLAYING || state == FeedItem.State.IN_PROGRESS) {
                    playingBind(media);
                }
                else {
                    noActionBind(media);
                }
                if (media.isCurrentlyPlaying()) {
                    container.setBackgroundColor(playingBackGroundColor);
                } else {
                    container.setBackgroundColor(normalBackGroundColor);
                }
            }
        }

        /**
         * Changes the date to the proper format for the view
         * @param date string of the date to be formatted
         * @return formatted date
         */
        private String dateFormat(String date) {
            int index = 0;
            if (countMatches(date, ' ') == 1 || countMatches(date, ' ') == 2) {
                index = date.lastIndexOf(' ');
            } else if (countMatches(date, '.') == 2) {
                index = date.lastIndexOf('.');
            } else if (countMatches(date, '-') == 2) {
                index = date.lastIndexOf('-');
            } else if (countMatches(date, '/') == 2) {
                index = date.lastIndexOf('/');
            }
            if (index > 0) {
                date = date.substring(0, index + 1).trim() + "\n" + date.substring(index + 1);
            }
            return date;
        }

        /**
         * Binds to the view that always occurs
         */
        private void generalBind() {
            String pubDateStr = DateUtils.formatAbbrev(mainActivity.get(), feedItem.getPubDate());
            placeholder.setText(feedItem.getFeed().getTitle());
            title.setText(feedItem.getTitle());
            pubDate.setText(dateFormat(pubDateStr));
            actionButtonUtils.configureActionButton(butSecondary, feedItem, true);
            butSecondary.setFocusable(false);
            butSecondary.setTag(feedItem);
            butSecondary.setOnClickListener(secondaryActionListener);

            Glide.with(mainActivity.get())
                    .load(feedItem.getImageLocation())
                    .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                    .fitCenter()
                    .dontAnimate()
                    .into(new CoverTarget(feedItem.getFeed().getImageLocation(), placeholder, cover, mainActivity.get()));
        }

        /**
         * Binds data when its downloading
         * @param media provides data to bind
         */
        private void downloadingBind(FeedMedia media) {
            progressLeft.setText(Converter.byteToString(itemAccess.getItemDownloadedBytes(this.feedItem)));
            if(itemAccess.getItemDownloadSize(this.feedItem) > 0) {
                progressRight.setText(Converter.byteToString(itemAccess.getItemDownloadSize(this.feedItem)));
            } else {
                progressRight.setText(Converter.byteToString(media.getSize()));
            }
            progressBar.setProgress(itemAccess.getItemDownloadProgressPercent(this.feedItem));
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Binds data when an episode is playing
         * @param media provides data to bind
         */
        private void playingBind(FeedMedia media) {
            if (media.getDuration() > 0) {
                int progress = (int) (100.0 * media.getPosition() / media.getDuration());
                progressBar.setProgress(progress);
                progressBar.setVisibility(View.VISIBLE);
                progressLeft.setText(Converter.getDurationStringLong(media.getPosition()));
                progressRight.setText(Converter.getDurationStringLong(media.getDuration()));
            }
        }

        /**
         * Binds data when no action occurs. In example, when we load the views for the first time
         * @param media provides data to bind
         */
        private void noActionBind(FeedMedia media) {
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

    private View.OnClickListener secondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FeedItem feedItem = (FeedItem) v.getTag();
            actionButtonCallback.onActionButtonPressed(feedItem, LongList.of(FeedItemUtil.getIds(episodesList)));
        }
    };

    /**
     * Used by date format
     * @param str the string to be counted on
     * @param ch the character to verify the amounts
     * @return the total count of the characters ch found in the string str
     */
    private static int countMatches(final CharSequence str, final char ch) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (ch == str.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    public interface ItemAccess {
        long getItemDownloadedBytes(FeedItem item);
        long getItemDownloadSize(FeedItem item);
        int getItemDownloadProgressPercent(FeedItem item);
    }

}
