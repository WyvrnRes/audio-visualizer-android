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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer
import com.gauravk.audiovisualizersample.R
import com.gauravk.audiovisualizersample.utils.AudioPlayer

class BlastActivity : AppCompatActivity() {

    private lateinit var mVisualizer: BlastVisualizer
    private lateinit var mAudioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blast)

        mVisualizer = findViewById(R.id.blast)
        mAudioPlayer = AudioPlayer()
    }

    override fun onStart() {
        super.onStart()
        startPlayingAudio(R.raw.sample)
    }

    override fun onStop() {
        super.onStop()
        stopPlayingAudio()
    }

    private fun startPlayingAudio(resId: Int) {
        mAudioPlayer.play(this, resId, object : AudioPlayer.AudioPlayerEvent {
            override fun onCompleted() {
                mVisualizer.hide()
            }
        })
        val audioSessionId = mAudioPlayer.audioSessionId
        if (audioSessionId != -1) {
            mVisualizer.setAudioSessionId(audioSessionId)
        }
    }

    private fun stopPlayingAudio() {
        mAudioPlayer.stop()
        mVisualizer.release()
    }
}
