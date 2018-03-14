package de.danoeh.antennapod.core.feed;

import java.util.List;

/**
 * Created by James on 2018-03-14.
 */

//temporary queue object that needs to be built upon

public class Queue {
    private List<FeedItem> queue;

    public void Queue(){}

    public Queue(List<FeedItem> q){
        this.queue = q;
    }

    public void setQueue(List<FeedItem> q){
        this.queue = q;
    }

    public List<FeedItem> getQueue(){
        return this.queue;
    }

}
