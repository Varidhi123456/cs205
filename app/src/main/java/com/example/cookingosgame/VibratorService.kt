package com.example.cookingosgame

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

class VibratorService {
    fun vibrate(context: Context, durationMillis: Long = 200) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}