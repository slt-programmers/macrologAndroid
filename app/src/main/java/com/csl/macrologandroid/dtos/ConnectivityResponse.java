package com.csl.macrologandroid.dtos;

//import com.google.gson.annotations.Expose;
//import com.google.gson.annotations.SerializedName;

public class ConnectivityResponse {

//    @Expose
//    @SerializedName("image")
    private String imageUrl;

//    @Expose
//    @SerializedName("name")
    private String accountName;

    private long syncedApplicationId;

    private long syncedAccountId;

    private long numberActivitiesSynced;

    public ConnectivityResponse(String imageUrl, String accountName, long syncedApplicationId, long syncedAccountId, long numberActivitiesSynced) {
        this.imageUrl = imageUrl;
        this.accountName = accountName;
        this.syncedApplicationId = syncedApplicationId;
        this.syncedAccountId = syncedAccountId;
        this.numberActivitiesSynced = numberActivitiesSynced;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getSyncedApplicationId() {
        return syncedApplicationId;
    }

    public void setSyncedApplicationId(long syncedApplicationId) {
        this.syncedApplicationId = syncedApplicationId;
    }

    public long getSyncedAccountId() {
        return syncedAccountId;
    }

    public void setSyncedAccountId(long syncedAccountId) {
        this.syncedAccountId = syncedAccountId;
    }

    public long getNumberActivitiesSynced() {
        return numberActivitiesSynced;
    }

    public void setNumberActivitiesSynced(long numberActivitiesSynced) {
        this.numberActivitiesSynced = numberActivitiesSynced;
    }
}
