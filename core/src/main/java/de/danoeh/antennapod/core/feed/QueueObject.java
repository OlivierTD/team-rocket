package de.danoeh.antennapod.core.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018-03-14.
 */

//temporary queue object that needs to be built upon

public class QueueObject implements Serializable{
    private List<FeedItem> queue;

    public QueueObject(){
        this.queue = new ArrayList<>();
    }

    public QueueObject(List<FeedItem> q){
        this.queue = q;
    }

    public void setQueueObject(List<FeedItem> q){
        this.queue = q;
    }

    public List<FeedItem> getQueueObject(){
        return this.queue;
    }

}
