package com.enfotrix.life_changer_user_2_0.Models

import com.google.firebase.Timestamp

data class ModelUser (

    val id: String,
    val name: String,
    val cnic: String,
    val address: String,
    val phone: String,
    val status: String,
    val photo: String,
    val cnic_front: String,
    val cnic_back: String?,
    val fa_id: String?,
    val userdevicetoken: String?,
    val pin: String,
    val created_at: String,
    val updated_at: String,
    val father_name: String?,
    val nominees: ModelNominee,
    val accounts: List<ModelBankAccount>,
    val investment: InvestmentModel


)