package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityEditNomineeBinding
import com.google.gson.Gson

class ActivityEditNominee : AppCompatActivity() {
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityEditNomineeBinding
    private lateinit var repo: Repo
    private lateinit var user: ModelUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNomineeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityEditNominee
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        val userJson = intent.getStringExtra("user_model_json")
         user = Gson().fromJson(userJson, ModelUser::class.java)








    }
}