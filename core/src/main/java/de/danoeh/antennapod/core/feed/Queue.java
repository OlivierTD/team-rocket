package de.danoeh.antennapod.core.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018-03-14.
 */

public class Queue implements Serializable {
    private List<Long> episodeIDList = new ArrayList<Long>();
    private String name;

    public Queue(String name) {
        this.name = name;
    }

    public Queue(ArrayList<Long> episodeList) {
        this.episodeIDList = episodeList;
    }

    public void setEpisodesIDList(List<Long> episodeIDList) {
        this.episodeIDList = episodeIDList;
    }

    public List<Long> getEpisodesIDList() {
        return this.episodeIDList;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
