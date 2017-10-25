package co.energenes.quikchat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by rfkamd on 7/28/2017.
 */

public class Contact extends RealmObject{

    @Index
    @PrimaryKey
    @Expose
    @SerializedName("id")
    private String id;

    @Index
    @Expose
    @SerializedName("available")
    private String available;

    @Index
    @Expose
    @SerializedName("name")
    private String name;

    @Index
    @Expose
    @SerializedName("phone")
    private String phone;

    @Index
    @Expose
    @SerializedName("status")
    private String status;

    @Index
    @Expose
    @SerializedName("lastSeen")
    private String lastSeen;

    @Index
    @Expose
    @SerializedName("avatar")
    private String avatar;

    @Index
    private String publicKey;

    public String getId() {
        return id;
    }

    public String getJson() {
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("name",name);
            obj.put("phone",phone);
            obj.put("status",status);
            obj.put("lastSeen",lastSeen);
            obj.put("avatar",avatar);
            obj.put("available",available);
            str = obj.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }


    public String getAvailable() {

        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
