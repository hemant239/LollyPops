package com.hemant239.lollypops.objects;

import java.io.Serializable;

public class User implements Serializable {

    String  userId;
    String name;
    String phoneNumber;
    String status;
    String profileImageUri;

    public User(String userId, String name, String phoneNumber, String status, String profileImageUri) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.profileImageUri = profileImageUri;
    }

    public User() {

    }


    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }
}
