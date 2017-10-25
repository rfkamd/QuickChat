package co.energenes.quikchat.views.activities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rfkamd on 7/20/2017.
 */

public interface Command {
    void execute(String peerId, JSONObject payload) throws JSONException;
}
