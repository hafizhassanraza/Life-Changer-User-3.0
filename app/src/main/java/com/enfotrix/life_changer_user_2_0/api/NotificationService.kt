package com.enfotrix.life_changer_user_2_0.api

import User
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.ui.ActivityNotifications
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService(){

    private val db = Firebase.firestore

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onNewToken(deviceTokenFCM: String) {
        super.onNewToken(deviceTokenFCM)

        // This method will be called whenever a new token is generated or the existing token is refreshed.
        // You can handle the token change here.
        // You can send the new token to your server or perform any other necessary actions.

        // For demonstration purposes, let's just log the token.


        mContext=applicationContext
        sharedPrefManager = SharedPrefManager(mContext)
        constants= Constants()


        if(!sharedPrefManager.getToken().isNullOrEmpty()){

            var user =sharedPrefManager.getUser()
            //user.userdevicetoken=deviceTokenFCM
            db.collection(constants.INVESTOR_COLLECTION).document(sharedPrefManager.getToken())
                .set(user)
        }

        //println("New FCM Token: $token")
    }


    val channelId="AssignId"
    val channelName="com.enfotrix.life_changer_user_2_0"

    //@RequiresApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {


        /*if(message.notification!=null){
            generatePushNotification(
                message.notification!!.title!!,
                message.notification!!.body!!,
            )
        }*/

        Log.d("MYTAG", "onMessageReceived: ${message.data.toString()}")

        val manager=getSystemService(NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)

        val notification=NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.announcement_icon)
            .setAutoCancel(false)
            .setContentIntent(null)
            .build()

        manager.notify(Random.nextInt(),notification)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager)
    {


        val channel=NotificationChannel(channelId,"assignwork",NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description="New work"
                enableLights(true)
            }


        manager.createNotificationChannel(channel)

    }






    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String,message: String):RemoteViews{
        val remoteViews= RemoteViews(
            "com.enfotrix.life_changer_user_2_0",
            R.layout.push_notification
        )
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(com.google.firebase.messaging.R.id.text, message)
        //remoteViews.setImageViewResource( R, R.drawable.icon_account_details)
        return remoteViews
    }

    fun generatePushNotification(title:String, message:String){



        val notificationManager =getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH))
        }

        notificationManager.notify(0,

            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.icon_account_details)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000))
                .setOnlyAlertOnce(true)
                .setContentIntent(
                    PendingIntent.getService(
                        this,
                        0 ,
                        Intent(this,ActivityNotifications::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                //.setContent(getRemoteView(title,message))
                .build()

            )

        /*var builder:NotificationCompat.Builder= NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_account_details)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(
                PendingIntent.getService(
                    this,
                    0 ,
                    Intent(this,ActivityNotifications::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )*/

    }



































}