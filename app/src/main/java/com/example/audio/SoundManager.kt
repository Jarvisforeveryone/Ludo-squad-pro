package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private var diceRollSoundId: Int = 0
    private var pawnMoveSoundId: Int = 0
    private var pawnCaptureSoundId: Int = 0
    private var gameWinSoundId: Int = 0
    private var isLoaded: Boolean = false

    var isSoundEnabled: Boolean = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        var loadCount = 0
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loadCount++
                if (loadCount >= 4) {
                    isLoaded = true
                }
            }
        }

        diceRollSoundId = soundPool.load(context, R.raw.dice_roll_sound, 1)
        pawnMoveSoundId = soundPool.load(context, R.raw.pawn_move, 1)
        pawnCaptureSoundId = soundPool.load(context, R.raw.pawn_capture, 1)
        gameWinSoundId = soundPool.load(context, R.raw.game_win, 1)
    }

    fun playDiceRoll() {
        if (!isSoundEnabled) return
        soundPool.play(diceRollSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun playPawnMove() {
        if (!isSoundEnabled) return
        soundPool.play(pawnMoveSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
    }

    fun playPawnCapture() {
        if (!isSoundEnabled) return
        soundPool.play(pawnCaptureSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playGameWin() {
        if (!isSoundEnabled) return
        soundPool.play(gameWinSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}
