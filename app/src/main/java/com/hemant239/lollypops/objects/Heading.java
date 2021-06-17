package com.hemant239.lollypops.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Heading implements Serializable {

    String  id,
            name,
            creator,
            date,
            timestamp,
            description,
            imageUri;


    public Heading() {
        imageUri="";
    }

    public Heading(String id) {
        this.id = id;
    }

    public Heading(String id, String name, String creator, String date,String timestamp,String description, String imageUri) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.date = date;
        this.timestamp = timestamp;
        this.description = description;
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Heading)) return false;
        Heading heading = (Heading) o;
        return getId().equals(heading.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
