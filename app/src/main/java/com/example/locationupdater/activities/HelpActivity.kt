package com.example.locationupdater.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.locationupdater.R
import com.example.locationupdater.dao.Hint
import kotlinx.android.synthetic.main.hint_item.*

class HelpActivity : AppCompatActivity() {

    private val TAG = "HelpActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hint_item)

        val sharedPreferences = getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE)
        val isHintShown = sharedPreferences.getBoolean(getString(R.string.isHintShown), false)

        //if hint has been shown earlier move ahead
        if(isHintShown)
            startActivity(Intent(this,MapsActivity::class.java))


        //continue with hints if not shown
        val hintList = ArrayList<Hint>()
        hintList.add(
            Hint(
                "The app will track when you enter and exit the boundaries defined by you." +
                        "We do not save any location's data.",
                0,
                false
            )
        )
        hintList.add(
            Hint(
                "Long press the map on the desired location to insert the boundary.",
                R.drawable.insert_jiofence,
                false
            )
        )
        hintList.add(
            Hint(
                "Enter the radius of the boundary you want to be the safe zone. " +
                        "Press the settings button to close the open tab and save data",
                R.drawable.splash_image2,
                false
            )
        )

        hintList.add(
            Hint(
                "You will be notified each time you entry or exit the boundary defined by you.",
                R.drawable.splash_image2,
                true
            )
        )

        var count = 0
        hintText.text = hintList[count].hint
        ImageHint.setImageResource(hintList[count].ImageId)
        count++
        proceed.setOnClickListener {
            if (count < 4) {
                hintText.text = hintList[count].hint
                ImageHint.setImageResource(hintList[count].ImageId)
                count += 1
                Log.e(TAG, "onCreate: $count")
            } else {
                //save the hint shown value in shared preferences
                with(sharedPreferences.edit()) {
                    putBoolean(getString(R.string.isHintShown), true)
                    apply()
                }
                startActivity(Intent(this, MapsActivity::class.java))
                Log.e(TAG, "onCreate: $count")
            }
        }
    }
}