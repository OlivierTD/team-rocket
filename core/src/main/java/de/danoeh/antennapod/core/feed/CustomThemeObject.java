package de.danoeh.antennapod.core.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anania on 3/27/2018.
 */

public class CustomThemeObject implements Serializable {
    private List<FeedItem> theme;
    private String name;

    public CustomThemeObject(String name) {
        this.name = name;
        this.theme = new ArrayList<>();
    }

    public CustomThemeObject(List<FeedItem> q) {
        this.theme = q;
    }

    public void setCustomThemeObject(List<FeedItem> q) {
        this.theme = q;
    }

    public List<FeedItem> getCustomThemeObject() {
        return this.theme;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

