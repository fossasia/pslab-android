package org.fossasia.pslab.others;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by viveksb007 on 11/7/17.
 */

public class AudioJack {

    /* TODO : Add runtime permission for Recording Audio */
    /* TODO : Output value in buffer would be between -2^16 and 2^16, need to map it too or show its FFT  */

    private static final String TAG = "AudioJack";

    private static final int SAMPLING_RATE = 44100;
    private static final int RECORDING_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord = null;
    private AudioTrack audioTrack = null;
    private int minRecorderBufferSize;
    private int minTrackBufferSize;
    private String io;

    public boolean configurationStatus;

    /*
    * Context to obtain AudioManager Instance and string io to classify if requested input or output.
    * */
    public AudioJack(String io) {
        this.io = io;
        configurationStatus = configure();
    }

    private boolean configure() {
        if ("input".equals(io)) {
            /* Initialize audioRecord */
            minRecorderBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, RECORDING_CHANNEL, RECORDER_AUDIO_ENCODING);
            if (minRecorderBufferSize == AudioRecord.ERROR || minRecorderBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                minRecorderBufferSize = SAMPLING_RATE * 2;
            }
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLING_RATE,
                    RECORDING_CHANNEL,
                    RECORDER_AUDIO_ENCODING,
                    minRecorderBufferSize);
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Audio Record can't be initialized");
                return false;
            }
            audioRecord.startRecording();
        } else {
            /* Initialize audioTrack */
            minTrackBufferSize = AudioTrack.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (minTrackBufferSize == AudioTrack.ERROR || minTrackBufferSize == AudioTrack.ERROR_BAD_VALUE) {
                minTrackBufferSize = SAMPLING_RATE * 2;
            }
            // Using STREAM_MUSIC. So to change amplitude stream music needs to be changed
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLING_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minTrackBufferSize,
                    AudioTrack.MODE_STREAM);
            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e(TAG, "AudioTrack can't be initialized");
                return false;
            }
            audioTrack.play();
        }
        return true;
    }

    public short[] read() {
        /* return captured audio buffer */
        short[] audioBuffer = new short[minRecorderBufferSize / 2];
        audioRecord.read(audioBuffer, 0, audioBuffer.length);
        return audioBuffer;
    }

    public void write(short[] buffer) {
        /* write buffer to audioTrack */
        audioTrack.write(buffer, 0, buffer.length);
    }

    public void release() {
        Log.v(TAG, "AudioJack object released");
        if (audioRecord != null) {
            if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING)
                audioRecord.stop();
            audioRecord.release();
        }
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING)
                audioTrack.stop();
            audioTrack.release();
        }
    }

}
