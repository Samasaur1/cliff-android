package com.gauck.sam.Cliff

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gauck.sam.Cliff.CliffFirebaseMessagingService.Companion
import com.gauck.sam.Cliff.ui.theme.CliffTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import org.w3c.dom.Text

class MainActivity : ComponentActivity() {
    private var queue: RequestQueue? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CliffTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CliffApp { str ->
                        this.getSharedPreferences("com.gauck.sam.Cliff", Context.MODE_PRIVATE)
                            .edit()
                            .putString("serverURL", str)
                            .apply()
                        askNotificationPermission()

                        Firebase.messaging.token.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                return@addOnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = task.result

                            // Log and toast
                            Log.d(TAG, "TOKEN: $token")

                            if (queue == null) {
                                queue = Volley.newRequestQueue(this)
                            }
                            val q = queue!!

                            val req = object : StringRequest(
                                Method.POST, "$str/registerFCM",
                                { resp ->
                                    Log.d(TAG, "Registered for notifications!")
                                    Toast.makeText(baseContext, "Registered for notifications!", Toast.LENGTH_SHORT).show()
                                }, { e ->
                                    Toast.makeText(baseContext, "Failed to register for notifications!", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Error: ${e.message}")
                                }
                            ) {
                                override fun getBody(): ByteArray {
                                    return token.toByteArray()
                                }
                            }

                            q.add(req)
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun TextFieldAndButton(onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    var serverURL by remember { mutableStateOf( "") }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(value = serverURL, onValueChange = { serverURL = it })

        Button(
            onClick = {
                onClick(serverURL)
            },
        ) {
            Text(text = "Register for notifications")
        }
    }
}

@Composable
fun CliffApp(onClick: (String) -> Unit) {
    TextFieldAndButton(onClick = onClick,
        modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}