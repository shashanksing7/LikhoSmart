import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import nishkaaminnovations.com.likhosmart.WorkShop.MusicPlayer.setWaveComplete

object AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: Uri? = null
    private var waveListener: setWaveComplete? = null
    private lateinit var PreparedListener:playerPreparedListener

    // Play the selected audio
    fun playAudio(context: Context, audioPath: Uri, onCompletion: (() -> Unit)? = null) {
        try {
            // Check if the same audio is already playing
            if (currentAudioPath != audioPath) {
                // Reset MediaPlayer for a new file
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer()
                } else {
                    mediaPlayer?.reset()
                }
                // Set the data source and prepare
                mediaPlayer?.setDataSource(context, audioPath)
                mediaPlayer?.setOnPreparedListener {
                    it.start() // Start playback when prepared
                    PreparedListener.playerPrepared(true)
                }
                mediaPlayer?.setOnCompletionListener {
                    onCompletion?.invoke() // Notify on completion
                    waveListener?.isWaveComplete(true)
                }
                mediaPlayer?.prepareAsync() // Use async preparation to avoid blocking
                currentAudioPath = audioPath
            } else {
                // Resume if the same audio is paused
                resumeAudio()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Pause the currently playing audio
    fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    // Resume playback of the current audio
    fun resumeAudio() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    // Stop playback and release resources
    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentAudioPath = null
    }

    // Check if audio is playing
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun setOnComplete(waveListener: setWaveComplete) {
        this.waveListener = waveListener
    }


    interface playerPreparedListener{
        fun playerPrepared(isPrepared:Boolean)
    }

    fun setPlayerPreparedListener(playerPreparedListener: playerPreparedListener){
      PreparedListener=playerPreparedListener
    }
}
