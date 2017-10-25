package co.energenes.quikchat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rfkamd on 7/29/2017.
 */

public class ArrayListContacts {

    @Expose
    @SerializedName("contacts")
    private List<String> contacts;

    public ArrayListContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }
}
