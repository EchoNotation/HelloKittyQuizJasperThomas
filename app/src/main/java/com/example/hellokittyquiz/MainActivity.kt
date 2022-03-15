package com.example.hellokittyquiz

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton : Button
    private lateinit var falseButton : Button
    private lateinit var cheatButton : Button
    private lateinit var previousButton : ImageButton
    private lateinit var nextButton : ImageButton
    private lateinit var textView : TextView
    private lateinit var backgroundLayout : LinearLayout

    private val REQUEST_CODE_CHEAT = 0

    private val questions = listOf(
        Question(R.string.kitty1, true),
        Question(R.string.kitty2, false),
        Question(R.string.kitty3, true),
        Question(R.string.kitty4, true)
    )

    private var numCorrectAnswers = 0
    private var numQuestionsLeft = questions.size
    private var questionsAnswered = BooleanArray(questions.size)
    private var answerState = IntArray(questions.size)

    private var cheatSound = MediaPlayer.create(this,R.raw.cheat)
    private var correctSound = MediaPlayer.create(this,R.raw.correct)
    private var incorrectSound = MediaPlayer.create(this,R.raw.incorrect)

    private var index = 0
    private val TAG = "MainActivity" //Logging identifier for this program

    fun updateQuestion() {
        textView.setText(questions[index].questionID)
        setBackground()
    }

    fun checkAnswer(userAnswer : Boolean) {
        if(questionsAnswered[index]) return //Do not allow multiple attempts at the same question
        questionsAnswered[index] = true

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT

        //Changing gravity doesn't do anything on sufficiently high build numbers, which includes this version running on my phone
        toast.setGravity(Gravity.TOP, 0, 0)

        if(quizViewModel.cheatedOnQuestion[index]) {
            toast.setText(R.string.judgement_toast)
            answerState[index] = 3
            cheatSound.start()
        }
        else if(questions[index].answer == userAnswer) {
            toast.setText(R.string.correct_toast)
            numCorrectAnswers++
            answerState[index] = 1
            correctSound.start()
        }
        else {
            toast.setText(R.string.incorrect_toast)
            answerState[index] = 2
            incorrectSound.start()
        }

        toast.show()

        numQuestionsLeft--
        if(numQuestionsLeft <= 0) {
            val scoreToast = Toast(this)
            scoreToast.duration = Toast.LENGTH_LONG
            scoreToast.setText("Quiz Finished! Your score was " + (( numCorrectAnswers * 100)/ questions.size) + "%")
            scoreToast.show()
        }
    }

    lateinit var quizViewModel : QuizViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val provider : ViewModelProvider = ViewModelProviders.of(this)
        quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "quizViewModel created")

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        cheatButton = findViewById(R.id.cheat_button)
        previousButton = findViewById(R.id.previous_button)
        nextButton = findViewById(R.id.next_button)
        textView = findViewById(R.id.text_view)
        backgroundLayout = findViewById(R.id.background_layout)

        updateQuestion()

        trueButton.setOnClickListener {
            //Do something when the true button is clicked
            checkAnswer(true)
        }
        falseButton.setOnClickListener {
            //Do something when the false button is clicked
            checkAnswer(false)
        }
        cheatButton.setOnClickListener {
            //Need to switch to the cheat_activity layout...
            //Wrap cheat_activity into an Intent.
            val answerIsTrue = questions[index].answer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }
        previousButton.setOnClickListener{
            index -= 1
            if(index < 0) index = 0
            updateQuestion()
        }
        nextButton.setOnClickListener{
            index += 1
            if(index >= questions.size) index = questions.size-1
            updateQuestion()
        }
        textView.setOnClickListener {
            index += 1
            if (index >= questions.size) index = questions.size - 1
            updateQuestion()
        }
    }
    fun setBackground(){
        //0 = Unattempted, 1 = Correct, 2 = Incorrect, 3 = Cheated
        var color = "@color/purple"
        if (answerState[index] == 0){
            color = "@color/white"
        }
        else if(answerState[index] == 1){
            color = "@color/green"
        }
        else if(answerState[index] == 2){
            color = "@color/red"
        }
        else{
            color = "@color/yellow"
        }
        backgroundLayout.setBackgroundColor(Color.parseColor(color))
    }

    //Log processes after creation

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
        //Need to save app state to QuizViewModel
        quizViewModel.index = index
        quizViewModel.numCorrectAnswers = numCorrectAnswers
        quizViewModel.numQuestionsLeft = numQuestionsLeft
        quizViewModel.questionsAnswered = questionsAnswered
        quizViewModel.answerState = answerState
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        //Need to retrieve app state from QuizViewModel
        index = quizViewModel.index
        numCorrectAnswers = quizViewModel.numCorrectAnswers
        numQuestionsLeft = quizViewModel.numQuestionsLeft
        questionsAnswered = quizViewModel.questionsAnswered
        answerState = quizViewModel.answerState
        updateQuestion()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_OK) {
            return
        }
        if(requestCode == REQUEST_CODE_CHEAT) {
            Log.d(TAG, "arrived from cheat screen")
            quizViewModel.cheatedOnQuestion[index] = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }
}