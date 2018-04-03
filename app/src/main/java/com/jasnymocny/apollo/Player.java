package com.jasnymocny.apollo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class Player implements Parcelable {

    private ArrayList<String> thingsYouSaid = new ArrayList<String>();

    private boolean lost = false;
    private String name;
    private int color;
    private byte score = 0;

    public byte getScore() {
        return score;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ArrayList<String> getThingsYouSaid() {
        return thingsYouSaid;
    }

    void addThingYouSaid(String thingYouSaid) {
        this.thingsYouSaid.add(thingYouSaid);
    }

    int getColor() {
        return color;
    }

    public boolean isLost() {
        return lost;
    }

    void loose() {
        lost = true;
    }

    public Player(String name, int color){
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return getName() + " " + isLost() + " " +  getScore() + "|" + thingsYouSaid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(thingsYouSaid);
        parcel.writeByte((byte) (lost ? 1 : 0));
        parcel.writeString(name);
    }

    public void score() {
        score += 1;
    }
    protected Player(Parcel in) {
        thingsYouSaid = in.createStringArrayList();
        lost = in.readByte() != 0;
        name = in.readString();
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
