package com.example.ProjectMonocerotis;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    public static volatile MediaRecorder mRecObj = new MediaRecorder();
    public static volatile AudioRecord aRecObj;
    public static volatile AudioTrack aTrkObj;

    public static volatile int rec_aSrcInt =  MediaRecorder.AudioSource.MIC;
    public static volatile int rec_smpRteInt = 44100;
    public static volatile int rec_numOfChanInt = AudioFormat.CHANNEL_IN_DEFAULT;
    public static volatile int rec_aformatInt = AudioFormat.ENCODING_PCM_16BIT;
    public static volatile int rec_bufferSizeInt = 15500;

    public static volatile int  ply_streamType = AudioManager.STREAM_SYSTEM;
    public static volatile int ply_smplRateHz = 0;
    public static volatile int ply_numOfChanInt = 2;
    public static volatile int ply_aFormatInt = AudioFormat.ENCODING_PCM_8BIT;
    public static volatile int ply_bufferSizeInt = 15500;
    public static volatile AudioFormat ply_audioFormat;

    public static volatile byte[] tempBuffer = new byte[3100];
    public static volatile byte[] workingBuffer;
    public static volatile ByteArrayOutputStream bOutStream;
    public static volatile ByteArrayInputStream bInStream;
    public static volatile Thread workerThread1;
    public static volatile Thread workerThread2;

    public static volatile boolean isRecording;
    public static volatile boolean isNotEmpty;

    public static volatile int endOfFile = 0;

    public static volatile int numBytesRet = 0;

    public static volatile TextView statusText;

    public static volatile PlaybackParams pbParms;

    public static volatile Spinner selectionSpinner;
    public static volatile String selectionText;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = (TextView) findViewById(R.id.textView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pbParms = new PlaybackParams();
        }
        this.selectionSpinner = (Spinner) findViewById(R.id.spinner);
        this.init_aFormat();
        this.init_Snd_Sytem();

    }

    public static void startRecording(View view) {
        init_aFormat();
        init_Snd_Sytem();
        isRecording = true;
        workerThread1 = new Thread(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                bOutStream = new ByteArrayOutputStream();
                try {
                    bOutStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                aRecObj.startRecording();
                while(isRecording){
                    numBytesRet = aRecObj.read(tempBuffer, 0, tempBuffer.length, AudioRecord.READ_BLOCKING);
                    try {
                        bOutStream.write(tempBuffer);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        workerThread1.start();


    }


    public static void stopRecording(View view){

        stopCapture();

    }


    public static void playRecording(View view){
        stopCapture();
        playBuffer();
    }

    public static void playBuffer(){
         workerThread2 = new Thread(){
             @RequiresApi(api = Build.VERSION_CODES.M)
             @Override
             public void run() {
                 isNotEmpty = true;
                 workingBuffer = bOutStream.toByteArray();
                 bInStream = new ByteArrayInputStream(workingBuffer);
                 selectionText = selectionSpinner.getSelectedItem().toString();
                 switch(selectionText){

                    case "High":
                        pbParms.setPitch(2f);
                        aTrkObj.setPlaybackParams(pbParms);
                     break;

                     case "Normal":
                         pbParms.setPitch(1f);
                         aTrkObj.setPlaybackParams(pbParms);
                         break;
                     case "Low":
                         pbParms.setPitch(.75f);
                         aTrkObj.setPlaybackParams(pbParms);
                    break;
                 }
                 aTrkObj.play();
                while (isNotEmpty){
                    endOfFile = bInStream.read(tempBuffer, 0, tempBuffer.length);
                        aTrkObj.write(tempBuffer,0,tempBuffer.length);
                    if(endOfFile == -1){
                        isNotEmpty = false;
                    }
                }
                aTrkObj.stop();
             }
         };
         workerThread2.start();
    }

    public static void stopCapture(){
        isRecording=false;
        aRecObj.stop();
        //workerThread1.stop();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void init_Snd_Sytem(){
      aRecObj = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_8BIT, rec_bufferSizeInt);
        //aRecObj = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, rec_bufferSizeInt);
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aRecObj  = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build())
                    .build();
        }*/
        //aTrkObj = new AudioTrack(new AudioAttributes.Builder()
        // .setContentType(CONTENT_TYPE_UNKNOWN)
        // .setUsage(USAGE_MEDIA)
        // .build(), ply_audioFormat, ply_numOfChanInt, ply_aFormatInt, ply_bufferSizeInt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aTrkObj = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                            .setSampleRate(4000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(16000)
                    .build();
        }

    }

    public static void init_aFormat(){
        ply_audioFormat =  new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_8BIT).setSampleRate(8000).build();
    }

}