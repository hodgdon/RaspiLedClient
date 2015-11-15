package com.example.dhodgdon.raspiledclient;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Button;

class MainActivityViewHolder {
    MainActivityViewHolder(@NonNull Activity activity) {
        redButton = (Button) activity.findViewById(R.id.red_button);
        blueButton = (Button) activity.findViewById(R.id.blue_button);
        greenButton = (Button) activity.findViewById(R.id.green_button);
        yellowButton = (Button) activity.findViewById(R.id.yellow_button);
    }

    final Button redButton, greenButton, yellowButton, blueButton;
}
