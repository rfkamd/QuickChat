package co.energenes.quikchat.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Message;
import co.energenes.quikchat.views.activities.ContactPickerActivity;
import co.energenes.quikchat.views.activities.ItemClickSupport;
import co.energenes.quikchat.views.adapters.ChatsAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;

/**
 * Created by rfkamd on 7/30/2017.
 */

public class ChatsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView rvConversations;
    private ChatsAdapter adapter;
    private SwipeRefreshLayout swipeContacts;
    private RealmResults<Message> results;
    private Context context;
    private FloatingActionButton btnNewMessage;
    private String username;
    private String receiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_content_chats, null, false);

        btnNewMessage = (FloatingActionButton) v.findViewById(R.id.btnNewMessage);
        rvConversations = (RecyclerView) v.findViewById(R.id.rvConversations);
        swipeContacts = (SwipeRefreshLayout) v.findViewById(R.id.swipeContacts);

        swipeContacts.setOnRefreshListener(this);
        swipeContacts.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

        username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);

        results = RealmHelper.getInstance(context).getConversationsResultSet();
        adapter = new ChatsAdapter(context, results);
        rvConversations.setLayoutManager(new LinearLayoutManager(context));
        rvConversations.setAdapter(adapter);

        btnNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(context, ContactPickerActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        return v;
    }


    private void addRecyclerViewItemClickListener(){
        ItemClickSupport.addTo(rvConversations).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                String username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);
                if(username.equals(results.get(position).getReceiver())){
                    receiver = results.get(position).getSender();
                }else {
                    receiver = results.get(position).getReceiver();
                }
//                Intent intent = new Intent(context, ConversationActivity.class);
//                intent.putExtra("convoId", results.get(position).getConvoId());
//                intent.putExtra("receiver", receiver);
//                startActivity(intent);
                Bundle bundle =  new Bundle();
                bundle.putString("convoId", results.get(position).getConvoId());
                bundle.putString("receiver", receiver);
                ConversationFragment fragmet = new ConversationFragment();
                fragmet.setArguments(bundle);
                FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.containerView, fragmet).addToBackStack(null).commit();
            }
        });

        ItemClickSupport.addTo(rvConversations).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, final int position, View v) {
                // TODO: 9/7/2017 check these into strings.xml
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("All messages will be deleted beyond recovery, Do You still want to delete conversation?");
                builder.setTitle("Confirm");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String convoId = results.get(position).getConvoId();
                        RealmHelper.getInstance(context).deleteConversationByConvoId(convoId);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

    }

    private void removeRecyclerViewItemClickListener(){
        ItemClickSupport.removeFrom(rvConversations);
    }

    RealmChangeListener listener = new RealmChangeListener<RealmResults<Message>>() {
        @Override
        public void onChange(RealmResults<Message> element) {
            results = element;
            rvConversations.smoothScrollToPosition(results.size());
        }
    };

    @Override
    public void onResume() {
        adapter.onResume();
        results.addChangeListener(listener);
        addRecyclerViewItemClickListener();
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onPause() {
        adapter.onPause();
        results.removeChangeListener(listener);
        removeRecyclerViewItemClickListener();
        super.onPause();
    }


    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeContacts.setRefreshing(false);
            }
        }, 3000);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            receiver = data.getStringExtra("receiver");
            Bundle bundle =  new Bundle();
            bundle.putString("convoId", Utils.getConvoId(username, receiver));
            bundle.putString("receiver", receiver);
            ConversationFragment fragmet = new ConversationFragment();
            fragmet.setArguments(bundle);

            try{
                FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.containerView, fragmet)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
