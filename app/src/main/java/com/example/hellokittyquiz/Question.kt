package com.example.hellokittyquiz

import androidx.annotation.StringRes

data class Question(@StringRes val questionID: Int, val answer : Boolean)