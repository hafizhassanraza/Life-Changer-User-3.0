package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils

class ActivitySplash : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mContext = this@ActivitySplash
        actionBar?.hide()
        supportActionBar?.hide()
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        Handler().postDelayed({
            navigateToNextScreen()
        }, DELAY_TIME)
    }

    private fun navigateToNextScreen() {
        if (sharedPrefManager.isLoggedIn()) {
            Toast.makeText(mContext, "debug1", Toast.LENGTH_SHORT).show()

            when {
                sharedPrefManager.getStatus() == constants.INVESTOR_STATUS_ACTIVE ||
                        sharedPrefManager.getUser()!!.status == constants.INVESTOR_STATUS_PENDING  -> {
                    startActivity(Intent(mContext, MainActivity::class.java))
                    finish()
                }
                sharedPrefManager.getStatus() == constants.INVESTOR_STATUS_INCOMPLETE -> {
                    startActivity(Intent(mContext, ActivityUserDetails::class.java))
                    finish()
                }
            }
        } else {
            Toast.makeText(mContext, "debug", Toast.LENGTH_SHORT).show()
            startActivity(Intent(mContext, ActivityLogin::class.java))
            finish()
        }
    }

    companion object {
        private const val DELAY_TIME = 1500L
    }
}
