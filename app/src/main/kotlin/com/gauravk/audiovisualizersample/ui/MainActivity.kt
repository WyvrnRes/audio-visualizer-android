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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gauravk.audiovisualizersample.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val PERM_REQ_CODE = 23
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.v_blob_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_blast_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_wave_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_bar_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_stream_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_circle_line_btn).setOnClickListener(this)
        findViewById<View>(R.id.v_hifi_btn).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.v_blob_btn -> {
                if (checkAudioPermission())
                    launchBlobActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_blast_btn -> {
                if (checkAudioPermission())
                    launchBlastActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_wave_btn -> {
                if (checkAudioPermission())
                    launchWaveActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_bar_btn -> {
                if (checkAudioPermission())
                    launchSpikyWaveActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_stream_btn -> {
                if (checkAudioPermission())
                    launchMusicStreamActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_circle_line_btn -> {
                if (checkAudioPermission())
                    launchCircleLineActivity()
                else
                    requestAudioPermission()
            }
            R.id.v_hifi_btn -> {
                if (checkAudioPermission()) {
                    val intent = Intent(this@MainActivity, HiFiActivity::class.java)
                    startActivity(intent)
                } else
                    requestAudioPermission()
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERM_REQ_CODE
        )
    }

    private fun launchBlobActivity() {
        val intent = Intent(this@MainActivity, BlobActivity::class.java)
        startActivity(intent)
    }

    private fun launchBlastActivity() {
        val intent = Intent(this@MainActivity, BlastActivity::class.java)
        startActivity(intent)
    }

    private fun launchWaveActivity() {
        val intent = Intent(this@MainActivity, WaveActivity::class.java)
        startActivity(intent)
    }

    private fun launchSpikyWaveActivity() {
        val intent = Intent(this@MainActivity, BarActivity::class.java)
        startActivity(intent)
    }

    private fun launchMusicStreamActivity() {
        val intent = Intent(this@MainActivity, MusicStreamActivity::class.java)
        startActivity(intent)
    }

    private fun launchCircleLineActivity() {
        val intent = Intent(this@MainActivity, CircleLineActivity::class.java)
        startActivity(intent)
    }
}
