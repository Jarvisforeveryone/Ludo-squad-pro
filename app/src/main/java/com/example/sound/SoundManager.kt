package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundManager(private val context: Context) {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    private var soundDiceRollId: Int = 0
    private var soundPieceMoveId: Int = 0
    private var soundPieceCaptureId: Int = 0
    private var soundGameWinId: Int = 0

    var isSoundEnabled: Boolean = true

    init {
        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundDiceRollId = soundPool.load(context, R.raw.sound_dice_roll, 1)
            soundPieceMoveId = soundPool.load(context, R.raw.sound_piece_move, 1)
            soundPieceCaptureId = soundPool.load(context, R.raw.sound_piece_capture, 1)
            soundGameWinId = soundPool.load(context, R.raw.sound_game_win, 1)
        } catch (_: Exception) {
            // Fallback gracefully if resource loading fails
        }
    }

    fun playDiceRoll() {
        if (!isSoundEnabled || soundDiceRollId == 0) return
        soundPool.play(soundDiceRollId, 1f, 1f, 1, 0, 1f)
    }

    fun playPieceMove() {
        if (!isSoundEnabled || soundPieceMoveId == 0) return
        soundPool.play(soundPieceMoveId, 0.9f, 0.9f, 1, 0, 1f)
    }

    fun playPieceCapture() {
        if (!isSoundEnabled || soundPieceCaptureId == 0) return
        soundPool.play(soundPieceCaptureId, 1f, 1f, 2, 0, 1f)
    }

    fun playGameWin() {
        if (!isSoundEnabled || soundGameWinId == 0) return
        soundPool.play(soundGameWinId, 1f, 1f, 3, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
