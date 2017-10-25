package co.energenes.quikchat.network;

/**
 * Created by rfkamd on 8/8/2017.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DownloadImageTask extends AsyncTask<String, Void, String> {

    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected String doInBackground(String... urls) {
        int count;
        String path = "/sdcard/downloadedfile.jpg";
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urls[0])
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            try {
                InputStream input = new BufferedInputStream(response.body().byteStream());

                // Output stream
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
//                        publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

        }
        return path;
    }

    protected void onPostExecute(String result) {
//        bmImage.setImageBitmap(result);
    }

//     new DownloadImageTask(mImageView)
//                .execute("https://pbs.twimg.com/profile_images/61655057/2425718692_1783fe0913_b.jpg");
}

