package co.energenes.quikchat.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Base64OutputStream;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.Message;

/**
 * Created by rfkamd on 9/3/2017.
 */

public class FileUtils {


    public static String getJson(Context context, Message msg) {

        //return simple JSON string if message is plain text, location or contact card
        if (msg.getMimeType().equals(Constants.mimeType.TEXT)) {
            return getJson(msg);
        }

        //return audio bytes in json as base64 string
        if (msg.getMimeType().equals(Constants.mimeType.AUDIO_3GP) || msg.getMimeType().equals(Constants.mimeType.AUDIO_MPEG4)) {
            return getAudioJson(context, msg);
        }

        //return image bytes in json as base64 string
        if (msg.getMimeType().equals(Constants.mimeType.BITMAP) || msg.getMimeType().equals(Constants.mimeType.JPEG) || msg.getMimeType().equals(Constants.mimeType.PNG)) {
            return getImageJson(context, msg);
        }

        //return document bytes in json as base64 string
        if (msg.getMimeType().equals(Constants.mimeType.DOCUMENT)) {
            return getDocumentJson(context, msg);
        }

        return null;
    }

    private static String getJson(Message msg) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", msg.getId());
            obj.put("convoId", msg.getConvoId());
            obj.put("message", msg.getMessage());
            obj.put("sender", msg.getSender());
            obj.put("receiver", msg.getReceiver());
            obj.put("state", msg.getState());
            obj.put("synced", msg.isSynced());
            obj.put("mimeType", msg.getMimeType());
            obj.put("dateTimeStamp", msg.getDateTimeStamp());
            obj.put("uri", msg.getUri());
            obj.put("bytes", msg.getBytes());
            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getImageJson(Context context, Message msg) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", msg.getId());
            obj.put("convoId", msg.getConvoId());
            obj.put("message", msg.getMessage());
            obj.put("sender", msg.getSender());
            obj.put("receiver", msg.getReceiver());
            obj.put("state", msg.getState());
            obj.put("synced", msg.isSynced());
            obj.put("mimeType", msg.getMimeType());
            obj.put("dateTimeStamp", msg.getDateTimeStamp());
            obj.put("uri", null);

            Uri uri = Uri.parse(msg.getUri());

            obj.put("bytes", readImageFileAsBase64String(context, uri));


            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getDocumentJson(Context context, Message msg) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", msg.getId());
            obj.put("convoId", msg.getConvoId());
            obj.put("message", msg.getMessage());
            obj.put("sender", msg.getSender());
            obj.put("receiver", msg.getReceiver());
            obj.put("state", msg.getState());
            obj.put("synced", msg.isSynced());
            obj.put("mimeType", msg.getMimeType());
            obj.put("dateTimeStamp", msg.getDateTimeStamp());
            obj.put("uri", null);

            Uri uri = Uri.parse(msg.getUri());

            obj.put("bytes", readFileAsBase64String(context, uri));


            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getAudioJson(Context context, Message msg) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", msg.getId());
            obj.put("convoId", msg.getConvoId());
            obj.put("message", msg.getMessage());
            obj.put("sender", msg.getSender());
            obj.put("receiver", msg.getReceiver());
            obj.put("state", msg.getState());
            obj.put("synced", msg.isSynced());
            obj.put("mimeType", msg.getMimeType());
            obj.put("dateTimeStamp", msg.getDateTimeStamp());
            obj.put("uri", null);

            Uri uri = Uri.parse(msg.getUri());

            obj.put("bytes", readFileAsBase64String(context, uri));



            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }

    private static String readImageFileAsBase64String(Context context, Uri uri) throws IOException {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(PathUtil.getPath(context, uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String readFileAsBase64String(Context context, Uri uri) {
        String file = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);//new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = is.read(buffer)) > -1) {
                    b64os.write(buffer, 0, bytesRead);
                }
                file = baos.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeQuietly(is);
                closeQuietly(b64os); // This also closes baos
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void storeJson(Context context, Message msg) {
        try {
            msg.setDateTimeStamp(Utils.getFormattedDate());
            if (Constants.mimeType.TEXT.equals(msg.getMimeType())) {
                RealmHelper.getInstance(context).saveMessage(msg);
            }

            if (Constants.mimeType.DOCUMENT.equals(msg.getMimeType())) {
                msg.setUri(storeAndGetDocumentFilePath(msg));
                msg.setBytes(null);
                RealmHelper.getInstance(context).saveMessage(msg);
            }

            if (Constants.mimeType.AUDIO_3GP.equals(msg.getMimeType()) || Constants.mimeType.AUDIO_MPEG4.equals(msg.getMimeType())) {
                msg.setUri(storeAndGetAudioFilePath(msg));
                msg.setBytes(null);
                RealmHelper.getInstance(context).saveMessage(msg);
            }

            if (Constants.mimeType.BITMAP.equals(msg.getMimeType()) || Constants.mimeType.JPEG.equals(msg.getMimeType()) || Constants.mimeType.PNG.equals(msg.getMimeType())) {
                msg.setUri(storeAndGetImageFilePath(msg));
                msg.setBytes(null);
                RealmHelper.getInstance(context).saveMessage(msg);
            }



//            if(Config.SHOW_NOTIFICATION) {
//                Intent intent = new Intent();
//                intent.putExtra("uuid", msg.getId());
//                intent.putExtra("type", getString(R.string.text_type_message));
//                intent.setAction(getString(R.string.text_show_notification));
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String storeAndGetImageFilePath(Message msg) {
        try {
            //for images
            byte[] bytes = Base64.decode(msg.getBytes(), Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            File image = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
            FileOutputStream os = new FileOutputStream(image, true);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            if (msg.getMimeType().equals(Constants.mimeType.PNG)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            }

            os.write(bos.toByteArray());
            os.flush();
            os.close();

            return image.toURI().toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String storeAndGetDocumentFilePath(Message msg) {
        try {
            byte[] bytes = Base64.decode(msg.getBytes(), Base64.DEFAULT);
            File doc = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
            FileOutputStream os = new FileOutputStream(doc, true);
            os.write(bytes);
            os.flush();
            os.close();
            return doc.toURI().toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String storeAndGetAudioFilePath(Message msg) {
        try {
            byte[] bytes = Base64.decode(msg.getBytes(), Base64.DEFAULT);
            File aud = new File(Utils.getExternalFilePath(msg.getId(), msg.getMimeType()));
            FileOutputStream os = new FileOutputStream(aud, true);
            os.write(bytes);
            os.flush();
            os.close();
            return aud.toURI().toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private byte[] readFileBytes(Context context, Uri uri) throws IOException {

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

    private byte[] readImageBytes(Context context, Uri uri) throws IOException {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(PathUtil.getPath(context, uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
//        result = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return imageBytes;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static void deleteFile(Context context, Uri uri){
        try{
            File file = new File(PathUtil.getPath(context, uri));
            if(file.exists()){
                file.delete();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
