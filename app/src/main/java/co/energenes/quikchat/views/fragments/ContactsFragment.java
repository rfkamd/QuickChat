package co.energenes.quikchat.views.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.ArrayListContacts;
import co.energenes.quikchat.models.Contact;
import co.energenes.quikchat.models.Contacts;
import co.energenes.quikchat.network.ApiResponse;
import co.energenes.quikchat.network.RestApi;
import co.energenes.quikchat.views.activities.ItemClickSupport;
import co.energenes.quikchat.views.adapters.ContactsAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by rfkamd on 7/30/2017.
 */

public class ContactsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;
    private SwipeRefreshLayout swipeContacts;
    private RecyclerView rvContacts;
    private RealmResults<Contact> results;
    RealmChangeListener listener = new RealmChangeListener<RealmResults<Contact>>() {
        @Override
        public void onChange(RealmResults<Contact> element) {
            results = element;
        }
    };
    private ContactsAdapter adapter;
    private boolean contactGranted;
    private String receiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, null, false);


        rvContacts = (RecyclerView) v.findViewById(R.id.rvContacts);
        swipeContacts = (SwipeRefreshLayout) v.findViewById(R.id.swipeContacts);

        swipeContacts.setOnRefreshListener(this);
        swipeContacts.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactGranted = true;
        } else {
            startPermissionAskingProcedure();
        }

        results = RealmHelper.getInstance(context).getAllContacts();
        adapter = new ContactsAdapter(context, results);
        rvContacts.setLayoutManager(new LinearLayoutManager(context));
        rvContacts.setAdapter(adapter);

        return v;
    }

    private void startPermissionAskingProcedure() {
        if (Utils.shouldAskPermission()) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
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
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    contactGranted = true;
                    checkForFriends();
                }
                return;
            }
        }
    }

    private void addRecyclerViewItemClickListener() {
        ItemClickSupport.addTo(rvContacts).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                String username = Prefs.getInstance(context).getStringValue(Prefs.USERNAME);
//                receiver = results.get(position).getPhone();//.getName();
//                Intent intent = new Intent(context, MainActivity.class);
//                intent.putExtra("convoId", Utils.getConvoId(username, receiver));//results.get(position).getConvoId()
//                intent.putExtra("receiver", receiver);
//                startActivity(intent);

                Toast.makeText(context, "Calling feature comming soon", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void removeRecyclerViewItemClickListener() {
        ItemClickSupport.removeFrom(rvContacts);
    }

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
        if (contactGranted) {
            checkForFriends();
        } else {
            swipeContacts.setRefreshing(false);
        }
    }

    private ArrayList<String> getAllContacts() {
        ContentResolver cr = context.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        ArrayList<String> alContacts = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (pCur.moveToNext()) {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactNumber = contactNumber.replace(" ", "").trim();
                        alContacts.add(contactNumber);
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext());
        }
        return alContacts;
    }

    private void checkForFriends() {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(getString(R.string.text_wait));
        dialog.setCancelable(false);
//        dialog.show();

        ArrayListContacts arr = new ArrayListContacts(getAllContacts());

        RestApi.getInstance().findFriends(arr, new ApiResponse() {
            @Override
            public void onResponse(boolean isSuccess, Object responseObject) {
                swipeContacts.setRefreshing(false);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Contacts res = (Contacts) responseObject;
                if ("success".equals(res.getStatus())) {
                    RealmHelper.getInstance(context).saveFriend(res.getData());
//                    try{
//                        for (Contact contact : res.getData()) {
//                            JSONObject obj = new JSONObject();
//                            obj.put("id", contact.getId());
//                            obj.put("name", contact.getName());
//                            obj.put("dateTimeStamp", Utils.getFormattedDate());
//
//                        }
//                    }catch (Exception ex){ex.printStackTrace();}
////                    startActivity(new Intent(ProfileActivity.this, ChatsActivity.class));
                } else if (res.getStatus() == "error") {
                    Toast.makeText(context, "There is an Error, Try Again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Throwable throwable, String message) {
                swipeContacts.setRefreshing(false);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                throwable.printStackTrace();
            }
        });
    }
}
