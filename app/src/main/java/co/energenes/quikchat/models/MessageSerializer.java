package co.energenes.quikchat.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by rfkamd on 7/26/2017.
 */

public class MessageSerializer implements JsonSerializer<Message> {

    @Override
    public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", src.getId());
        jsonObject.addProperty("convoId", src.getConvoId());
        jsonObject.addProperty("message", src.getMessage());
        jsonObject.addProperty("sender", src.getSender());
        jsonObject.addProperty("receiver", src.getReceiver());
        jsonObject.addProperty("state", src.getState());
        jsonObject.addProperty("synced", src.isSynced());
        jsonObject.addProperty("dateTimeStamp", src.getDateTimeStamp());

//        jsonObject.add("favoriteDog", context.serialize(src.getFavoriteDog()));
//        jsonObject.add("dogs", context.serialize(src.getDogs()));
        return jsonObject;
    }
}