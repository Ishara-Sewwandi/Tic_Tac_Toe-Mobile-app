package com.example.tictactoe

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tictactoe.databinding.ActivityGameBinding
import android.content.Context.MODE_PRIVATE
import android.widget.ImageView


class GameActivity : AppCompatActivity(),View.OnClickListener{

    lateinit var binding: ActivityGameBinding

    private var gameModel :GameModel? =null
    private var isSoundOn: Boolean = false
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        isSoundOn = sharedPreferences.getBoolean("isSoundOn", true)
        mediaPlayer = MediaPlayer.create(this, R.raw.sounds)
        GameData.fetchGameModel()
        mediaPlayer.start()
        if (isSoundOn){
            mediaPlayer.pause()
        }
        updateSoundIcon(isSoundOn)

        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        binding.startGameBtn.setOnClickListener {
            startGame()
        }

        GameData.gameModel.observe(this){
            gameModel = it
            setUI()
        }

        val isSoundOn = sharedPreferences.getBoolean("isSoundOn", true)
        if (isSoundOn) {
            mediaPlayer.start()
        } else {
            mediaPlayer.pause()
        }

        binding.soundBtn.setOnClickListener {
            toggleSound()
        }

    }

    private fun updateSoundIcon(isSoundOn: Boolean) {
        val imageView = findViewById<ImageView>(R.id.sound_btn)
        if (isSoundOn) {
            imageView.setImageResource(R.drawable.sound_on)
        } else {
            imageView.setImageResource(R.drawable.sound_mute)
        }
    }

    private fun toggleSound() {
        val isSoundOn = sharedPreferences.getBoolean("isSoundOn", true)
        val editor = sharedPreferences.edit()

        // Toggle the sound status and update the icon
        if (isSoundOn) {
            mediaPlayer.pause()
            editor.putBoolean("isSoundOn", false)
            updateSoundIcon(false) // Update the sound icon to mute
            Toast.makeText(this, "Sound muted", Toast.LENGTH_SHORT).show()
        } else {
            mediaPlayer.start()
            editor.putBoolean("isSoundOn", true)
            updateSoundIcon(true) // Update the sound icon to sound on
            Toast.makeText(this, "Sound enabled", Toast.LENGTH_SHORT).show()
        }
        editor.apply()
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    fun setUI(){
        gameModel?.apply {
            binding.btn0.text= filledPos[0]
            binding.btn1.text= filledPos[1]
            binding.btn2.text= filledPos[2]
            binding.btn3.text= filledPos[3]
            binding.btn4.text= filledPos[4]
            binding.btn5.text= filledPos[5]
            binding.btn6.text= filledPos[6]
            binding.btn7.text= filledPos[7]
            binding.btn8.text= filledPos[8]

//            when start the game start button visible
            binding.startGameBtn.visibility = View.VISIBLE

            binding.statusText.text=
                when(gameStatus){
                    GameStatus.CREATED -> {
                        binding.startGameBtn.visibility = View.INVISIBLE
                        "Game ID:"+ gameId
                    }
                    GameStatus.JOINED ->{
                        "Click Start"
                    }
                    GameStatus.INPROGRESS ->{
                        binding.startGameBtn.visibility = View.INVISIBLE
                        when(GameData.firstID) {
                            currentPlayer -> "Your turn"
                            else -> currentPlayer + "turn"
                        }
                    }
                    GameStatus.FINISHED ->
                        if (winner.isNotEmpty()) {
                            when(GameData.firstID){
                                winner -> "You Won"
                                else ->   winner + "Won"
                            }

                        }
                    else "DRAW"
                }
        }
    }

    fun startGame(){
        gameModel?.apply {
            updateGameData(
                GameModel(
                    gameId = gameId,
                    gameStatus =GameStatus.INPROGRESS
                )
            )
        }
    }
    fun updateGameData(model: GameModel){
        GameData.saveGameModel(model)
    }

//    check for winner
    fun checkForWinner(){
        val winningPos = arrayOf(
            intArrayOf(0,1,2),
            intArrayOf(3,4,5),
            intArrayOf(6,7,8),
            intArrayOf(0,3,6),
            intArrayOf(1,4,7),
            intArrayOf(2,5,8),
            intArrayOf(0,4,8),
            intArrayOf(2,4,6),
        )

        gameModel?.apply {
            for (i in winningPos){
                //012 win
                if(
                    filledPos[i[0]] == filledPos[i[1]]&&
                    filledPos[i[1]] == filledPos [i[2]]&&
                    filledPos[i[0]].isNotEmpty()
                ){
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[i[0]]
                }
            }

            if (filledPos.none(){it.isEmpty()}) {
                gameStatus = GameStatus.FINISHED
            }

            updateGameData(this)
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus!= GameStatus.INPROGRESS){
                Toast.makeText(applicationContext,"Game not started",Toast.LENGTH_SHORT).show()
                return
            }
            //game is in  progress

            if (gameId!="-1" && currentPlayer!=GameData.firstID){
                Toast.makeText(applicationContext,"Not your turn",Toast.LENGTH_SHORT).show()
                return
            }


            val clickedPos =(v?.tag as String).toInt()
            if(filledPos[clickedPos].isEmpty()){
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if (currentPlayer=="X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }
        }
    }
}