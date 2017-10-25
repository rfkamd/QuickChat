package co.energenes.quikchat.views.activities;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Config;
import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.FileUtils;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Message;
import co.energenes.quikchat.models.MessageAck;
import co.energenes.quikchat.models.MessageSerializer;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketService extends Service {

//    // Registered callbacks
//    private RtcListener rtcListener;

    public static BlockingQueue<Message> MESSAGE_QUEUE;
    public static RealmResults<Message> realmResults;
    private static String TAG = "SocketService";
    private final IBinder binder = new LocalBinder();
    private String host;
    private Socket socket;
    RealmChangeListener realmChangeListener = new RealmChangeListener<RealmResults<Message>>() {
        @Override
        public void onChange(RealmResults<Message> messages) {
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                if (Constants.STATE_NONE.equals(msg.getState())) {

                    //todo 1 -done- this is message sending so send messges according to mimetype and converting to bytes and adding bytes to json and send it forward
                    //this is a text event only so emit text event with out any changes
                    if (Constants.mimeType.TEXT.equals(msg.getMimeType())) {
                        String json  = FileUtils.getJson(getApplicationContext(), msg);
                        socket.emit("text", json, new Ack() {
                            @Override
                            public void call(Object... args) {
                                try {
                                    String uuid = (String) args[0];
                                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                    }else {
                        //this is a media event and contains file - convert file to base64 string and then emit the media messsage
                        String json  = FileUtils.getJson(getApplicationContext(), msg);
                        socket.emit("media", json, new Ack() {
                            @Override
                            public void call(Object... args) {
                                try {
                                    String uuid = (String) args[0];
                                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                    }

//                    if (!Constants.mimeType.TEXT.equals(messages.get(i).getMimeType())) {
//                        String json = null;
//
////                        if(msg.getMimeType().equals(Constants.mimeType.DOCUMENT) ||
////                                msg.getMimeType().equals(Constants.mimeType.BITMAP) ||
////                                msg.getMimeType().equals(Constants.mimeType.PNG) ||
////                                msg.getMimeType().equals(Constants.mimeType.JPEG)){
////                            json = msg.getJson(getApplicationContext());
////                        }else{
////                            json = msg.getJson();
////                        }
//
////                    msg = gson.fromJson(json, Message.class);
////                    String encoded = Utils.readFileAsBase64String(msg.getUri());
////                    msg.setUri(encoded);
////                        if (Constants.mimeType.BITMAP.equals(messages.get(i).getMimeType()) ||
////                                Constants.mimeType.JPEG.equals(messages.get(i).getMimeType()) ||
////                                Constants.mimeType.PNG.equals(messages.get(i).getMimeType())) {
////
////                            json = msg.convertToJSONString(getApplicationContext(), true);
////
////                        }else if (Constants.mimeType.DOCUMENT.equals(messages.get(i).getMimeType()) ||
////                                Constants.mimeType.AUDIO_3GP.equals(messages.get(i).getMimeType()) ||
////                                Constants.mimeType.AUDIO_MPEG4.equals(messages.get(i).getMimeType())) {
////
//                            json = msg.convertToJSONString(getApplicationContext());
////                        }
//
//                            socket.emit("media", json, new Ack() {
//                            @Override
//                            public void call(Object... args) {
//                                try {
//                                    String uuid = (String) args[0];
//                                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);
//
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        });
//                    } else {
//                        //message is text type send befikr hokr
//                        String json = msg.getJson();
//                        socket.emit("text", json, new Ack() {
//                            @Override
//                            public void call(Object... args) {
//                                try {
//                                    String uuid = (String) args[0];
//                                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);
//
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        });
//                    }
                }


            }
        }
    };

    //region RTC CLient Variables

//    private final static String TAG = WebRtcClient.class.getCanonicalName();
//    private final static int MAX_PEER = 2;
//    private boolean[] endPoints = new boolean[MAX_PEER];
//    private PeerConnectionFactory factory;
//    private HashMap<String, Peer> peers = new HashMap<>();
//    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
//    private PeerConnectionParameters pcParams;
//    private MediaConstraints pcConstraints = new MediaConstraints();
//    private MediaStream localMS;
//    private VideoSource videoSource;

    //endregion RTC CLient Variables
    private Gson gson;

    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        MESSAGE_QUEUE = new ArrayBlockingQueue<>(1000);

        host = "http://" + Config.HOST;
        host += (":" + Config.PORT + "/");

//        gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
//                .create();

        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(Message.class, new MessageSerializer())
//                .registerTypeAdapter(Class.forName("io.realm.MessageRealmProxy"), new MessageSerializer())
                .create();


        try {
            socket = IO.socket(host);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        MessageHandler messageHandler = new MessageHandler(getApplicationContext());

        socket.on("id", messageHandler.onId);
        socket.on("text", messageHandler.onText);
        socket.on("ack", messageHandler.onAck);
        socket.on("statusUpdate", messageHandler.onStateUpdate);
        socket.on("media", messageHandler.onMedia);
        socket.on("call", messageHandler.onCall);
//        socket.on;


        //emit username
        try {
            socket.connect();

//            JSONObject message = new JSONObject();
//            Prefs prefs = Prefs.getInstance(getApplicationContext());
//            String username = prefs.getStringValue(Prefs.USERNAME);
//            message.put("userName", username);
//
//            socket.emit("username", message);

            realmResults = RealmHelper.getInstance(getApplicationContext()).getUnSyncedMessages();
            realmResults.addChangeListener(realmChangeListener);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return START_STICKY;
    }

    public Socket getSocket() {
        return socket;
    }

    private void addNotification(Context ctx, Intent intnt) {


        String uuid = intnt.getStringExtra("uuid");

        Message msg = RealmHelper.getInstance(ctx).getMessageForUUID(uuid);
        String text;
        if (msg.getMessage().length() > 20) {
            text = msg.getMessage().substring(0, 20);
        } else {
            text = msg.getMessage();
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msg.getSender())
                        .setContentText(text);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);
//        builder.setDefaults(Notification.DEFAULT_SOUND);

//        //Intent notificationIntent = new Intent(ctx, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intnt,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        //close db
        realmResults.removeChangeListener(realmChangeListener);
        super.onDestroy();
    }

    public void syncMessages() {
        RealmResults<Message> messages = RealmHelper.getInstance(getApplicationContext()).getUnSyncedMessages();
        for (int i = 0; i < messages.size(); i++) {
            if (Constants.STATE_NONE.equals(messages.get(i).getState())) {
                if (Constants.mimeType.TEXT.equals(messages.get(i).getMimeType())) {
                    String json = messages.get(i).getJson();
                    socket.emit("text", json, new Ack() {
                        @Override
                        public void call(Object... args) {
                            try {
                                String uuid = (String) args[0];
                                RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);
                                Log.d(TAG, "message synced" + uuid);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                } else {
                    try {
//                    JSONObject doc = new JSONObject(messages.get(i).getJson());
//                    doc.put("bytesData", );
                        // TODO: 9/8/2017 change it to use FileUtils
                        messages.get(i).setUri(Utils.readFileAsBase64String(messages.get(i).getUri()));
                        socket.emit("media", messages.get(i).getJson(), new Ack() {
                            @Override
                            public void call(Object... args) {
                                try {
                                    String uuid = (String) args[0];
                                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(uuid, Constants.STATE_SENT, true);
                                    Log.d(TAG, "message synced" + uuid);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }
    }

    public void sendMessage(final String event, final MessageAck msg) {
        socket.emit(event, msg.getJson(), new Ack() {
            @Override
            public void call(Object... args) {
                RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(msg.getId(), Constants.STATE_READ);
            }
        });
    }

    // Class used for the socket Binder.
    public class LocalBinder extends Binder {
        public SocketService getService() {
            // Return this instance of MyService so clients can call public methods
            return SocketService.this;
        }
    }

    private class MessageHandler {
        private Context context;
        private Emitter.Listener onCall = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    try {
                        Intent intent = new Intent(SocketService.this, MainActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.putExtra("url", data.getString("url"));
//                        intent.setType("text/plain");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        private Emitter.Listener onMedia = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    Message msg = gson.fromJson(data.toString(), Message.class);
                    //set Date to current date
                    //todo 2 -done- message receiver for media event convert bytes to file here based on mime type

                    FileUtils.storeJson(getApplicationContext(), msg);

//                    msg.setDateTimeStamp(Utils.getFormattedDate());
//                    String filePath = saveBytesAndGetPath(msg);
//                    msg.setUri(filePath);
//
//                    RealmHelper.getInstance(getApplicationContext()).saveMessage(msg);
                    if (Config.SHOW_NOTIFICATION) {
                        Intent intent = new Intent();
                        intent.putExtra("uuid", msg.getId());
                        intent.putExtra("type", getString(R.string.text_type_message));
                        intent.setAction(getString(R.string.text_show_notification));
                        addNotification(context, intent);
                    }
                    Ack ack = (Ack) args[args.length - 1];
                    ack.call();
                }

            }
        };
        private Emitter.Listener onText = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    Message msg = gson.fromJson(data.toString(), Message.class);
                    FileUtils.storeJson(getApplicationContext(), msg);

                    //set Date to current date
//                    msg.setDateTimeStamp(Utils.getFormattedDate());
//                    if (!Constants.mimeType.TEXT.equals(msg.getMimeType())) {
//                        //todo 3 -done- message receiver for text but have to save audio here convert bytes to audio file
//                        String filePath = saveBytesAndGetPath(msg);
//                        msg.setUri(filePath);
//                        msg.setBytes(null);
////                        RealmHelper.getInstance(getApplicationContext()).saveMessage(msg);
////                        if(Config.SHOW_NOTIFICATION){
////                            Intent intent = new Intent();
////                            intent.putExtra("uuid", msg.getId());
////                            intent.putExtra("type", getString(R.string.text_type_message));
////                            intent.setAction(getString(R.string.text_show_notification));
////                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
////                        }l
//                    }
////                    else{
////                        String filePath = saveBytesAndGetPath(msg);
////                        msg.setUri(filePath);
////                        RealmHelper.getInstance(getApplicationContext()).saveMessage(msg);
////                        if(Config.SHOW_NOTIFICATION){
////                            Intent intent = new Intent();
////                            intent.putExtra("uuid", msg.getId());
////                            intent.putExtra("type", getString(R.string.text_type_message));
////                            intent.setAction(getString(R.string.text_show_notification));
////                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
////                        }
////                    }
//
//                    RealmHelper.getInstance(getApplicationContext()).saveMessage(msg);
                    if (Config.SHOW_NOTIFICATION) {
                        Intent intent = new Intent();
                        intent.putExtra("uuid", msg.getId());
                        intent.putExtra("type", getString(R.string.text_type_message));
                        intent.setAction(getString(R.string.text_show_notification));
                        addNotification(context, intent);
//                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }

                }
                Ack ack = (Ack) args[args.length - 1];
                ack.call();
            }
        };
        private Emitter.Listener onAck = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    MessageAck ack = gson.fromJson(data.toString(), MessageAck.class);
                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(ack.getId(), Constants.STATE_RECEIVED);
                }
            }
        };
        private Emitter.Listener onStateUpdate = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    MessageAck ack = gson.fromJson(data.toString(), MessageAck.class);
                    RealmHelper.getInstance(getApplicationContext()).updateMessageStateWithUUID(ack.getId(), Constants.STATE_READ);
                }
            }
        };
        private Emitter.Listener onId = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String id = (String) args[0];

                //send server our username to store in list everytime we connect to server
                try {
                    JSONObject jsonObject = new JSONObject();
                    Prefs prefs = Prefs.getInstance(getApplicationContext());
                    String username = prefs.getStringValue(Prefs.USERNAME);
                    jsonObject.put("userName", username);
                    socket.emit("username", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "ID: " + id);
                Prefs.getInstance(getApplicationContext()).saveKey(Prefs.ID, id);
                syncMessages();
            }
        };

        MessageHandler(Context context) {
            this.context = context;
        }

        private String saveBytesAndGetPath(Message msg) {
            String path = null;
            try {

//                if (msg.getMimeType().equals(Constants.mimeType.AUDIO_MPEG4) || msg.getMimeType().equals(Constants.mimeType.AUDIO_3GP)) {
//
//                    File audio = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
//                    FileOutputStream os = new FileOutputStream(audio, true);
//                    os.write(msg.getBytes());
//                    os.flush();
//                    os.close();
//                    path = audio.toURI().toString();
//
//                }else if (msg.getMimeType().equals(Constants.mimeType.DOCUMENT)) {
//                    //for document
//                    File doc = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
//                    FileOutputStream os = new FileOutputStream(doc, true);
//                    os.write(msg.getBytes());
//                    os.flush();
//                    os.close();
//                    path = doc.toURI().toString();
//                } else {
//                    //for images
//                    byte[] bytes = msg.getBytes();//Base64.decode(msg.getUri(), Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                    File image = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
//                    FileOutputStream os = new FileOutputStream(image, true);
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    if (msg.getMimeType().equals(Constants.mimeType.PNG)) {
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
//                    } else {//if(msg.getMimeType().equals(Constants.mimeType.BITMAP) || msg.getMimeType().equals(Constants.mimeType.JPEG))
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                    }
////                    byte[] bitmapdata = bos.toByteArray();
//
//                    os.write(bos.toByteArray());
//                    os.flush();
//                    os.close();
//                    path = image.toURI().toString();
//
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return path;
        }


    }

//    public void sendBytes(final String event,String data){
//        socket.emit(event, data, new Ack() {
//            @Override
//            public void call(Object... args) {
//                String uuid = (String) args[0];
//                Message msg = RealmHelper.getInstance(getApplicationContext()).getMessageForUUID(uuid);
//                msg.setState(Constants.STATE_SENT);
//                msg.setSynced(true);
//            }
//        });
//    }


}
