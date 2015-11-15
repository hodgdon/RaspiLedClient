package com.example.dhodgdon.raspiledclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class ServerSelectionDialogFragment extends AppCompatDialogFragment{
    /**
     * Implement to receive user-selected IP:PORT from dialog
     */
    interface ServerSelectionFeedback {
        void setServerIpPort(@Nullable String ip, @Nullable Integer port);
    }

    /**
     * @param ip initial string for server IP (or null for no initial string)
     * @param port initial port number (or null for no initial port number)
     * @return instance of dialog fragment which requests IP:PORT from user
     */
    static AppCompatDialogFragment newInstance(@Nullable String ip, @Nullable Integer port) {
        Bundle arguments = new Bundle();
        if(null != ip) {
            arguments.putString(KEY_IP, ip);
        }
        if(null != port) {
            arguments.putInt(KEY_PORT, port);
        }
        ServerSelectionDialogFragment fragment = new ServerSelectionDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_useArgs = savedInstanceState == null;

        return new AlertDialog.Builder(getContext())
                .setTitle("Raspberry Pi Address")
                .setView(R.layout.view_server_entry)
                .setPositiveButton("OK", m_onPositiveClicked)
                .create();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if(dialog != null) {
            m_views = new ServerEntryViewHolder(dialog);
            if(m_useArgs) {
                if (getArguments().containsKey(KEY_IP)) {
                    m_views.editIp.setText(getArguments().getString(KEY_IP));
                }
                if (getArguments().containsKey(KEY_PORT)) {
                    m_views.editPort.setText(Integer.toString(getArguments().getInt(KEY_PORT)));
                }
            }
        }
    }

    private final DialogInterface.OnClickListener m_onPositiveClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            if(m_feedback != null && m_views != null) {
                String ip = m_views.editIp.getText().toString();
                String portString = m_views.editPort.getText().toString();

                Integer port;
                try {
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    port = null;
                }

                m_feedback.setServerIpPort(ip, port);
            }
            dismiss();

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof ServerSelectionFeedback) {
            m_feedback = (ServerSelectionFeedback) activity;
        }
    }

    @Nullable
    private ServerSelectionFeedback m_feedback;

    @Nullable
    private ServerEntryViewHolder m_views;

    private boolean m_useArgs;

    private static final String KEY_IP = "ip", KEY_PORT = "port";
}
