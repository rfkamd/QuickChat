package co.energenes.quikchat.views.fragments;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.Closeable;
import java.io.IOException;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Constants;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.views.activities.MapsActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by rfkamd on 8/8/2017.
 */

public class AttachmentFragment extends BottomSheetDialogFragment implements View.OnClickListener{

    private Button btnDocument;
    private Button btnContact;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnAudio;
    private Button btnLocation;
    private String id;

    private int requestCodeContacts = 1024;
    private int requestCodeLocation = 2048;
    private int requestCodeImage = 3072;
    private int requestCodeFile = 4096;
    private Uri photoUri;

    private ResultListener resultListener;
    private String contact;

    public AttachmentFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attachment_sheet, container);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnDocument = (Button) view.findViewById(R.id.btnDocument);
        btnContact = (Button) view.findViewById(R.id.btnContact);
        btnCamera = (Button) view.findViewById(R.id.btnCamera);
//        btnGallery = (Button) view.findViewById(R.id.btnGallery);
//        btnAudio = (Button) view.findViewById(R.id.btnAudio);
        btnLocation = (Button) view.findViewById(R.id.btnLocation);

//        btnGallery.setVisibility(View.GONE);
//        btnAudio.setVisibility(View.GONE);

        btnDocument.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
//        btnGallery.setOnClickListener(this);
//        btnAudio.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDocument:
                openDocument();
                return;
            case R.id.btnContact:
                openContact();
                return;
            case R.id.btnCamera:
                openCamera();
                return;
//            case R.id.btnGallery:
//                openGallery();
//                return;
//            case R.id.btnAudio:
//                openAudio();
//                return;
            case R.id.btnLocation:
                openLocation();
                return;
        }

        this.dismiss();
    }

    private void openDocument(){
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/pdf");
//        String[] mimeTypes = {"image/*", "application/pdf"};
//        intent .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, requestCodeFile);
    }

    private void openContact(){
        Intent i=new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, requestCodeContacts);
    }

    private void openCamera(){
        String uri = Utils.getExternalFilePath( Utils.generateUUID(), Constants.mimeType.JPEG );//Utils.getFileExtByMimeType()
        photoUri = Uri.parse(uri);
        Intent intent  = Utils.getImageIntent(getActivity(), photoUri);
        startActivityForResult(intent, requestCodeImage);
    }

//    private void openGallery(){
//        Toast.makeText(getActivity(), "Gallery", Toast.LENGTH_SHORT).show();
//    }
//
//    private void openAudio(){
//        Toast.makeText(getActivity(), "Audio", Toast.LENGTH_SHORT).show();
//    }

    private void openLocation(){
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        startActivityForResult(intent, requestCodeLocation);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //region contact
        if (requestCode == requestCodeContacts && resultCode == RESULT_OK) {

            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String number = cursor.getString(numberIndex);
            String name = cursor.getString(nameIndex);
            contact = name + "\n" + number;
            if(resultListener != null){
                resultListener.onResult(contact, requestCode);
            }
            cursor.close();
        }
        //endregion contact

        //region location
        if (requestCode == requestCodeLocation && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), data.getStringExtra("location"), Toast.LENGTH_LONG).show();
                LatLng latLng = new LatLng(data.getExtras().getDouble("latitude"),data.getExtras().getDouble("longitude"));
                String loc  = data.getStringExtra("location");
                String location = loc + "\n" + latLng.toString();
                if(resultListener != null){
                    resultListener.onResult(location, requestCode);
                }
            }
        }
        //endregion location

        //region Image
        if (requestCode == requestCodeImage && resultCode == RESULT_OK) {
            String result = null;

            result = processImages(resultCode, data);


            if(resultListener != null){
                resultListener.onResult(result, requestCode);
            }
        }
        //endregion contact

        //region File
        if (requestCode == requestCodeFile && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
//                String result = null;
//                try {
//                    InputStream is = getActivity().getContentResolver().openInputStream(uri);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
//                    byte[] buffer = new byte[8192];
//                    int bytesRead;
//                    try {
//                        while ((bytesRead = is.read(buffer)) > -1) {
//                            b64os.write(buffer, 0, bytesRead);
//                        }
//                        result =  baos.toString();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        closeQuietly(is);
//                        closeQuietly(b64os); // This also closes baos
//                    }
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }

                if(resultListener != null){
                    resultListener.onResult(uri.toString(), requestCode);
                }
            }
        }
        //endregion location

        this.dismiss();

    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }


    private String processImages(int resultCode, Intent data) {
        final boolean isCamera;
        final String action;
        String result = null;
        try {
            if (resultCode == RESULT_OK) {

                if (data == null) {
                    isCamera = true;
                } else {
                    action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (!isCamera) {
                    ClipData clip = data.getClipData();
                    if (clip != null) {
                        photoUri = clip.getItemAt(0).getUri();
                    } else {
                        photoUri = data == null ? null : data.getData();
                    }
                }

                result = photoUri.toString();

//                InputStream is = getActivity().getContentResolver().openInputStream(photoUri);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
//                byte[] buffer = new byte[8192];
//                int bytesRead;
//                try {
//                    while ((bytesRead = is.read(buffer)) > -1) {
//                        b64os.write(buffer, 0, bytesRead);
//                    }
//                    result =  baos.toString();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    closeQuietly(is);
//                    closeQuietly(b64os); // This also closes baos
//                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }


    public static interface ResultListener{
        void onResult(String result, int requestCode);
    }

    public void setOnResultListener(ResultListener resultListener){
        this.resultListener = resultListener;
    }

}
