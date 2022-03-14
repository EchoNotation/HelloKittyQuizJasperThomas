package com.example.hellokittyquiz

import android.util.Log
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {

    public val TAG = "QuizViewModel"
    public var index = 0
    public var numCorrectAnswers = 0
    public var numQuestionsLeft = 4 //This should ideally pull from questions.size in MainActivity.kt
    public var questionsAnswered = BooleanArray(numQuestionsLeft)
    public var cheatedOnQuestion = BooleanArray(numQuestionsLeft)

    init {
        Log.d(TAG, "ViewModel instance created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance destroyed")
    }
}