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
package com.gauravk.audiovisualizer.utils

import android.graphics.Color

object AVConstants {
    const val DEFAULT_DENSITY = 0.25f
    // Modernized default color - using a vibrant modern teal/cyan
    val DEFAULT_COLOR = Color.parseColor("#00BCD4")
    const val DEFAULT_STROKE_WIDTH = 8.0f  // Increased from 6.0f for better visibility
    const val MAX_ANIM_BATCH_COUNT = 4
}
