package co.energenes.quikchat.views.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Config;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements RtcListener {
    private final static int VIDEO_CALL_SENT = 666;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    Point displaySize;
    PeerConnectionParameters params;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private WebRtcClient client;
    private Socket socket;
    private String callerId;
    private SocketService socketService;
    private boolean bound = false;
    private String id;
    private String receiver;
    private String convoId;
    private Button btnAcceptCall;
    private Button btnRejectCall;
    private AudioManager audioManager;

    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            bound = true;
            socket = socketService.getSocket();

            if (params != null) {
//                client = new WebRtcClient(MainActivity.this, socket, params, VideoRendererGui.getEGLContext(), id);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        askPermission();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);

        btnAcceptCall = (Button) findViewById(R.id.btnAcceptCall);
        btnRejectCall = (Button) findViewById(R.id.btnRejectCall);

        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);

        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                init();
            }
        });

        // local and remote render
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            String url = getIntent().getStringExtra("url");
            String[] arr = url.split("/");
            callerId = arr[3];
        }

        id = Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.ID);

        if (getIntent().hasExtra("receiver")) {
            receiver = getIntent().getStringExtra("receiver");
        }

        if (getIntent().hasExtra("convoId")) {
            convoId = getIntent().getStringExtra("convoId");
        }

        btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChatsActivity.class));
                finish();
            }
        });

        btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    answer(callerId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

//        if(Utils.isNullOrEmpty(callerId)){
//            btnAcceptCall.setVisibility(View.GONE);
//        }

        btnAcceptCall.setVisibility(View.GONE);

    }

    //region permission stuff

    private void askPermission() {

        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WAKE_LOCK};


        if (Utils.shouldAskPermission()) {

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Permission Require");
                        builder.setMessage("QuikChat Requires " + permission + " to continue");
                        builder.create().show();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
                    }
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                return;
            }
        }
    }

    //endregion permission stuff


    //region bind and unbind service

    @Override
    protected void onStart() {
        // bind to Service
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Unbind from service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
    }

    //endregion bind and unbind service

    private void init() {
        for (int i = 0; i < 500 ; i++) {
            continue;
        }

        displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        if (bound) {
            client = new WebRtcClient(this, socket, params, VideoRendererGui.getEGLContext(), id);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bound) {
            if (vsv != null) {
                vsv.onPause();
            }
            if (client != null) {
                client.onPause();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bound) {
            if (vsv != null) {
                vsv.onResume();
            }
            if (client != null) {
                client.onResume();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.onDestroy();
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);
        super.onDestroy();
    }


    @Override
    public void onCallReady(String callId) {
        if (callerId != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnAcceptCall.setVisibility(View.VISIBLE);
                }
            });
        } else {
            call(callId);
        }
    }

    public void answer(String callerId) throws JSONException {
        client.sendMessage(callerId, "init", null);
        startCam();
        btnAcceptCall.setVisibility(View.GONE);
    }

    public void call(String callId) {
        try {
            startCam();
            JSONObject obj = new JSONObject();
            obj.put("receiver", receiver);
            obj.put("url", Config.SOCKET_ADDRESS + callId);
            socket.emit("call", obj);
            Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void startCam() {
        // Camera settings
        if (client != null) {
            client.start(convoId);//"android_test");
        }
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(remoteRender,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                scalingType);

        remoteStream.audioTracks.get(0).setEnabled(true);
        remoteStream.audioTracks.get(0).setState(MediaStreamTrack.State.LIVE);

//        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        audioManager.setSpeakerphoneOn(true);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);

    }
}
