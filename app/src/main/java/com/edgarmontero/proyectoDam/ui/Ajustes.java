package com.edgarmontero.proyectoDam.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.edgarmontero.proyectoDam.R;

public class Ajustes extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PREFERENCES_FILE = "com.edgarmontero.proyectoDam.preferences";
    private static final String NOTIFICATIONS_KEY = "notifications_enabled";
    private static final String DARK_MODE_KEY = "dark_mode_enabled";

    private String mParam1;
    private String mParam2;

    public Ajustes() {
        // Required empty public constructor
    }

    public static Ajustes newInstance(String param1, String param2) {
        Ajustes fragment = new Ajustes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Check saved preferences for dark mode
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = preferences.getBoolean(DARK_MODE_KEY, false);
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        // Switch to enable or disable notifications
        Switch switchNotifications = view.findViewById(R.id.switch_notifications);
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableNotifications();
                } else {
                    disableNotifications();
                }
            }
        });

        Switch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        switchDarkMode.setChecked(preferences.getBoolean(DARK_MODE_KEY, false));

        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    Toast.makeText(getContext(), "Dark Mode Enabled", Toast.LENGTH_SHORT).show();
                    enableDarkMode();
                    editor.putBoolean(DARK_MODE_KEY, true);
                } else {
                    Toast.makeText(getContext(), "Dark Mode Disabled", Toast.LENGTH_SHORT).show();
                    disableDarkMode();
                    editor.putBoolean(DARK_MODE_KEY, false);
                }
                editor.apply();
            }
        });

        return view;
    }

    // Function to enable dark mode
    private void enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    // Function to disable dark mode
    private void disableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void enableNotifications() {
        Toast.makeText(getContext(), "Notificaciones activadas", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        Toast.makeText(getContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
    }
}
