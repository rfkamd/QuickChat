package co.energenes.quikchat.views.activities;

import org.webrtc.MediaStream;

/**
 * Created by rfkamd on 7/20/2017.
 */

public interface RtcListener {
    void onCallReady(String callId);

    void onStatusChanged(String newStatus);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(int endPoint);
}
