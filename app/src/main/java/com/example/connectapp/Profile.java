package com.example.connectapp;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile implements Parcelable {

    private String profileName;
    private String name;
    private String lastName;
    private String phone;
    private String email;
    private String street;
    private String houseNumber; // Hinzugefügt: Hausnummer
    private String postalCode;
    private String city;
    private String country;  // Optional
    private boolean isDefaultProfile; // Neues Attribut für Standardprofil

    // Angepasster Konstruktor
    public Profile(String profileName, String name, String lastName, String phone, String email,
                   String street, String houseNumber, String postalCode, String city, String country,
                   boolean isDefaultProfile) {
        this.profileName = profileName;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.isDefaultProfile = isDefaultProfile;
    }

    // Getter und Setter
    public boolean isDefaultProfile() {
        return isDefaultProfile;
    }

    public void setDefaultProfile(boolean isDefaultProfile) {
        this.isDefaultProfile = isDefaultProfile;
    }

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

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber; // Getter für houseNumber
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
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
        dest.writeString(street);
        dest.writeString(houseNumber); // houseNumber wird gespeichert
        dest.writeString(postalCode);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeByte((byte) (isDefaultProfile ? 1 : 0));
    }

    protected Profile(Parcel in) {
        profileName = in.readString();
        name = in.readString();
        lastName = in.readString();
        phone = in.readString();
        email = in.readString();
        street = in.readString();
        houseNumber = in.readString(); // houseNumber wird aus Parcel gelesen
        postalCode = in.readString();
        city = in.readString();
        country = in.readString();
        isDefaultProfile = in.readByte() != 0;
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

    // Methode toJson(): Konvertiert das Profil in ein JSONObject
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("profileName", profileName);
        jsonObject.put("name", name);
        jsonObject.put("lastName", lastName);
        jsonObject.put("phone", phone);
        jsonObject.put("email", email);
        jsonObject.put("street", street);
        jsonObject.put("houseNumber", houseNumber); // houseNumber wird zum JSON hinzugefügt
        jsonObject.put("postalCode", postalCode);
        jsonObject.put("city", city);
        jsonObject.put("country", country);
        jsonObject.put("isDefaultProfile", isDefaultProfile);
        return jsonObject;
    }

    // Methode fromJson(): Erstellt ein Profil aus einem JSONObject
    public static Profile fromJson(JSONObject jsonObject) throws JSONException {
        String profileName = jsonObject.getString("profileName");
        String name = jsonObject.getString("name");
        String lastName = jsonObject.getString("lastName");
        String phone = jsonObject.getString("phone");
        String email = jsonObject.getString("email");
        String street = jsonObject.getString("street");
        String houseNumber = jsonObject.getString("houseNumber"); // houseNumber wird aus JSON gelesen
        String postalCode = jsonObject.getString("postalCode");
        String city = jsonObject.getString("city");
        String country = jsonObject.getString("country");
        boolean isDefaultProfile = jsonObject.optBoolean("isDefaultProfile", false);
        return new Profile(profileName, name, lastName, phone, email, street, houseNumber, postalCode, city, country, isDefaultProfile);
    }
}
