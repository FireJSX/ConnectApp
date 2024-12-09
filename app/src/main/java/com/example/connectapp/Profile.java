package com.example.connectapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {

    private String profileName;
    private String name;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private boolean isDefaultProfile; // Neues Attribut für Standardprofil

    // Konstruktor mit dem isDefaultProfile-Flag
    public Profile(String profileName, String name, String lastName, String phone, String email, String address, boolean isDefaultProfile) {
        this.profileName = profileName;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.isDefaultProfile = isDefaultProfile; // Setze das Standardprofil
    }

    // Getter und Setter
    public boolean isDefaultProfile() {
        return isDefaultProfile;
    }

    public void setDefaultProfile(boolean isDefaultProfile) {
        this.isDefaultProfile = isDefaultProfile;
    }

    // Die restlichen Getter-Methoden
    public String getProfileName() {
        return profileName;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    // Parcelable-Methoden für die Übergabe über Intents
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profileName);
        dest.writeString(name);
        dest.writeString(lastName);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeByte((byte) (isDefaultProfile ? 1 : 0)); // Schreibe das isDefaultProfile-Flag
    }

    protected Profile(Parcel in) {
        profileName = in.readString();
        name = in.readString();
        lastName = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
        isDefaultProfile = in.readByte() != 0; // Lese das isDefaultProfile-Flag
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
}
