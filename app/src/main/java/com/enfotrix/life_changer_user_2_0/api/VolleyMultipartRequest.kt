package com.enfotrix.life_changer_user_2_0.api

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException

abstract  class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, mErrorListener) {

    private val mMultipartBody = ByteArrayOutputStream()
    private val mBoundary: String = "----VolleyBoundary" + System.currentTimeMillis()

    override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        // Add any headers here if needed
        return headers
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data; boundary=$mBoundary"
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // Populate the multipart form data
            dos.write(mMultipartBody.toByteArray())
            dos.writeBytes("\r\n--$mBoundary--\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, null)
    }
    abstract fun getByteData(): MutableMap<String, DataPart>


}
