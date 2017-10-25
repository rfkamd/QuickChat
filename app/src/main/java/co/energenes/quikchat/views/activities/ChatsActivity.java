package co.energenes.quikchat.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Config;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.views.fragments.TabFragment;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ChatsActivity extends AppCompatActivity {

//    private RecyclerView rvConversations;
//    private FloatingActionButton fabNewConvo;
//    private ChatsAdapter adapter;
//
//    private SocketService socketService;
//    private boolean bound = false;
//    private String receiver;
//    private RealmResults<Message> results;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        if(!Utils.isServiceRunning(ChatsActivity.this, SocketService.class)){
            startService(new Intent(ChatsActivity.this, SocketService.class));
        }

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        fabNewConvo = (FloatingActionButton) findViewById(R.id.fabNewConvo);
//
//        rvConversations = (RecyclerView) findViewById(R.id.rvConversations);
//
//        results = RealmHelper.getInstance(getApplicationContext()).getConversationsResultSet();
//        adapter = new ChatsAdapter(ChatsActivity.this, results);
//
//        rvConversations.setLayoutManager(new LinearLayoutManager(this));
//
//        rvConversations.setAdapter(adapter);
//
//        fabNewConvo.setVisibility(View.GONE);
//                .setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                final AlertDialog.Builder dialog = new AlertDialog.Builder(ChatsActivity.this);
//                final EditText txtReceiver = new EditText(ChatsActivity.this);
//                txtReceiver.setHint("Receiver Number");
////                txtReceiver.setInputType();
//                dialog.setView(txtReceiver);
//                dialog.setCancelable(false);
//                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        receiver = txtReceiver.getText().toString();
//                        String username = Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME);
//                        String convoId = Utils.getConvoId(username ,  receiver);
//                        Intent intent = new Intent(ChatsActivity.this, ConversationActivity.class);
//                        intent.putExtra("receiver", receiver);
//                        intent.putExtra("convoId", convoId);
//                        startActivity(intent);
//                        dialogInterface.dismiss();
//                    }
//                });
//
//                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//
//                dialog.create().show();
//
//            }
//        });

        /**
         *Setup the DrawerLayout and NavigationView
         */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff) ;

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

        if(getIntent().hasExtra("uuid")){
            // TODO: 8/5/2017 goto conversation fragment for the convoId of provided uuid of message
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
        mNavigationView.inflateMenu(R.menu.menu_home);

        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                mNavigationView.setCheckedItem(item.getItemId());
                int id = item.getItemId();
                if(id == R.id.itemProfile){
                    Intent i = new Intent(ChatsActivity.this, ProfileActivity.class);
                    i.putExtra("openInEditMode", true);
                    startActivity(i);
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name, R.string.app_name);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        setSupportActionBar(toolbar);

        String avatar = Config.SOCKET_ADDRESS + RealmHelper.getInstance(getApplicationContext()).getUser().getAvatar().replace("uploads\\", "uploads/");
        if(!Utils.isNullOrEmpty(avatar)){
            Glide.with(this)
                    .load(avatar)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .into(((ImageView)mNavigationView.getHeaderView(0).findViewById(R.id.imgNavAvatar)));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.SHOW_NOTIFICATION = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Config.SHOW_NOTIFICATION = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //    private void addRecyclerViewItemClickListener(){
//        ItemClickSupport.addTo(rvConversations).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                String username = Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME);
//                if(username.equals(results.get(position).getReceiver())){
//                    receiver = results.get(position).getSender();
//                }else {
//                    receiver = results.get(position).getReceiver();
//                }
//                Intent intent = new Intent(ChatsActivity.this, MainActivity.class);
//                intent.putExtra("convoId", results.get(position).getConvoId());
//                intent.putExtra("receiver", receiver);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void removeRecyclerViewItemClickListener(){
//        ItemClickSupport.removeFrom(rvConversations);
//    }
//
//    RealmChangeListener listener = new RealmChangeListener<RealmResults<Message>>() {
//        @Override
//        public void onChange(RealmResults<Message> element) {
//            results = element;
//            rvConversations.smoothScrollToPosition(results.size());
//        }
//    };
//
//
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // bind to Service
//        Intent intent = new Intent(this, SocketService.class);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Unbind from service
//        if (bound) {
////            socketService.setCallbacks(null); // unregister
//            unbindService(serviceConnection);
//            bound = false;
//        }
//    }
//
//    /** Callbacks for service binding, passed to bindService() */
//    private ServiceConnection serviceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // cast the IBinder and get MyService instance
//            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
//            socketService = binder.getService();
//            bound = true;
////            socketService.setCallbacks(MainActivity.this); // register
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            bound = false;
//        }
//    };




//    private void setMenuCounter(@IdRes int itemId, int count) {
//        TextView view = (TextView) mNavigationView.getMenu().findItem(R.id.cart).getActionView();
//        view.setText(count > 0 ? String.valueOf(count) : null);
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        try{
//            unregisterReceiver(receiver);
//        }catch (Exception ex){
//            //ex.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        getMenuInflater().inflate( R.menu.menu_home, menu);
//
//        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
//        searchView = (SearchView) myActionMenuItem.getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Intent intent = new Intent();
//                intent.setAction("search-query");
//                intent.putExtra("message", query);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                if( ! searchView.isIconified()) {
//                    searchView.setIconified(true);
//                }
//                myActionMenuItem.collapseActionView();
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String s) {
//                Intent intent = new Intent();
//                intent.setAction("search-query");
//                intent.putExtra("message", s);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // THIS IS YOUR DRAWER/HAMBURGER BUTTON
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawer(GravityCompat.START);  // OPEN DRAWER
                }else{
                    mDrawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startPermissionAskingProcedure(){
        if (Utils.shouldAskPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
                }
                return;
            }
        }
    }


//    @Override
//    public void onBackPressed() {
//
////        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
////        if (drawer.isDrawerOpen(GravityCompat.START)) {
////            drawer.closeDrawer(GravityCompat.START);
////        }
//
//        if(isBackPressedOnce){
//            super.onBackPressed();
//            isBackPressedOnce = false;
//        }
//
//        FragmentManager fm = getSupportFragmentManager();
//        if(fm.getBackStackEntryCount() == 0) {
//
//            isBackPressedOnce = true;
//            Toast.makeText(UserActivity.this, getString(R.string.exit_app_by_Back), Toast.LENGTH_LONG).show();
//            Handler mHandler = new Handler();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    isBackPressedOnce = false;
//                }
//            }, 2000);
//        }else
//            super.onBackPressed();
//
//    }
//
//
//    public BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String productId = intent.getStringExtra("product_id");
//
//            ProductFragment fragment = new ProductFragment();
//
//            Bundle args = new Bundle();
//            args.putString("product_id", productId );
//            fragment.setArguments(args);
//
//            mFragmentManager = getSupportFragmentManager();
//            mFragmentTransaction = mFragmentManager.beginTransaction();
////            mFragmentTransaction.replace(R.id.containerView,new ProductFragment11()).commit();
//            mFragmentTransaction.replace(R.id.containerView, fragment, null);
//            mFragmentTransaction.addToBackStack(null);
//            mFragmentTransaction.commit();
//
//        }
//    };

}
