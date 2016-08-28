package com.example.borhan.cityhelp;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Success on 8/22/2016.
 */
public class placesNode implements Parcelable {
    String name;
    String place_id;
    String locatoin;
    String address;
    String rating;


    public placesNode(){
        name = null;
        place_id = null;
        locatoin = null;
        address = null;
        rating = null;
    }



    public placesNode(String m, String m1, String m2, String m3, String m4) {
        name = m;
        place_id = m1;
        locatoin = m2;
        address = m3;
        rating = m4;
    }


    protected placesNode(Parcel in) {
        name = in.readString();
        place_id = in.readString();
        locatoin = in.readString();
        address = in.readString();
        rating = in.readString();
    }

    public String getRating(){
        return rating;
    }

    public void setRating(String rating){
        this.rating = rating;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPlace_id(){
        return place_id;
    }

    public void setPlace_id(String id){
        this.place_id = id;
    }

    public String getLocatoin(){
        return locatoin;
    }

    public void setLocatoin(String loc){
        this.locatoin = loc;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(place_id);
        parcel.writeString(locatoin);
        parcel.writeString(address);
        parcel.writeString(rating);
    }


    ////////////////////////////////////////////
    public static final Creator<placesNode> CREATOR = new Creator<placesNode>() {
        @Override
        public placesNode createFromParcel(Parcel in) {
            return new placesNode(in);
        }

        @Override
        public placesNode[] newArray(int size) {
            return new placesNode[size];
        }
    };



}
