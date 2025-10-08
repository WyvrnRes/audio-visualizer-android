/*
        Copyright 2018 Gaurav Kumar

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.gauravk.audiovisualizersample.ui

import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer
import com.gauravk.audiovisualizersample.R
import java.io.IOException

class MusicStreamActivity : AppCompatActivity() {

    private lateinit var imgButton: ImageButton
    private lateinit var mVisualizer: BlastVisualizer
    private lateinit var mediaPlayer: MediaPlayer

    private val stream = "http://stream.radioreklama.bg/radio1rock128"

    private var prepared = false
    private var started = false
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_stream)

        imgButton = findViewById(R.id.playbtn)
        imgButton.setImageResource(R.drawable.playbtn)
        mVisualizer = findViewById(R.id.blast)

        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        val audioSessionId = mediaPlayer.audioSessionId
        if (audioSessionId != AudioManager.ERROR) {
            mVisualizer.setAudioSessionId(mediaPlayer.audioSessionId)
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        PlayerTask().execute(stream)

        imgButton.setOnClickListener {
            if (started) {
                started = false
                mediaPlayer.pause()
                imgButton.setImageResource(R.drawable.playbtn)
            } else {
                started = true
                mediaPlayer.start()
                mediaPlayer.setOnPreparedListener { mp -> mp.start() }
                imgButton.setImageResource(R.drawable.pausebtn)
            }
        }
    }

    private inner class PlayerTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg strings: String): Boolean {
            try {
                mediaPlayer.setDataSource(strings[0])
                mediaPlayer.prepare()
                prepared = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return prepared
        }

        override fun onPostExecute(aBoolean: Boolean) {
            super.onPostExecute(aBoolean)

            if (progressDialog.isShowing) {
                progressDialog.cancel()
            }
            imgButton.setImageResource(R.drawable.playbtn)
        }
    }

    override fun onPause() {
        super.onPause()
        if (started) {
            mediaPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (started) {
            mediaPlayer.setOnPreparedListener { mp -> mp.start() }
            mediaPlayer.prepareAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (prepared) {
            mediaPlayer.release()
        }
        mVisualizer.release()
    }
}
