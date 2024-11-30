package com.example.connectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {

    private Context context;
    private ArrayList<Profile> profiles;

    public ProfileAdapter(Context context, ArrayList<Profile> profiles) {
        super(context, 0, profiles);
        this.context = context;
        this.profiles = profiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
        }

        Profile profile = getItem(position);
        TextView nameTextView = convertView.findViewById(R.id.profile_name);
        ImageView menuButton = convertView.findViewById(R.id.profile_menu_button);

        nameTextView.setText(profile.getProfileName());

        // Setze den OnClickListener für das Menü
        menuButton.setOnClickListener(view -> {
            showPopupMenu(view, position);
        });

        return convertView;
    }

    private void showPopupMenu(View view, int position) {
        // PopupMenu für Bearbeiten und Löschen erstellen
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = ((AppCompatActivity) context).getMenuInflater();
        inflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

        // Menüoptionen klicken
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.edit_profile) {
                // Profil bearbeiten
                ((MainActivity) context).editProfile(position);
                return true;
            } else if (itemId == R.id.delete_profile) {
                // Profil löschen
                ((MainActivity) context).deleteProfile(position);
                return true;
            }
            return false;
        });

        popupMenu.show(); // Zeige das Menü an
    }
}
