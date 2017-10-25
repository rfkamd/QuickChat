package co.energenes.quikchat.views.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiEditText;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Message;
import co.energenes.quikchat.models.MessageAck;
import co.energenes.quikchat.views.activities.SocketService;
import co.energenes.quikchat.views.adapters.MessagesAdapter;
import co.energenes.quikchat.views.widget.ui.ViewProxy;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by rfkamd on 8/3/2017.
 */

public class ConversationFragment extends Fragment {

    boolean audioAvailable = true;
    private View layout_message;
    private View layout_audio;
    private Context context;
    private EmojiEditText txtMessage;
    private ImageButton btnAttachment;
    private ImageButton btnImageSelect;
    private ImageButton btnSend;
    private boolean recordAudio;
    private String convoId = null;
    private String receiver;
    private String username;
    private Message.Builder builder;
    //-----------------------------------------
    private TextView recordTimeText;
    private View slideText;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    //-----------------------------------------
    private long startTime = 0L;
    private Timer timer;
    private AudioTimerTask myTimerTask;

    private String audioSavePathInDevice;

    private MediaRecorder mediaRecorder;
    private Message msg;

    private RecyclerView rvConversations;
    private MessagesAdapter adapter;
    private RealmResults<Message> results;
    private SocketService socketService;
    RealmChangeListener listener = new RealmChangeListener<RealmResults<Message>>() {
        @Override
        public void onChange(RealmResults<Message> element) {
            results = element;
            rvConversations.smoothScrollToPosition(results.size());
            updateStatus();
        }
    };
    private boolean bound = false;
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
            updateStatus();
//            socketService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_conversation, container, false);

        username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);
        receiver = getArguments().getString("receiver");
        convoId = getArguments().getString("convoId");

        txtMessage = (EmojiEditText) root.findViewById(R.id.txtMessage);
        btnAttachment = (ImageButton) root.findViewById(R.id.btnAttachment);
        btnImageSelect = (ImageButton) root.findViewById(R.id.btnImageSelect);
        btnSend = (ImageButton) root.findViewById(R.id.btnSend);

        slideText = root.findViewById(R.id.slideText);
//        recordPanel = root.findViewById(R.id.record_panel);
        recordTimeText = (TextView) root.findViewById(R.id.recording_time_text);

        layout_message = root.findViewById(R.id.layout_message);
        layout_audio = root.findViewById(R.id.layout_audio);

        if (!checkPermission()) {
            requestPermission();
        } else {
            audioAvailable = true;
        }

        if (audioAvailable) {
            if (Utils.isNullOrEmpty(txtMessage.getText().toString())) {
                btnSend.setImageResource(R.drawable.mic);
                recordAudio = true;
            } else {
                btnSend.setImageResource(android.R.drawable.ic_menu_send);
                recordAudio = false;
            }
        }


        rvConversations = (RecyclerView) root.findViewById(R.id.rvConversations);
        rvConversations.setLayoutManager(new LinearLayoutManager(getActivity()));

        results = RealmHelper.getInstance(context).getMessagesResultSetForConvoId(convoId);

        adapter = new MessagesAdapter(getActivity(), results);
        rvConversations.setAdapter(adapter);


        initListeners();
        return root;
    }


    private void onSendButtonDown(){

        Animation a = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        a.setFillAfter(true);
        btnSend.startAnimation(a);

        layout_message.animate()
                .translationX(layout_message.getWidth())
                .setDuration(100)
                .alpha(0.0f)
                .setListener(null).start();

        layout_audio.animate()
                .translationX(0)
                .setDuration(100)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layout_audio.setVisibility(View.VISIBLE);
                    }
                }).start();
    }

    private void onSendButtonUp(){

        Animation a = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        a.setFillAfter(true);
        btnSend.startAnimation(a);

        // Start the animation
        layout_message.animate()
                .translationX(0)
                .setDuration(100)
                .alpha(1.0f)
                .setListener(null).start();

        layout_audio.animate()
                .translationX(layout_audio.getWidth())
                .setDuration(100)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layout_audio.setVisibility(View.GONE);
                    }
                }).start();
    }

    public void initListeners() {

        txtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (audioAvailable) {
                    if (Utils.isNullOrEmpty(txtMessage.getText().toString())) {
                        btnSend.setImageResource(R.drawable.mic);
                        recordAudio = true;
                    } else {
                        btnSend.setImageResource(android.R.drawable.ic_menu_send);
                        recordAudio = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        btnSend.setOnTouchListener(new View.OnTouchListener() {

            private boolean canceled;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (audioAvailable && recordAudio) {

                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();
                        params.leftMargin = dp(30);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                        startRecording();
                        btnSend.getParent().requestDisallowInterceptTouchEvent(true);
                        layout_audio.setVisibility(View.VISIBLE);

                        onSendButtonDown();

                        canceled = false;

                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                        startedDraggingX = -1;
                        onSendButtonUp();

                        if(!canceled){
                            stopRecording();
                            view.performClick();
                        }else{
                            cancelRecording();
                            Log.e("rfk", "Canceled Recording");
                        }

                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {



                        float x = motionEvent.getX();
                        if (x < -distCanMove) {
                            //cancelRecording();//stopRecording();
                        }
                        x = x + ViewProxy.getX(btnSend);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();

                        //continue drag
                        if (startedDraggingX != -1) {

                            canceled = true;

                            float dist = (x - startedDraggingX);
                            params.leftMargin = dp(30) + (int) dist;
                            slideText.setLayoutParams(params);
                            float alpha = 1.0f + dist / distCanMove;
                            if (alpha > 1) {
                                alpha = 1;
                            } else if (alpha < 0) {
                                alpha = 0;
                            }
                            ViewProxy.setAlpha(slideText, alpha);
                        }

                        //start Drag
                        if (x <= ViewProxy.getX(slideText) + slideText.getWidth() + dp(30)) {

                            if (startedDraggingX == -1) {

                                startedDraggingX = x;
                                distCanMove = (layout_audio.getMeasuredWidth() - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                                if (distCanMove <= 0) {
                                    distCanMove = dp(80);
                                } else if (distCanMove > dp(80)) {
                                    distCanMove = dp(80);
                                }
                            }
                        }

                        if (params.leftMargin > dp(30)) {
                            params.leftMargin = dp(30);
                            slideText.setLayoutParams(params);
                            ViewProxy.setAlpha(slideText, 1);
                            startedDraggingX = -1;
                        }
                    }


                }else{
                    view.performClick();
                }

                view.onTouchEvent(motionEvent);
                return true;
            }



        });


//        btnSend.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                //region record Audio if text is empty and audio available
//
//                if (audioAvailable && recordAudio) {
//
//                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//
//                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();
//                        params.leftMargin = dp(30);
//                        slideText.setLayoutParams(params);
//                        ViewProxy.setAlpha(slideText, 1);
//                        startedDraggingX = -1;
//                         startRecording();
//                        btnSend.getParent().requestDisallowInterceptTouchEvent(true);
//                        recordPanel.setVisibility(View.VISIBLE);
//
//                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
//
//                        startedDraggingX = -1;
//                         stopRecording();
//
//                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//
//                        float x = motionEvent.getX();
//                        if (x < -distCanMove) {
//                             stopRecording();
//                        }
//                        x = x + ViewProxy.getX(btnSend);
//                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();
//                        if (startedDraggingX != -1) {
//
//                            float dist = (x - startedDraggingX);
//                            params.leftMargin = dp(30) + (int) dist;
//                            slideText.setLayoutParams(params);
//                            float alpha = 1.0f + dist / distCanMove;
//                            if (alpha > 1) {
//                                alpha = 1;
//                            } else if (alpha < 0) {
//                                alpha = 0;
//                            }
//                            ViewProxy.setAlpha(slideText, alpha);
//                        }
//
//                        if (x <= ViewProxy.getX(slideText) + slideText.getWidth() + dp(30)) {
//
//                            if (startedDraggingX == -1) {
//                                startedDraggingX = x;
//                                distCanMove = (recordPanel.getMeasuredWidth()
//                                        - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
//                                if (distCanMove <= 0) {
//                                    distCanMove = dp(80);
//                                } else if (distCanMove > dp(80)) {
//                                    distCanMove = dp(80);
//                                }
//                            }
//                        }
//                        if (params.leftMargin > dp(30)) {
//                            params.leftMargin = dp(30);
//                            slideText.setLayoutParams(params);
//                            ViewProxy.setAlpha(slideText, 1);
//                            startedDraggingX = -1;
//                        }
//                    }
//                }
//
//                view.onTouchEvent(motionEvent);
//                return true;
//                //endregion
//            }
//        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msg == null) {
                    if(!Utils.isNullOrEmpty(txtMessage.getText().toString())){
                        convoId = Utils.getConvoId(username, receiver);
                        builder = new Message.Builder();

                        Message msg = builder.sender(username).
                                receiver(receiver).
                                dateTimeStamp(Utils.getFormattedDate()).
                                message(txtMessage.getText().toString()).
                                state(Constants.STATE_NONE).
                                synced(false).
                                mimeType(Constants.mimeType.TEXT).
                                convoId(convoId).build();

                        RealmHelper.getInstance(context).saveMessage(msg);
                        System.out.println("message saved in DB");
                        txtMessage.setText("");
                    }
                }else{
                    RealmHelper.getInstance(context).saveMessage(msg);
                }
            }
        });


        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentFragment myDialog = new AttachmentFragment();
                myDialog.setOnResultListener(resultListener);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                myDialog.show(fm, "AttachmentFragment");
            }
        });

    }

    private void updateStatus() {
        if (socketService == null) {
//            update = true;
            return;
        }
        RealmResults<Message> unreadMessages = RealmHelper.getInstance(context).getUnreadMessagesForConvoId(convoId);
        for (Message msg : unreadMessages) {
            if(Prefs.getInstance(context).getStringValue(Prefs.USERNAME).equals(msg.getReceiver())){
                MessageAck ack = new MessageAck();
                ack.setId(msg.getId());
                ack.setReceiver(msg.getSender());
                ack.setState(Constants.STATE_READ);
                socketService.sendMessage("statusUpdate", ack);
            }
        }
    }

    public void MediaRecorderReady(String filePath) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioSavePathInDevice);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }

    private void startRecording() {
        startTime = SystemClock.uptimeMillis();
        timer = new Timer();
        myTimerTask = new AudioTimerTask();

        builder = new Message.Builder();

        msg = builder.sender(username).
                receiver(receiver).
                dateTimeStamp(Utils.getFormattedDate()).
                state(Constants.STATE_NONE).
                synced(false).
                mimeType(Constants.mimeType.AUDIO_MPEG4).
                convoId(convoId).build();

        audioSavePathInDevice = Utils.getExternalFilePath(msg.getId(), Constants.mimeType.AUDIO_MPEG4);
        MediaRecorderReady(null);//audioSavePathInDevice);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            timer.schedule(myTimerTask, 1000, 1000);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        if(mediaRecorder == null) {
            return;
        }
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        msg.setUri(new File(audioSavePathInDevice).toURI().toString());
        recordTimeText.setText("00:00");
    }

    private void cancelRecording() {
        if (timer != null) {
            timer.cancel();
        }
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            File file = new File(Utils.getInternalFilePath(getActivity(), msg.getId(), Constants.mimeType.AUDIO_MPEG4));
            if(file.exists()){
                file.delete();
            }
            recordTimeText.setText("00:00");
        }
        msg = null;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 100);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(context,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    audioAvailable = (StoragePermission && RecordPermission) ? true : false;

                }
                break;
        }
    }

    @Override
    public void onResume() {
        adapter.onResume();
        results.addChangeListener(listener);
        updateStatus();
        rvConversations.smoothScrollToPosition(adapter.getItemCount());
        super.onResume();
    }

    @Override
    public void onPause() {
        adapter.onPause();
        results.removeChangeListener(listener);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        // bind to Service
        Intent intent = new Intent(context, SocketService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from service
        if (bound) {
//            socketService.setCallbacks(null); // unregister
            context.unbindService(serviceConnection);
            bound = false;
        }
    }

    class AudioTimerTask extends TimerTask {
        private String hms = "00:00";

        public String getHMS() {
            return hms;
        }

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastsec + " hms " + hms);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (recordTimeText != null)
                            recordTimeText.setText(hms);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public AttachmentFragment.ResultListener resultListener = new AttachmentFragment.ResultListener() {
        @Override
        public void onResult(String result, int requestCode) {
            if(requestCode == 1024 || requestCode == 2048 ){
                //textbased messages fowrd to user
                if(!Utils.isNullOrEmpty(result)){
                    convoId = Utils.getConvoId(username, receiver);
                    builder = new Message.Builder();

                    Message msg = builder.sender(username).
                            receiver(receiver).
                            dateTimeStamp(Utils.getFormattedDate()).
                            message(result).
                            state(Constants.STATE_NONE).
                            synced(false).
                            mimeType(Constants.mimeType.TEXT).
                            convoId(convoId).build();

                    RealmHelper.getInstance(context).saveMessage(msg);
                    System.out.println("message saved in DB");
                }
            }else if(requestCode == 3072){//for image
                //read the binary convert to base64 string then send
                if(!Utils.isNullOrEmpty(result)){
                    convoId = Utils.getConvoId(username, receiver);
                    builder = new Message.Builder();

                    Message msg = builder.sender(username).
                            receiver(receiver).
                            dateTimeStamp(Utils.getFormattedDate()).
                            bytesData(result).
                            state(Constants.STATE_NONE).
                            synced(false).
                            mimeType(Constants.mimeType.JPEG).
                            convoId(convoId).build();

                    RealmHelper.getInstance(context).saveMessage(msg);
                    System.out.println("message saved in DB");
                }

            }else{// for document
                //read the binary convert to base64 string then send
                if(!Utils.isNullOrEmpty(result)){
                    convoId = Utils.getConvoId(username, receiver);
                    builder = new Message.Builder();

                    Message msg = builder.sender(username).
                            receiver(receiver).
                            dateTimeStamp(Utils.getFormattedDate()).
                            bytesData(result).
                            state(Constants.STATE_NONE).
                            synced(false).
                            mimeType(Constants.mimeType.DOCUMENT).
                            convoId(convoId).build();

                    RealmHelper.getInstance(context).saveMessage(msg);
                    System.out.println("message saved in DB");
                }
            }
        }
    };


}
