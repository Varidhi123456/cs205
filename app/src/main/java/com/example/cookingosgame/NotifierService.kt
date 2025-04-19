package com.example.cookingosgame

import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.VibratorManager

class NotifierService {
    fun vibrate(context: Context, durationMillis: Long = 200) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun playSpecificNotificationSound(context: Context) {
        try {
            // Get all notification sounds
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_NOTIFICATION)
            val cursor = manager.cursor

            // Choose which sound to use
            if (cursor.moveToPosition(3)) {  // Change index to select different sounds
                val soundUri = manager.getRingtoneUri(cursor.position)
                val ringtone = RingtoneManager.getRingtone(context, soundUri)
                ringtone.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}