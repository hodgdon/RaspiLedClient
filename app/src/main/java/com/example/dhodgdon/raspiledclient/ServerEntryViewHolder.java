package com.example.dhodgdon.raspiledclient;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

/**
 * Views in {@link ServerSelectionDialogFragment}
 */
class ServerEntryViewHolder {
    ServerEntryViewHolder(@NonNull Dialog root) {
        View
                server_ip_edittext = root.findViewById(R.id.server_ip_edittext),
                server_port_edittext = root.findViewById(R.id.server_port_edittext);

        if(
                server_ip_edittext instanceof EditText &&
                server_port_edittext instanceof EditText) {

            editIp = (EditText) server_ip_edittext;
            editPort = (EditText) server_port_edittext;

        } else {
            throw new RuntimeException("View Inflation Error");
        }
    }

    @NonNull
    final EditText editIp, editPort;
}
