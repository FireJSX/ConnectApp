package com.example.connectapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {
    private String name;
    private String phone;
    private String email;
    private String address;

    // Konstruktor
    public Profile(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // Getter
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }

    // Parcelable-Methoden
    protected Profile(Parcel in) {
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    @Override
    public String toString() {
        return name;  // Anpassung für die Listendarstellung
    }

}

