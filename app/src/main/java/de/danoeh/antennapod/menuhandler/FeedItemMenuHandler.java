package de.danoeh.antennapod.menuhandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.core.gpoddernet.model.GpodnetEpisodeAction;
import de.danoeh.antennapod.core.gpoddernet.model.GpodnetEpisodeAction.Action;
import de.danoeh.antennapod.core.preferences.GpodnetPreferences;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.storage.DBTasks;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.DownloadRequestException;
import de.danoeh.antennapod.core.util.IntentUtils;
import de.danoeh.antennapod.core.util.LongList;
import de.danoeh.antennapod.core.util.ShareUtils;

/**
 * Handles interactions with the FeedItemMenu.
 */
public class FeedItemMenuHandler {

    private static final String TAG = "FeedItemMenuHandler";

    //Object where we will store the list of queues
    private static ArrayList<Queue> queueList = new ArrayList<>();

    private FeedItemMenuHandler(Context context) {
    }

    /**
     * Used by the MenuHandler to access different types of menus through one
     * interface
     */
    public interface MenuInterface {
        /**
         * Implementations of this method should call findItem(id) on their
         * menu-object and call setVisibility(visibility) on the returned
         * MenuItem object.
         */
        void setItemVisibility(int id, boolean visible);
    }

    /**
     * This method should be called in the prepare-methods of menus. It changes
     * the visibility of the menu items depending on a FeedItem's attributes.
     *
     * @param mi               An instance of MenuInterface that the method uses to change a
     *                         MenuItem's visibility
     * @param selectedItem     The FeedItem for which the menu is supposed to be prepared
     * @param showExtendedMenu True if MenuItems that let the user share information about
     *                         the FeedItem and visit its website should be set visible. This
     *                         parameter should be set to false if the menu space is limited.
     * @param queueAccess      Used for testing if the queue contains the selected item; only used for
     *                         move to top/bottom in the queue
     * @return Returns true if selectedItem is not null.
     */
    public static boolean onPrepareMenu(MenuInterface mi,
                                        FeedItem selectedItem,
                                        boolean showExtendedMenu,
                                        @Nullable LongList queueAccess) {
        if (selectedItem == null) {
            return false;
        }
        boolean hasMedia = selectedItem.getMedia() != null;
        boolean isPlaying = hasMedia && selectedItem.getState() == FeedItem.State.PLAYING;

        if (!isPlaying) {
            mi.setItemVisibility(R.id.skip_episode_item, false);
        }

        boolean isInQueue = selectedItem.isTagged(FeedItem.TAG_QUEUE);
        if (queueAccess == null || queueAccess.size() == 0 || queueAccess.get(0) == selectedItem.getId()) {
            mi.setItemVisibility(R.id.move_to_top_item, false);
        }
        if (queueAccess == null || queueAccess.size() == 0 || queueAccess.get(queueAccess.size() - 1) == selectedItem.getId()) {
            mi.setItemVisibility(R.id.move_to_bottom_item, false);
        }
        if (!isInQueue) {
            mi.setItemVisibility(R.id.remove_from_queue_item, false);
        }
        if (!(!isInQueue && selectedItem.getMedia() != null)) {
            mi.setItemVisibility(R.id.add_to_queue_item, false);
        }

        if (!showExtendedMenu || selectedItem.getLink() == null) {
            mi.setItemVisibility(R.id.visit_website_item, false);
            mi.setItemVisibility(R.id.share_link_item, false);
            mi.setItemVisibility(R.id.share_link_with_position_item, false);
        }
        if (!showExtendedMenu || !hasMedia || selectedItem.getMedia().getDownload_url() == null) {
            mi.setItemVisibility(R.id.share_download_url_item, false);
            mi.setItemVisibility(R.id.share_download_url_with_position_item, false);
        }
        if (!hasMedia || selectedItem.getMedia().getPosition() <= 0) {
            mi.setItemVisibility(R.id.share_link_with_position_item, false);
            mi.setItemVisibility(R.id.share_download_url_with_position_item, false);
        }

        mi.setItemVisibility(R.id.share_file, hasMedia && selectedItem.getMedia().fileExists());

        if (selectedItem.isPlayed()) {
            mi.setItemVisibility(R.id.mark_read_item, false);
        } else {
            mi.setItemVisibility(R.id.mark_unread_item, false);
        }

        if (selectedItem.getMedia() == null || selectedItem.getMedia().getPosition() == 0) {
            mi.setItemVisibility(R.id.reset_position, false);
        }

        if (!UserPreferences.isEnableAutodownload()) {
            mi.setItemVisibility(R.id.activate_auto_download, false);
            mi.setItemVisibility(R.id.deactivate_auto_download, false);
        } else if (selectedItem.getAutoDownload()) {
            mi.setItemVisibility(R.id.activate_auto_download, false);
        } else {
            mi.setItemVisibility(R.id.deactivate_auto_download, false);
        }

        if (selectedItem.getPaymentLink() == null || !selectedItem.getFlattrStatus().flattrable()) {
            mi.setItemVisibility(R.id.support_item, false);
        }

        boolean isFavorite = selectedItem.isTagged(FeedItem.TAG_FAVORITE);
        mi.setItemVisibility(R.id.add_to_favorites_item, !isFavorite);
        mi.setItemVisibility(R.id.remove_from_favorites_item, isFavorite);

        return true;
    }

    /**
     * The same method as onPrepareMenu(MenuInterface, FeedItem, boolean, QueueAccess), but lets the
     * caller also specify a list of menu items that should not be shown.
     *
     * @param excludeIds Menu item that should be excluded
     * @return true if selectedItem is not null.
     */
    public static boolean onPrepareMenu(MenuInterface mi,
                                        FeedItem selectedItem,
                                        boolean showExtendedMenu,
                                        LongList queueAccess,
                                        int... excludeIds) {
        boolean rc = onPrepareMenu(mi, selectedItem, showExtendedMenu, queueAccess);
        if (rc && excludeIds != null) {
            for (int id : excludeIds) {
                mi.setItemVisibility(id, false);
            }
        }
        return rc;
    }

    public static boolean onMenuItemClicked(Context context, int menuItemId,
                                            FeedItem selectedItem) throws DownloadRequestException {
        switch (menuItemId) {
            case R.id.skip_episode_item:
                context.sendBroadcast(new Intent(PlaybackService.ACTION_SKIP_CURRENT_EPISODE));
                break;
            case R.id.remove_item:
                DBWriter.deleteFeedMediaOfItem(context, selectedItem.getMedia().getId());
                break;
            case R.id.mark_read_item:
                selectedItem.setPlayed(true);
                DBWriter.markItemPlayed(selectedItem, FeedItem.PLAYED, false);
                if (GpodnetPreferences.loggedIn()) {
                    FeedMedia media = selectedItem.getMedia();
                    // not all items have media, Gpodder only cares about those that do
                    if (media != null) {
                        GpodnetEpisodeAction actionPlay = new GpodnetEpisodeAction.Builder(selectedItem, Action.PLAY)
                                .currentDeviceId()
                                .currentTimestamp()
                                .started(media.getDuration() / 1000)
                                .position(media.getDuration() / 1000)
                                .total(media.getDuration() / 1000)
                                .build();
                        GpodnetPreferences.enqueueEpisodeAction(actionPlay);
                    }
                }
                break;
            case R.id.mark_unread_item:
                selectedItem.setPlayed(false);
                DBWriter.markItemPlayed(selectedItem, FeedItem.UNPLAYED, false);
                if (GpodnetPreferences.loggedIn() && selectedItem.getMedia() != null) {
                    GpodnetEpisodeAction actionNew = new GpodnetEpisodeAction.Builder(selectedItem, Action.NEW)
                            .currentDeviceId()
                            .currentTimestamp()
                            .build();
                    GpodnetPreferences.enqueueEpisodeAction(actionNew);
                }
                break;
            case R.id.add_to_queue_item:
                SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPreferences.getString("queue list", null);
                Type type = new TypeToken<ArrayList<Queue>>() {
                }.getType();
                queueList = gson.fromJson(json, type);

                if (queueList == null) {
                    queueList = new ArrayList<>();
                }

                //Creating an alert dialog to allow users to choose a queue
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setIcon(R.drawable.ic_launcher);
                alertBuilder.setTitle("Select a queue");

                //Build the array of names for the single choice
                ArrayList<String> names = new ArrayList<String>();
                for (Queue queue : queueList) {
                    names.add(queue.getName());
                }

                //Set the single choice items in our alert dialog
                alertBuilder.setSingleChoiceItems(names.toArray(new String[queueList.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    }
                });

                alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        //Fetch the selected queue
                        ListView lw = ((AlertDialog) dialog).getListView();
                        int pos = lw.getCheckedItemPosition();
                        long id = selectedItem.getId();
                        // If the list is empty, set a new one
                        if (queueList.get(pos).getEpisodesIDList() == null) {
                            List<Long> episodesIDList = new ArrayList<Long>();
                            episodesIDList.add(id);
                            queueList.get(pos).setEpisodesIDList(episodesIDList);
                        } else {
                            queueList.get(pos).getEpisodesIDList().add(id);
                        }

                        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(queueList);
                        editor.putString("queue list", json);
                        editor.apply();

                        dialog.dismiss();
                    }
                });
                alertBuilder.show();
                break;
            case R.id.remove_from_queue_item:

                SharedPreferences sharedPreferences2 = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
                Gson gson2 = new Gson();
                String json2 = sharedPreferences2.getString("queue list", null);
                Type type2 = new TypeToken<ArrayList<Queue>>() {
                }.getType();
                queueList = gson2.fromJson(json2, type2);

                if (queueList == null) {
                    queueList = new ArrayList<>();
                }
                long id = selectedItem.getId();
                for (Queue queue : queueList) {
                    boolean removed = queue.getEpisodesIDList().remove(id);
                    if (removed == true) {
                        break;
                    }
                }

                SharedPreferences sharedPreferences3 = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences3.edit();
                Gson gson3 = new Gson();
                String json3 = gson3.toJson(queueList);
                editor.putString("queue list", json3);
                editor.apply();
                break;
            case R.id.add_to_favorites_item:
                DBWriter.addFavoriteItem(selectedItem);
                break;
            case R.id.remove_from_favorites_item:
                DBWriter.removeFavoriteItem(selectedItem);
                break;
            case R.id.reset_position:
                selectedItem.getMedia().setPosition(0);
                DBWriter.markItemPlayed(selectedItem, FeedItem.UNPLAYED, true);
                break;
            case R.id.activate_auto_download:
                selectedItem.setAutoDownload(true);
                DBWriter.setFeedItemAutoDownload(selectedItem, true);
                break;
            case R.id.deactivate_auto_download:
                selectedItem.setAutoDownload(false);
                DBWriter.setFeedItemAutoDownload(selectedItem, false);
                break;
            case R.id.visit_website_item:
                Uri uri = Uri.parse(selectedItem.getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (IntentUtils.isCallable(context, intent)) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.download_error_malformed_url),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.support_item:
                DBTasks.flattrItemIfLoggedIn(context, selectedItem);
                break;
            case R.id.share_link_item:
                ShareUtils.shareFeedItemLink(context, selectedItem);
                break;
            case R.id.share_download_url_item:
                ShareUtils.shareFeedItemDownloadLink(context, selectedItem);
                break;
            case R.id.share_link_with_position_item:
                ShareUtils.shareFeedItemLink(context, selectedItem, true);
                break;
            case R.id.share_download_url_with_position_item:
                ShareUtils.shareFeedItemDownloadLink(context, selectedItem, true);
                break;
            case R.id.share_file:
                ShareUtils.shareFeedItemFile(context, selectedItem.getMedia());
                break;
            default:
                Log.d(TAG, "Unknown menuItemId: " + menuItemId);
                return false;
        }
        // Refresh menu state

        return true;
    }

}
