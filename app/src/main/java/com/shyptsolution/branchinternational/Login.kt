package com.shyptsolution.branchinternational

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.API_URL
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.util
import org.json.JSONObject

class Login : AppCompatActivity() {
    private val TAG = "LOGIN"
    private lateinit var userNameEditText : EditText
    private lateinit var passWordEditText : EditText
    private lateinit var loginButton : Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = getSharedPreferences("branch-international", AppCompatActivity.MODE_PRIVATE)
        editor =  sharedPreferences.edit()
        supportActionBar?.title = "Login"
        userNameEditText = findViewById(R.id.username)
        passWordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = userNameEditText.text.toString()
            val password = passWordEditText.text.toString()
            if(Patterns.EMAIL_ADDRESS.matcher(username).matches())
            userLogin(username,password)
            else util.notify("Not a valid Username",this)
        }

        // Login Automatically if Info is Saved
        if(System.currentTimeMillis()-sharedPreferences.getLong("lastLoginTime",0)<=7*24*60*60*1000){
            findViewById<ProgressBar>(R.id.LoginProgressBar).visibility = View.VISIBLE
            startActivity(Intent(this,MainActivity::class.java))
        }
        else{
            editor.apply {
                putString("auth_token", "")
                putString("username", "")
                putString("password","")
                apply()
            }
        }

    }

    private fun userLogin(username : String,password : String){
            val progressBar = findViewById<ProgressBar>(R.id.LoginProgressBar)
            val rememberMeCheckBox = findViewById<CheckBox>(R.id.saveLoginInfoCheckBox)
            progressBar.visibility = View.VISIBLE
            val queue = Volley.newRequestQueue(this)
            val jsonBody = """
            {
                "username": $username,
                "password": $password
            }
        """.trimIndent()
            val request = JsonObjectRequest(
                Request.Method.POST, API_URL+"login", JSONObject(jsonBody),
                { response ->
                    progressBar.visibility = View.GONE
                    Log.d(TAG, "Login response: $response")
                        editor.apply {
                            putString("auth_token", response.getString("auth_token"))
                            if(rememberMeCheckBox.isChecked){
                                putString("username", username)
                                putString("password",password)
                                putLong("lastLoginTime",System.currentTimeMillis())
                            }
                            apply()
                        }
                        startActivity(Intent(this,MainActivity::class.java))
                },
                { error ->
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Login error: $error")
                    when (error) {
                        is AuthFailureError -> util.notify("Username or password is invalid.",this)
                        is NoConnectionError -> util.notify("Please connect to Internet.",this)
                        is TimeoutError -> util.notify("Try Logging in again.",this)
                        else -> util.notify("Some error occurred",this)
                    }
                    error.stackTrace
                })
            queue.add(request)
    }

}