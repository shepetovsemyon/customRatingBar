package ru.shepetov.mycustomviewapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRatingBar.setOnScoreChanged {
            Log.d(TAG, "setOnScoreChanged: ${myRatingBar.score}")
        }

        myRatingBar.setOnScoreSubmit {
            Log.d(TAG, "setOnScoreSubmit: ${myRatingBar.score}")
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

