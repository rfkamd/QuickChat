package co.energenes.quikchat.Utilities;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by rfkamd on 7/18/2017.
 */

public class Utils {


    /**
     * Checks for Internet Connectivity
     *
     * @param context Application Context
     * @return true if Internet is available and false if Internet is not available
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * checks if a String is null or not
     *
     * @param text String to check
     * @return true if String is null or empty, false if text is either null or empty
     */
    public static boolean isNullOrEmpty(String text) {
        if (text == null) {
            return true;
        } else if (text.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * get Size in Density Pixels
     *
     * @param res      Resources -> getRecources
     * @param sizeInDp number to convert into density pixels
     * @return number of density pixels
     */
    public static int getSizeInDp(Resources res, int sizeInDp) {
        float scale = res.getDisplayMetrics().density;
        int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
        return dpAsPixels;
    }

    /**
     * compresses image with preserving 80% quality
     *
     * @param filePath image file path to compress
     * @return compressed image file path
     */
    public static String compressImage(String filePath) {


        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private static String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "QuikChat/Media/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        for (int i = 0; i < 500; i++) {
            continue;
        }
        return uriSting;

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static boolean validatePhoneNumber(String phone) {

        String regex = "^((\\+|00)(\\d{1,3})[\\s-]?)?(\\d{10}|\\d{9})$";
        //String str = "+966-580255946";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phone);
        if (m.matches()) {
            System.out.println("Country = " + m.group(3));
            System.out.println("Data = " + m.group(4));
            return true;
        } else {
            return false;
        }

    }

    /**
     * checks whether a service is running or not
     *
     * @param context      application context
     * @param serviceClass service class which needs to be checked
     * @return true if service is running else false
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if GPS/Location enabled
     *
     * @param context application context
     * @return true if gps is enabled, false otherwise
     */
    public static boolean isLocationEnabled(final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            return false;
            // notify user
//            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
//            dialog.setMessage("GPS Not Enabled");
//            dialog.setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    context.startActivity(myIntent);
//                    paramDialogInterface.dismiss();
//                }
//            });
//            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    paramDialogInterface.dismiss();
//                }
//            });
//            dialog.show();
        } else {
            return true;
//            startService(intent);
//            prefs.saveKey(ApplicationPrefs.IS_SERVICE_RUNNING, true);
//            Toast.makeText(MainActivity.this,"Service Started",Toast.LENGTH_LONG).show();
//            refreshActivity();
        }
    }

    /**
     * gets Image path on storage for the URI provided
     *
     * @param activity   context
     * @param contentUri image uri to get image path for
     * @return Image Path String
     */
    public static String getImagePathFromURI(Context activity, Uri contentUri) {

        Cursor cursor = activity.getContentResolver().query(contentUri, null, null,
                null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id
                .substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = activity.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Images.Media._ID + " = ? ",
                        new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor
                .getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;

    }

    /**
     * makes and return camera or image selecion intent with camera saving image in a particular path
     *
     * @param context   application context
     * @param mPhotoUri path to save image
     * @return camera or image selecion intent with camera saving image in a particular path
     */
    public static Intent getImageIntent(Context context, Uri mPhotoUri) {
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

//        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "walem");
//        root.mkdirs();
//        String date = new SimpleDateFormat("ddMMyyyy_hhmmss").format(new Date());
//        final String fname = "image_" + userId + "_" + date + ".jpg";
//        final File sdImageMainDirectory = new File(root, fname);
//        mPhotoUri = Uri.fromFile(sdImageMainDirectory);

        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
//            mPhotoUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

//        startActivityForResult(chooserIntent, 1);

        return chooserIntent;
    }

    /**
     * returns current Date and time in ddMMyyyy_hhmmss pattern
     *
     * @return formatted date time string
     */
    public static String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy_hh:mm:ss").format(new Date());
    }

    /**
     * returns current Date and time in milliseconds
     *
     * @return miliseconds
     */
    public static long getCurrentDateInMillis() {
        return new Date().getTime();
    }

    /*
    * Check if version is marshmallow and above.
    * Used in deciding to ask runtime permission
    * */
    public static boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    /**
     * returns current Date and time in provided pattern
     *
     * @param pattern pattern to format date
     * @return formatted date time string
     */
    public static String getFormattedDate(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * returns provided Date and time in provided pattern
     *
     * @param pattern pattern to format date into default is ddMMyyyy_hhmmss
     * @param date    date to be formatted
     * @return formatted date time string
     */
    public static String getFormattedDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }


    public static long getFormattedDateInMillis(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy_hh:mm:ss");
        try {
            return format.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * returns current date time in millis
     * @return date time in millis
     */
    public static long getCurrentDateTimeInMillis() {
        return new Date().getTime();
    }

    /**
     * sorts and returns a Conversation Id for a chat session
     *
     * @param user1 string user id 1
     * @param user2 string user id 2
     * @return String Sorted Conversation Id
     */
    public static String getConvoId(String user1, String user2) {
        String[] ids = {user1, user2};//{user1,"-", user2};
        Arrays.sort(ids);
        String mConvoId = ids[0] + "-" + ids[1];
        return mConvoId;
    }

    /**
     * Generates and return Unique UUID
     * @return Unique UUID String
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }

    /**
     * stops a service if It is running
     *
     * @param context      application context
     * @param serviceClass service class which needs to be checked
     */
    private void stopService(Context context, Class<?> serviceClass) {
        if (isServiceRunning(context, serviceClass)) {
            Toast.makeText(context, "Service is running!! Stopping...", Toast.LENGTH_LONG).show();
            context.stopService(new Intent(context, serviceClass));
        } else {
            Toast.makeText(context, "Service not running", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * gets Audio message file path for given message unique uuid
     *
     * @param uuid string unique uuid for message
     * @return String return audio message path for given uuid
     */
    public static String getInternalFilePath(Activity activity, String uuid, String mimeType) {
        return activity.getFilesDir().getAbsolutePath() + "/" + uuid + getFileExtByMimeType(mimeType);
    }

    /**
     * generates Audio message file path for given message unique uuid
     *
     * @param uuid string unique uuid for message
     * @return String returns generate audio message path for given uuid
     */
    public static String getExternalFilePath(String uuid, String mimeType) {
        if(mimeType.equals(Constants.mimeType.DOCUMENT)){
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "QuikChat/Documents");
            if (!file.exists()) {
                file.mkdirs();
            }
            File filePath = new File(file.getAbsolutePath() + File.separator + uuid + getFileExtByMimeType(mimeType));
            return filePath.getAbsolutePath();
        }
        if(mimeType.equals(Constants.mimeType.BITMAP) || mimeType.equals(Constants.mimeType.PNG) || mimeType.equals(Constants.mimeType.JPEG)) {
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "QuikChat/Media/Images");
            if (!file.exists()) {
                file.mkdirs();
            }
            File filePath = new File(file.getAbsolutePath() + File.separator + uuid + getFileExtByMimeType(mimeType));
            return filePath.getAbsolutePath();
        }
        if(mimeType.equals(Constants.mimeType.AUDIO_MPEG4)){
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "QuikChat/Media/Audio");
            if (!file.exists()) {
                file.mkdirs();
            }
            File filePath = new File(file.getAbsolutePath() + File.separator + uuid + getFileExtByMimeType(mimeType));
            return filePath.getAbsolutePath();
        }
        return null;
    }

    /**
     * Reads File And returns File's bytes as base64 string to transport over json
     * @param path String path to file
     * @return base64 string
     */
    public static String readFileAsBase64String(String path) {
        try {
            InputStream is = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = is.read(buffer)) > -1) {
                    b64os.write(buffer, 0, bytesRead);
                }
                return baos.toString();
            } catch (IOException e) {
                Log.e(TAG, "Cannot read file " + path, e);
                // Or throw if you prefer
                return "";
            } finally {
                closeQuietly(is);
                closeQuietly(b64os); // This also closes baos
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found " + path, e);
            // Or throw if you prefer
            return "";
        }
    }


    public static String readFileAsBase64String(Context context, Uri uri) {
        String file= null;
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
                // Or throw if you prefer
                file = "";
            } finally {
                closeQuietly(is);
                closeQuietly(b64os); // This also closes baos
            }
        } catch (FileNotFoundException e) {
            // Or throw if you prefer
            file = "";
        }
        return file;
    }


    public static String convertBytesToBase64String(byte[] buffer) {
        String str= null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
        try {

            b64os.write(buffer);
            str =  baos.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeQuietly(b64os);
        }
        return str;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }

    /**
     * returns extension for a file by mimtype
     * @param mimeType mimtype for a file
     * @return extension like .pdf,  .jpg
     */
    public static String getFileExtByMimeType(String mimeType){
        String ext = "." + mimeType.split("/")[1];
        return ext;
    }


    public static long getMediaDuration(Context context, Uri uri){
        MediaPlayer mp = MediaPlayer.create(context, uri);
        int totalDuration = 0;
        if(mp != null){
            totalDuration = mp.getDuration();
//            holder.playBar.setMax(totalDuration);
//            holder.txtFullTime.setText(duration);
            mp.reset();
            mp.release();
        }
        return totalDuration;
    }

    public static String getFormattedMediaDuration(long totalDuration){
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration)));
    }

}
