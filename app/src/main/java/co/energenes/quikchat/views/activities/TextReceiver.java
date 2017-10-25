package co.energenes.quikchat.views.activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import co.energenes.quikchat.R;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Message;

public class TextReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("type").equals(context.getString(R.string.text_type_message))){
            addNotification(context, intent);
        }else{
            // TODO: 8/5/2017 show call screen
        }
    }



    private void addNotification(Context ctx, Intent intnt) {


        String uuid = intnt.getStringExtra("uuid");

        Message msg = RealmHelper.getInstance(ctx).getMessageForUUID(uuid);
        String text;
        if(msg.getMessage().length() > 20){
             text = msg.getMessage().substring(0, 20);
        }else{
            text = msg.getMessage();
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msg.getSender())
                        .setContentText(text);

//        //Intent notificationIntent = new Intent(ctx, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intnt,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

}
