package co.energenes.quikchat.data;

import android.content.Context;
import android.net.Uri;

import java.util.List;

import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.FileUtils;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.models.Contact;
import co.energenes.quikchat.models.Message;
import co.energenes.quikchat.models.User;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class RealmHelper {

    private Realm realm;
    private static RealmHelper instance;
    private Context context;

    private RealmHelper(Context context) {
        this.context = context;
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
//                .migration(new DataMigration())
                .build();
        realm = Realm.getInstance(config);
    }

    public static RealmHelper getInstance(Context context) {
        instance = new RealmHelper(context);
        return instance;
    }

//    public void closeRealm() {
//        realm.close();
//    }

//    public void saveMessage(final Message message) {
////        long nextID = 0;
//        try{
////            Message msg = realm.where(Message.class).findFirst();
////            if(msg == null){
////                nextID = 1L;
////            }else{
////                nextID = realm.where(Message.class).max("id").longValue() + 1;
////            }
////            message.setId(nextID);
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.copyToRealm(message);
//                }
//            });
////            realm.beginTransaction();
////
////            realm.commitTransaction();
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//
////        realm.executeTransaction(new Realm.Transaction() {
////            @Override
////            public void execute(Realm realm) {
////                long nextID =  realm.where(Message.class).max("id").longValue() + 1;
////                message.setId(nextID);
////                Message s = realm.copyToRealm(message);
////            }
////        });
//    }

    public void saveMessage(final Message message) {
        try{
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
//                    realm.copyToRealm(message);
                    realm.copyToRealmOrUpdate(message);//update message if id exists
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
//        closeRealm();
    }

    public void saveUser(final User user) {
        try{
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(user);
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
//        closeRealm();
    }

    public void saveFriend(final List<Contact> user) {
        try{
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(user);
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
//        closeRealm();
    }

    public User getUser() {
        User message = realm.where(User.class).findFirst();
//        closeRealm();
        return message;
    }

//    public Contact getContact() {
//        Contact contact = realm.where(Contact.class).findFirst();
////        closeRealm();
//        return contact;
//    }

    public Message getMessageForUUID(String uuid) {
        Message message = realm.where(Message.class).equalTo(Constants.FIELD_ID, uuid).findFirst();
//        closeRealm();
        return message;
    }

    public RealmResults<Message> getUnreadMessagesForConvoId(String convoId) {
        RealmResults<Message> savedMessages = realm.where(Message.class).equalTo(Constants.FIELD_CONVO_ID, convoId).notEqualTo(Constants.FIELD_STATE, Constants.STATE_READ).findAll().sort(Constants.FIELD_DATE_TIME_STAMP, Sort.ASCENDING);
//        closeRealm();
        return savedMessages;
    }


    public void updateMessageStateWithUUID(final String uuid, final String state){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Message message = realm.where(Message.class).equalTo(Constants.FIELD_ID, uuid).findFirst();
                message.setState(state);
            }
        });
//        closeRealm();
    }

    public void updateMessageStateWithUUID(final String uuid, final String state, final boolean synced){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Message message = realm.where(Message.class).equalTo(Constants.FIELD_ID, uuid).findFirst();
                message.setState(state);
                message.setSynced(synced);
                realm.copyToRealmOrUpdate(message);


            }
        });
//        closeRealm();
    }

    public RealmResults<Message> getUnSyncedMessages() {
        RealmResults<Message> savedMessages = realm.where(Message.class).
                equalTo(Constants.FIELD_SYNCED, false).equalTo(Constants.FIELD_STATE, Constants.STATE_NONE).
                findAll().sort(Constants.FIELD_DATE_TIME_STAMP, Sort.ASCENDING);
//        closeRealm();
        return savedMessages;
    }

    public RealmResults<Contact> getAllContacts() {
        RealmResults<Contact> contacts = realm.where(Contact.class).findAll().sort(Constants.FIELD_NAME, Sort.ASCENDING);
        return contacts;
    }

    public Contact getContactByPhone(String phone) {
        Contact contact = realm.where(Contact.class).equalTo(Constants.FIELD_PHONE, phone).findFirst();
        return contact;
    }

    public RealmResults<Message> getMessagesResultSetForConvoId(String convoId) {
//        RealmResults<Message> savedMessages = realm.where(Message.class).equalTo(Constants.FIELD_CONVO_ID, convoId).findAll().sort(Constants.FIELD_DATE_TIME_STAMP, Sort.ASCENDING);
        RealmResults<Message> savedMessages = realm.where(Message.class).equalTo(Constants.FIELD_CONVO_ID, convoId).findAllSorted(Constants.FIELD_DATE_TIME_STAMP, Sort.ASCENDING);
        return savedMessages;
    }


    public void deleteConversationByConvoId(final String convoId) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<Message> messages = realm.where(Message.class).equalTo(Constants.FIELD_CONVO_ID, convoId).findAll();
                for (Message message : messages) {
                    if(!Constants.mimeType.TEXT.equals(message.getMimeType())){
                        if(!Utils.isNullOrEmpty(message.getUri())) {
                            FileUtils.deleteFile(context, Uri.parse(message.getUri()));
                        }
                    }
                    realm.where(Message.class).equalTo(Constants.FIELD_ID, message.getId()).findAll().deleteAllFromRealm();
                }

//                realm.where(Message.class).equalTo(Constants.FIELD_CONVO_ID, convoId).findAll().deleteAllFromRealm();
            }
        });
    }

    public RealmResults<Message> getConversationsResultSet() {
        RealmResults<Message> savedMessages = realm.where(Message.class).findAllSorted(Constants.FIELD_DATE_TIME_STAMP, Sort.DESCENDING).where().distinct(Constants.FIELD_CONVO_ID);
//        closeRealm();
        return savedMessages;
    }

//    public static ArrayList<Message> getConversationsList(RealmResults<Message> savedMessages) {
//        ArrayList<Message> messages = new ArrayList<>();
//        for (Message msg : savedMessages) {
//            messages.add(msg);
//        }
//        return messages;
//    }
//
//    public static ArrayList<Message> getMessagesList(RealmResults<Message> savedMessages) {
//        ArrayList<Message> messages = new ArrayList<>();
//        for (Message msg : savedMessages) {
//            messages.add(msg);
//        }
//        return messages;
//    }

}
