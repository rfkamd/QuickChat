package co.energenes.quikchat.views.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import co.energenes.quikchat.R;
import co.energenes.quikchat.models.Contact;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by rfkamd on 8/1/2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ConversationViewHolder>{

    private RealmResults<Contact> contacts;
    private Context context;
    private LayoutInflater inflater;

    public ContactsAdapter(Context context, RealmResults<Contact> contacts){
        this.contacts = contacts;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }


    public class ConversationViewHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView txtName;
        public CircleImageView imgAvatar;

        public ConversationViewHolder(View itemView) {
            super(itemView);

            txtName = (AppCompatTextView) itemView.findViewById(R.id.txtName);
            imgAvatar = (CircleImageView) itemView.findViewById(R.id.imgAvatar);

        }
    }


    @Override
    public ContactsAdapter.ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= inflater.inflate(R.layout.layout_contact,parent,false);
        return new ContactsAdapter.ConversationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.ConversationViewHolder holder, int position) {
        holder.txtName.setText(contacts.get(position).getName());
//        holder.imgAvatar.setText(contacts.get(position).getAvatar());
        Glide.with(context)
                .load(contacts.get(position).getAvatar())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_person_black_144dp)
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void onResume(){
        contacts.addChangeListener(changeListener);
    }

    public void onPause(){
        contacts.removeChangeListener(changeListener);
    }

    RealmChangeListener<RealmResults<Contact>> changeListener = new RealmChangeListener<RealmResults<Contact>>(){

        @Override
        public void onChange(RealmResults<Contact> element) {
            contacts = element;
            notifyDataSetChanged();
        }
    };


}

