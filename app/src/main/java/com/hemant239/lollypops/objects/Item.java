package com.hemant239.lollypops.objects;

import java.io.Serializable;
import java.util.Objects;

public class Item implements Serializable {

    String  id,
            name,
            createdBy,
            createdOn,
            completedOn,
            description,
            imageUri,
            timeAdded,
            timeCompleted;

    public Item() {
        id = "";
        name = "";
        createdBy = "";
        createdOn = "";
        completedOn = "";
        description = "";
        imageUri = "";
        timeAdded = "";
        timeCompleted = "";
    }


    public Item(String id) {
        this.id = id;
    }



    public Item(String id, String name, String createdBy, String createdOn, String completedOn, String description, String imageUri, String timeAdded, String timeCompleted) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.completedOn = completedOn;
        this.description = description;
        this.imageUri = imageUri;
        this.timeAdded = timeAdded;
        this.timeCompleted = timeCompleted;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public String getTimeCompleted() {
        return timeCompleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setTimeCompleted(String timeCompleted) {
        this.timeCompleted = timeCompleted;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return getId().equals(item.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
