package co.energenes.quikchat.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Contact;
import co.energenes.quikchat.models.Message;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ConversationViewHolder>{

    private RealmResults<Message> messages;
    private LayoutInflater inflater;
    private RealmHelper helper;
    private String username;

    public ChatsAdapter(Context context, RealmResults<Message> messages){
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        helper = RealmHelper.getInstance(context);
        username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);
    }


    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public AppCompatImageView imgAvatar;
        public AppCompatTextView txtName;
        public AppCompatTextView txtDateTime;
        public AppCompatTextView txtLastText;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imgAvatar = (AppCompatImageView) itemView.findViewById(R.id.imgAvatar);
            txtName = (AppCompatTextView) itemView.findViewById(R.id.txtName);
            txtDateTime = (AppCompatTextView) itemView.findViewById(R.id.txtDateTime);
            txtLastText = (AppCompatTextView) itemView.findViewById(R.id.txtLastText);
        }
    }


    @Override
    public ChatsAdapter.ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= inflater.inflate(R.layout.layout_conversations,parent,false);
        return new ConversationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChatsAdapter.ConversationViewHolder holder, int position) {

        holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        holder.txtDateTime.setText(messages.get(position).getDateTimeStamp());
        if(!messages.get(position).getState().equals(Constants.STATE_READ)){
            holder.txtLastText.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            holder.txtLastText.setTypeface(Typeface.DEFAULT);
        }
        if(messages.get(position).getMimeType().equals(Constants.mimeType.TEXT)){
            // TODO: 8/15/2017 mimetype k hisaab se message hint set krwana ha like audio message or image
            if(messages.get(position).getMessage().length() > 25){
                holder.txtLastText.setText(messages.get(position).getMessage().substring(0,25));
            }else {
                holder.txtLastText.setText(messages.get(position).getMessage());
            }
        }
        //if user is receiver of text
        if(messages.get(position).getReceiver().equals(username)){
            Contact c = helper.getContactByPhone(messages.get(position).getSender());
            if(c != null){
                holder.txtName.setText(c.getName());
            }else{
                holder.txtName.setText(messages.get(position).getSender());
            }
        }else{
            //user is sender of text
            holder.txtName.setText(messages.get(position).getReceiver());
        }





    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void onResume(){
        messages.addChangeListener(changeListener);
    }

    public void onPause(){
        messages.removeChangeListener(changeListener);
    }

    RealmChangeListener<RealmResults<Message>> changeListener = new RealmChangeListener<RealmResults<Message>>(){

        @Override
        public void onChange(RealmResults<Message> element) {
            messages = element;
            notifyDataSetChanged();
        }
    };




}
