package co.energenes.quikchat.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import co.energenes.quikchat.R;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Contact;
import co.energenes.quikchat.views.adapters.ContactsAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ContactPickerActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeContacts;
    private RealmResults<Contact> results;
    private ContactsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_picker);

        rvConversations = (RecyclerView) findViewById(R.id.rvConversations);
        swipeContacts = (SwipeRefreshLayout) findViewById(R.id.swipeContacts);

        swipeContacts.setOnRefreshListener(this);
        swipeContacts.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

        results = RealmHelper.getInstance(getApplicationContext()).getAllContacts();
        adapter = new ContactsAdapter(this, results);

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);

    }

    private void addRecyclerViewItemClickListener(){
        ItemClickSupport.addTo(rvConversations).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent();
                intent.putExtra("receiver", results.get(position).getPhone());
                intent.putExtra("name", results.get(position).getName());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void removeRecyclerViewItemClickListener(){
        ItemClickSupport.removeFrom(rvConversations);
    }

    RealmChangeListener listener = new RealmChangeListener<RealmResults<Contact>>() {
        @Override
        public void onChange(RealmResults<Contact> element) {
            results = element;
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
}
