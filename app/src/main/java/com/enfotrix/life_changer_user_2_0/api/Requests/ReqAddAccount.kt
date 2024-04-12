package com.enfotrix.life_changer_user_2_0.api.Requests

data class ReqAddAccount(

    var type: String,// user ,nominee_account ,admin_account
    var bank_name: String,
    var account_tittle: String,
    var account_number: String

)
