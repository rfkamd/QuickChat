package co.energenes.quikchat.Utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rfkamd on 8/17/2017.
 */

public class AudioPlayer {

    private static final String TAG = AudioPlayer.class.getSimpleName();

    private TextView txtTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlay;
    private ImageButton btnPause;

    private static LibVLC mLibVLC = null;
    private static MediaPlayer mediaPlayer = null;
    private Media media;
    private String path;

    private Context context;
    private static AudioPlayer instance;

    private long position = 0;

    private List<HashMap<String, Long>> list;


    //region view setters
    public void setTotalTimeView(TextView txtTotalTime) {
        this.txtTotalTime = txtTotalTime;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public void setPlayButton(ImageButton btnPlay) {
        this.btnPlay = btnPlay;
    }

    public void setPauseButton(ImageButton btnPause) {
        this.btnPause = btnPause;
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
    }

    //endregion view setters


    private AudioPlayer(Context context){
        this.context = context;
        this.mLibVLC = new LibVLC(context);
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer(mLibVLC);
            mediaPlayer.setEventListener(listener);
        }
    }


    public static AudioPlayer getInstance(Context context){
        if(instance == null)
            instance = new AudioPlayer(context);
        return instance;
    }


    private MediaPlayer.EventListener listener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type){
                case MediaPlayer.Event.EndReached:
                    position = 0;
                    seekBar.setProgress(0);
                    setPlayable();

//                    int totalDuration = (int)Utils.getMediaDuration(context, uri);
//                    String duration = Utils.getFormattedMediaDuration(totalDuration);
//                    seekBar.setMax(totalDuration);
//                    txtFullTime.setText(duration);

                    return;
                case MediaPlayer.Event.PositionChanged:
                    float total  = mediaPlayer.getTime();
                    updatePlaytime((int)mediaPlayer.getTime());
                    seekBar.setProgress((int)total);
                    position = mediaPlayer.getTime();
                    return;
                case MediaPlayer.Event.Opening:
                    //todo get last position here
                    setTotalTime();
                    initMediaSeekBar();
                    return;

//                case MediaPlayer.Event.MediaChanged:
//                    setTotalTime();
////                    seekBar.setMax((int)media.getDuration());
//                    return;
            }

        }
    };



    public void init(String path){

        if(path.equals(this.path)){
            if(position != 0){
                return;
            }
            setPlayable();

            this.path = path;
            Uri uri = null;
            try {
                uri = Uri.fromFile(new File(PathUtil.getPath(context, Uri.parse(path))));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            media = new Media(mLibVLC, uri);
            mediaPlayer.setMedia(media);

            return;
        }else{
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
            if((seekBar != null) && (seekBar.getProgress() > 0)){
                seekBar.setProgress(0);
            }
            position = 0;
            //todo save current state of current media player here
            //postion id and path should be saved in a list
        }




//        position = seekBar.getProgress();

        setPlayable();

        this.path = path;
        Uri uri = null;
        try {
            uri = Uri.fromFile(new File(PathUtil.getPath(context, Uri.parse(path))));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        media = new Media(mLibVLC, uri);
        mediaPlayer.setMedia(media);
    }

    public void play(){

        mediaPlayer.setTime(position);
        mediaPlayer.play();
        setPausable();
    }

    public void pause() {

        if (mediaPlayer == null) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setPlayable();
        }
    }

    private void setPlayable() {
        if (btnPlay != null) {
            btnPlay.setVisibility(View.VISIBLE);
        }

        if (btnPause != null) {
            btnPause.setVisibility(View.GONE);
        }
    }

    private void setPausable() {
        if (btnPlay != null) {
            btnPlay.setVisibility(View.GONE);
        }

        if (btnPause != null) {
            btnPause.setVisibility(View.VISIBLE);
        }
    }

    private void setTotalTime() {

        if (txtTotalTime == null) {
            return;
        }

        StringBuilder playbackStr = new StringBuilder();
        long totalDuration = 0;

        // by this point the media player is brought to ready state
        // by the call to init().
        if (mediaPlayer != null) {
            try {
                totalDuration = mediaPlayer.getLength();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (totalDuration < 0) {
            throw new IllegalArgumentException();
        }

        // set total time as the audio is being played
        if (totalDuration != 0) {
            playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
        }

        txtTotalTime.setText(playbackStr);
    }

    private void initMediaSeekBar() {

        if (seekBar == null) {
            return;
        }

        // update seekbar
        long finalTime = mediaPlayer.getLength();
        seekBar.setMax((int) finalTime);

        seekBar.setProgress((int)position);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
//                    mediaPlayer.setMedia(media);
//                    mediaPlayer.play();
                    position = seekBar.getProgress();
                    mediaPlayer.setTime(seekBar.getProgress());
                    mediaPlayer.play();
//                    mediaPlayer.setPosition(seekBar.getProgress());
//                    updatePlaytime(seekBar.getProgress());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });
    }

    private void updatePlaytime(int currentTime) {

        if (txtTotalTime == null) {
            return;
        }

        if (currentTime < 0) {
            throw new IllegalArgumentException();
        }

        StringBuilder playbackStr = new StringBuilder();

        // set the current time
        // its ok to show 00:00 in the UI
        playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

        playbackStr.append("/");

        // show total duration.
        long totalDuration = 0;

        if (mediaPlayer != null) {
            try {
                totalDuration = media.getDuration();//mediaPlayer.getLength();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // set total time as the audio is being played
        if (totalDuration != 0) {
            playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
        } else {
            Log.w(TAG, "Something's strange, this audio track duration is zero");
        }

        txtTotalTime.setText(playbackStr);
    }


//    public long getMediaDuration(Uri uri){
//        File file = new File(uri.getPath());
//        media = new Media(mLibVLC, uri);
//        mediaPlayer.setMedia(media);
//        long duration =  mediaPlayer.getLength();
//        mediaPlayer.release();
//        return duration;
//    }

}
