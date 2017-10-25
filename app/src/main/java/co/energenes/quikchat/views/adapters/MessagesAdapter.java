package co.energenes.quikchat.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vanniktech.emoji.EmojiTextView;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.AudioPlayer;
import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.PathUtil;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.models.Message;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by rfkamd on 7/21/2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ConversationViewHolder> {


    private String username;
    private LayoutInflater inflater;
    private Context context;
    private AudioPlayer audioPlayer;
    private RealmResults<Message> messages;

    RealmChangeListener<RealmResults<Message>> changeListener = new RealmChangeListener<RealmResults<Message>>() {

        @Override
        public void onChange(RealmResults<Message> element) {
            messages = element;
            notifyDataSetChanged();
        }
    };

    public MessagesAdapter(Context context, RealmResults<Message> messages) {
        this.messages = messages;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);
        audioPlayer = AudioPlayer.getInstance(context);
    }

    @Override
    public MessagesAdapter.ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_message, parent, false);
        return new MessagesAdapter.ConversationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessagesAdapter.ConversationViewHolder holder, int position) {
        final Message msg = messages.get(position);

        //region setting data

        if (Constants.mimeType.AUDIO_MPEG4.equals(msg.getMimeType()) || Constants.mimeType.AUDIO_3GP.equals(msg.getMimeType())) {

            //region setting audio
            if (Utils.isNullOrEmpty(msg.getUri()))
                return;

            final Uri uri = Uri.parse(msg.getUri());

            int totalDuration = (int)Utils.getMediaDuration(context, uri);
            String duration = Utils.getFormattedMediaDuration(totalDuration);
            holder.playBar.setMax(totalDuration);
            holder.txtFullTime.setText(duration);

            holder.btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    audioPlayer.init(msg.getUri());

                    audioPlayer.setSeekBar(holder.playBar);
                    audioPlayer.setTotalTimeView(holder.txtFullTime);
                    audioPlayer.setPlayButton(holder.btnPlay);
                    audioPlayer.setPauseButton(holder.btnPause);

                    audioPlayer.play();

                }
            });

            holder.btnPlay.setVisibility(View.VISIBLE);
            holder.btnPause.setVisibility(View.GONE);

            holder.imgMessage.setVisibility(View.GONE);
            holder.txtMessage.setVisibility(View.GONE);
            holder.layout_audio_player.setVisibility(View.VISIBLE);
            //endregion

        } else if (Constants.mimeType.BITMAP.equals(msg.getMimeType()) || Constants.mimeType.JPEG.equals(msg.getMimeType()) || Constants.mimeType.PNG.equals(msg.getMimeType())) {

            //region setting image

            if (Utils.isNullOrEmpty(msg.getUri()))
                return;

            final Uri uri = Uri.parse(msg.getUri());
            Glide.with(context)
                    .load(uri)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgMessage)
                    .onLoadFailed(new Exception("Load Failed"), ContextCompat.getDrawable(context, R.drawable.ic_insert_photo));

            holder.imgMessage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try{
                        Intent intent = new Intent();//new Intent(Intent.ACTION_VIEW, uri);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + PathUtil.getPath(context, uri)), "image/*");
                        //intent.setDataAndType(Uri.parse("file://" + "/sdcard/test.jpg"), "image/*");
                        context.startActivity(intent);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });

//            if(msg.getSender().equals(username)){
////                String uri = msg.getUri();
//                Uri uri = Uri.parse(msg.getUri());
//                Glide.with(context).load(uri).thumbnail(1f).into(holder.imgMessage).onLoadFailed(new Exception("Load Failed"), ContextCompat.getDrawable(context, R.drawable.ic_insert_photo));
//            }else{
////                Glide.with(context).load(msg.getUri()).thumbnail(1).into(holder.imgMessage);
////                String uri = ;
//
//                Uri uri = Uri.parse(msg.getUri());
//                Glide.with(context).load(uri).thumbnail(1f).into(holder.imgMessage).onLoadFailed(new Exception("Load Failed"), ContextCompat.getDrawable(context, R.drawable.ic_insert_photo));
//
////                File file = new File(uri.getPath());
////                if(file.exists()) {
////                    Glide.with(context).load(uri).thumbnail(1f).into(holder.imgMessage).onLoadFailed(new Exception("Load Failed"), ContextCompat.getDrawable(context, R.drawable.ic_insert_photo));
////                }
//            }
            holder.imgMessage.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(View.GONE);
            holder.layout_audio_player.setVisibility(View.GONE);

            //endregion setting image

        } else if (Constants.mimeType.TEXT.equals(msg.getMimeType())) {

            //region setting text
            if (Utils.isNullOrEmpty(msg.getMessage()))
                return;
            holder.txtMessage.setText(msg.getMessage());
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.imgMessage.setVisibility(View.GONE);
            holder.layout_audio_player.setVisibility(View.GONE);

            //endregion setting text

        } else if (Constants.mimeType.DOCUMENT.equals(msg.getMimeType())) {

            //region setting document icon

            if (Utils.isNullOrEmpty(msg.getUri()))
                return;

            Glide.with(context).load(R.drawable.ic_insert_drive_file).into(holder.imgMessage);
            holder.imgMessage.setAlpha(0.50f);
            holder.txtMessage.setVisibility(View.GONE);
            holder.imgMessage.setVisibility(View.VISIBLE);
            holder.layout_audio_player.setVisibility(View.GONE);

            holder.imgMessage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try{
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(msg.getUri()));
//                        intent.setAction(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.parse("file://" + PathUtil.getPath(context, uri)), "image/*");
                        //intent.setDataAndType(Uri.parse("file://" + "/sdcard/test.jpg"), "image/*");
                        context.startActivity(intent);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });


            //endregion setting document icon
        }

        //endregion setting data

        //region setting dateTime and message state checks

        String date = DateUtils.getRelativeTimeSpanString(Utils.getFormattedDateInMillis(msg.getDateTimeStamp())).toString();
        holder.txtDateTime.setText(date);

        if (Constants.STATE_NONE.equals(msg.getState())) {
            holder.imgStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_access_time_black_24dp));
        } else if (Constants.STATE_SENT.equals(msg.getState())) {
            holder.imgStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_black_24dp));
        } else if (Constants.STATE_RECEIVED.equals(msg.getState())) {
            holder.imgStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_all_black_24dp));
        } else if (Constants.STATE_READ.equals(msg.getState())) {
            holder.imgStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_all_blue_24dp));
        }

        if (!username.equals(msg.getSender())) {
            holder.message_row_text.setBackground(context.getResources().getDrawable(R.drawable.balloon_incoming_normal));
            holder.message_row_parent.setGravity(Gravity.LEFT);
        } else {
            holder.message_row_text.setBackground(context.getResources().getDrawable(R.drawable.balloon_outgoing_normal));
            holder.message_row_parent.setGravity(Gravity.RIGHT);
        }

        //endregion setting dateTime and message state checks
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void onResume() {
        messages.addChangeListener(changeListener);
    }

    public void onPause() {
        messages.removeChangeListener(changeListener);
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {

        public EmojiTextView txtMessage;
        public AppCompatTextView txtDateTime;
        public AppCompatImageView imgMessage;
        public AppCompatImageView imgStatus;
        public LinearLayout message_row_parent;
        public LinearLayout message_row_text;
        public View layout_audio_player;
        public SeekBar playBar;
        public ImageButton btnPlay;
        public ImageButton btnPause;
        public TextView txtElapsedTime;
        public TextView txtFullTime;



        public ConversationViewHolder(View itemView) {
            super(itemView);
            layout_audio_player = itemView.findViewById(R.id.layout_audio_player);
            message_row_text = (LinearLayout) itemView.findViewById(R.id.message_row_text);
            message_row_parent = (LinearLayout) itemView.findViewById(R.id.message_row_parent);
            imgStatus = (AppCompatImageView) itemView.findViewById(R.id.imgStatus);
            imgMessage = (AppCompatImageView) itemView.findViewById(R.id.imgMessage);
            txtMessage = (EmojiTextView) itemView.findViewById(R.id.txtMessage);
            txtDateTime = (AppCompatTextView) itemView.findViewById(R.id.txtDateTime);

            playBar = (SeekBar) itemView.findViewById(R.id.playBar);
            btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlay);
            btnPause = (ImageButton) itemView.findViewById(R.id.btnPause);
            txtElapsedTime = (TextView) itemView.findViewById(R.id.txtElapsedTime);
            txtFullTime = (TextView) itemView.findViewById(R.id.txtFullTime);

        }
    }


}

