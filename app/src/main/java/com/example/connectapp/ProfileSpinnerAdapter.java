package com.example.connectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileSpinnerAdapter extends ArrayAdapter<Profile> {
    private Context context;
    private ArrayList<Profile> profiles;
    private String defaultProfileName;

    public ProfileSpinnerAdapter(Context context, ArrayList<Profile> profiles, String defaultProfileName) {
        super(context, 0, profiles);
        this.context = context;
        this.profiles = profiles;
        this.defaultProfileName = defaultProfileName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, true);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
        }

        Profile profile = profiles.get(position);

        TextView profileNameTextView = convertView.findViewById(R.id.profile_name);
        ImageView starIcon = convertView.findViewById(R.id.icon_star);
        ImageView menuButton = convertView.findViewById(R.id.profile_menu_button);

        profileNameTextView.setText(profile.getProfileName());

        // Zeige den Stern nur, wenn das Profil das Standardprofil ist
        if (profile.getProfileName().equals(defaultProfileName)) {
            starIcon.setVisibility(View.VISIBLE);
        } else {
            starIcon.setVisibility(View.GONE);
        }

        // Blende das Menü-Icon im Spinner-Dropdown aus
        if (isDropdown) {
            menuButton.setVisibility(View.GONE);
        } else {
            menuButton.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
