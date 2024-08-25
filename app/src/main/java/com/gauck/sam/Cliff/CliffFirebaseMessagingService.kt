package com.gauck.sam.Cliff

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CliffFirebaseMessagingService: FirebaseMessagingService() {
    private var queue: RequestQueue? = null
    override fun onNewToken(token: String) {
        val prefs = this.getSharedPreferences("com.gauck.sam.Cliff", Context.MODE_PRIVATE)

        if (queue == null) {
            queue = Volley.newRequestQueue(this)
        }
        val q = queue!!
        val url = prefs.getString("serverURL", "")
        if (url == "") {
            Toast.makeText(baseContext, "No URL found", Toast.LENGTH_SHORT).show()
            return
        }

        val req = object : StringRequest(
            Method.POST, "$url/registerAndroid",
            { resp ->
                Log.d(TAG, "Registered for notifications!")
                Toast.makeText(baseContext, "Registered for notifications!", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(baseContext, "Failed to register for notifications!", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBody(): ByteArray {
                return token.toByteArray()
            }
        }

        q.add(req)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(baseContext, "Notification recieved!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private const val TAG = "CliffFirebaseMessagingService"
    }
}