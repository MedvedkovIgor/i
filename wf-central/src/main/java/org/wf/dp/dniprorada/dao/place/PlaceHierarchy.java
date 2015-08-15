package org.wf.dp.dniprorada.dao.place;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wf.dp.dniprorada.model.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dgroup
 * @since  22.07.2015
 */
public class PlaceHierarchy {
    @JsonProperty
    private Long levelOfArea;

    @JsonProperty
    private Long level;

    @JsonProperty
    private Place place;

    @JsonProperty
    private List<PlaceHierarchy> children = new ArrayList<>();


    public Long getLevelOfArea() {
        return levelOfArea;
    }
    public void setLevelOfArea(Long levelOfArea) {
        this.levelOfArea = levelOfArea;
    }

    public Long getLevel() {
        return level;
    }
    public void setLevel(Long level) {
        this.level = level;
    }

    public Place getPlace() {
        return place;
    }
    public void setPlace(Place place) {
        this.place = place;
    }

    public List<PlaceHierarchy> getChildren() {
        return children;
    }
    public void setChildren(List<PlaceHierarchy> children) {
        this.children = children;
    }

    public void addChild(PlaceHierarchy childNode) {
        if (childNode == null)
            return;
        children.add(childNode);
    }

    @Override
    public String toString() {
        return "PlaceHierarchy{" +
                "levelOfArea=" + levelOfArea +
                ", level=" + level +
                ", place=" + place +
                ", children=" + children +
                '}';
    }
}