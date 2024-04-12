package com.enfotrix.life_changer_user_2_0.Models

import com.google.firebase.Timestamp


data class TransactionModel @JvmOverloads constructor(




    var id: String = "",
    var amount: Int = 0,
    var user_id: String = "",
    var new_balance: Int = 0,
    var previous_balance: Int = 0,
    var receiver_account_id: String = "",
    var sender_account_id: String = "",
    var status: String = "",
    var type: String = "",
    var updated_at: String = "",
    var created_at: String = "",




)