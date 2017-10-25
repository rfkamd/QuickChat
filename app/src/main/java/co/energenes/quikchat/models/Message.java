package co.energenes.quikchat.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.PathUtil;
import co.energenes.quikchat.Utilities.Utils;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class Message extends RealmObject {

    @Index
    @PrimaryKey
    @Expose
    @SerializedName("id")
    private String id;

    @Index
    @Expose
    @SerializedName("convoId")
    private String convoId;

    @Index
    @Expose
    @SerializedName("message")
    private String message;

    @Index
    @Expose
    @SerializedName("sender")
    private String sender;

    @Index
    @Expose
    @SerializedName("receiver")
    private String receiver;

    @Index
    @Expose
    @SerializedName("state")
    private String state;

    @Index
    @Expose
    @SerializedName("synced")
    private boolean synced;

    @Index
    @Expose
    @SerializedName("dateTimeStamp")
    private String dateTimeStamp;

    @Index
    @Expose
    @SerializedName("mimeType")
    private String mimeType;

    @Index
    @Expose
    @SerializedName("uri")
    private String uri;

    @Ignore
    @Expose
    @SerializedName("bytes")
    private String bytes;


    private String getStringFromUri(Context context, Uri uri){
        String result = null;
        try {
//            InputStream is = context.getContentResolver().openInputStream(uri);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
//            byte[] buffer = new byte[8192];
//            int bytesRead;
//            try {
//                while ((bytesRead = is.read(buffer)) > -1) {
//                    b64os.write(buffer, 0, bytesRead);
//                }
//                result = baos.toString();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                closeQuietly(is);
//                closeQuietly(b64os); // This also closes baos
//            }

//            File file  = new File();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(PathUtil.getPath(context, uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            result = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }




        return result;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }


    public byte[] readBytes(Context context, Uri uri) throws IOException {

        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }


    private byte[] readImageBytes(){
        byte [] bytes = null;
        try{

//            BitmapFactory.

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return bytes;
    }


    public String convertToJSONString(Context context) {
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("convoId",convoId);
            obj.put("message",message);
            obj.put("sender",sender);
            obj.put("receiver",receiver);
            obj.put("state",state);
            obj.put("synced",synced);
            obj.put("mimeType",mimeType);
            obj.put("dateTimeStamp",dateTimeStamp);
            if(!mimeType.equals(Constants.mimeType.TEXT)){
                if(!Utils.isNullOrEmpty(uri)){
                    byte [] readBytes = readBytes(context, Uri.parse(uri));
                    obj.put("uri", null);
                    obj.put("bytes",Utils.convertBytesToBase64String(readBytes));
                }
            }else{
                obj.put("uri", uri);
            }
            str = obj.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }

    public String convertToJSONString(Context context, boolean isImage) {
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("convoId",convoId);
            obj.put("message",message);
            obj.put("sender",sender);
            obj.put("receiver",receiver);
            obj.put("state",state);
            obj.put("synced",synced);
            obj.put("mimeType",mimeType);
            obj.put("dateTimeStamp",dateTimeStamp);
            if(!mimeType.equals(Constants.mimeType.TEXT)){
                if(!Utils.isNullOrEmpty(uri)){
//                    bytes = readBytes(context, Uri.parse(uri));
                    obj.put("uri",readImageBytes());
                }
            }else{
                obj.put("uri", uri);
            }
            str = obj.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }


    public String getJson() {
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("convoId",convoId);
            obj.put("message",message);
            obj.put("sender",sender);
            obj.put("receiver",receiver);
            obj.put("state",state);
            obj.put("synced",synced);
            obj.put("mimeType",mimeType);
            obj.put("dateTimeStamp",dateTimeStamp);
            if(!mimeType.equals(Constants.mimeType.TEXT)){
                String encoded = Utils.readFileAsBase64String(uri);
                obj.put("uri",encoded);
            }else{
                obj.put("uri", uri);
            }

            str = obj.toString();
//            str = new Gson().toJson(this);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }

    public String getJson(Context context) {
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("convoId",convoId);
            obj.put("message",message);
            obj.put("sender",sender);
            obj.put("receiver",receiver);
            obj.put("state",state);
            obj.put("synced",synced);
            obj.put("mimeType",mimeType);
            obj.put("dateTimeStamp",dateTimeStamp);
            String encoded = getStringFromUri(context, Uri.parse(uri));
            obj.put("uri",encoded);


            str = obj.toString();
//            str = new Gson().toJson(this);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConvoId() {
        return convoId;
    }

    public void setConvoId(String convoId) {
        this.convoId = convoId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(String dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public static class Builder{
        Message msg;

        public Builder(){
            msg = new Message();
            msg.id = Utils.generateUUID();
        }

        public Builder sender(String sender){
            msg.sender = sender;
            return this;
        }

        public Builder receiver(String receiver){
            msg.receiver = receiver;
            return this;
        }

        public Builder message(String message){
            msg.message = message;
            return this;
        }

        public Builder dateTimeStamp(String dateTimeStamp){
            msg.dateTimeStamp = dateTimeStamp;
            return this;
        }


        public Builder state(String state){
            msg.state = state;
            return this;
        }

        public Builder convoId(String convoId){
            msg.convoId = convoId;
            return this;
        }

        public Builder synced(boolean synced){
            msg.synced = synced;
            return this;
        }

        public Builder mimeType(String mimeType){
            msg.mimeType = mimeType;
            return this;
        }

        public Builder bytesData(String bytesData){
            msg.uri = bytesData;
            return this;
        }


        public Message build(){
            if(msg.mimeType == null){
                return null;
            }
            if(msg.sender == null){
                return null;
            }
            if(msg.receiver == null){
                return null;
            }
            if(msg.convoId == null){
                msg.convoId = Utils.getConvoId(msg.sender, msg.receiver);
            }

            return msg;

        }

    }


}
