package co.energenes.quikchat.models;

import org.json.JSONObject;

/**
 * Created by rfkamd on 7/24/2017.
 */

public class MessageAck {

    private String id;
    private String state;
    private String receiver;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getJson(){
        String str = null;
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("state",state);
            obj.put("receiver",receiver);
            str = obj.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return str;
    }
}
