package com.example.tictactoe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object GameData {
    private var _gameModel :MutableLiveData<GameModel> = MutableLiveData()
    var gameModel: LiveData<GameModel> = _gameModel
    var firstID = ""


    fun saveGameModel(model: GameModel){
        _gameModel.postValue(model)
//        through game id identify the online or offline
        if(model.gameId!="-1"){
            Firebase.firestore.collection("games")
                .document(model.gameId)
                .set(model)
        }

    }
//    automatically open other device game
    fun fetchGameModel(){
        gameModel.value?.apply { 
            if (gameId!="-1"){
                Firebase.firestore.collection("games")
                    .document(gameId)
                    .addSnapshotListener { value, error ->
                        val model = value?.toObject(GameModel::class.java)
                        _gameModel.postValue(model)
                    }
            }
        }
    }

}