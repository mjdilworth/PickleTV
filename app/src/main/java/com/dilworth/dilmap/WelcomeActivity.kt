package com.dilworth.dilmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.setPadding

class WelcomeActivity : ComponentActivity() {
    private lateinit var rootLayout: LinearLayout
    private lateinit var titleText: TextView
    private lateinit var demoButton: Button
    private lateinit var signInButton: Button

    private lateinit var signInLayout: LinearLayout
    private lateinit var emailInput: EditText
    private lateinit var submitButton: Button
    private lateinit var backButton: Button

    private var showingSignIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("WelcomeActivity", "onCreate called")

        createWelcomeScreen()
        setContentView(rootLayout)
    }

    private fun createWelcomeScreen() {
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(32))
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF000000.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        titleText = TextView(this).apply {
            text = "Welcome to PickleTV"
            textSize = 48f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(dpToPx(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(48))
            }
        }

        demoButton = Button(this).apply {
            text = "Play Demo Video"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(400),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(16), 0, dpToPx(16))
            }
            setOnClickListener { launchDemoVideo() }
            requestFocus()
        }

        signInButton = Button(this).apply {
            text = "Sign In with Email"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(400),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(16), 0, dpToPx(16))
            }
            setOnClickListener { showSignInScreen() }
        }

        createSignInLayout()

        rootLayout.addView(titleText)
        rootLayout.addView(demoButton)
        rootLayout.addView(signInButton)
        rootLayout.addView(signInLayout)
    }

    private fun createSignInLayout() {
        signInLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = LinearLayout.GONE
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val signInTitle = TextView(this).apply {
            text = "Enter Your Email"
            textSize = 32f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(32), 0, dpToPx(24))
        }

        emailInput = EditText(this).apply {
            hint = "email@example.com"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            textSize = 20f
            setPadding(dpToPx(16))
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(400),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        submitButton = Button(this).apply {
            text = "Sign In"
            textSize = 22f
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(400),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(24), 0, dpToPx(12))
            }
            setOnClickListener { handleSignIn() }
        }

        backButton = Button(this).apply {
            text = "Back"
            textSize = 22f
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(400),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(12), 0, 0)
            }
            setOnClickListener { showWelcomeScreen() }
        }

        val privacyPolicyLink = TextView(this).apply {
            text = "Privacy Policy"
            textSize = 16f
            setTextColor(0xFF00BFFF.toInt()) // Sky blue color for link
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(16), 0, 0)
            setOnClickListener { openPrivacyPolicy() }
            // Make it look like a link
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        }

        signInLayout.addView(signInTitle)
        signInLayout.addView(emailInput)
        signInLayout.addView(submitButton)
        signInLayout.addView(backButton)
        signInLayout.addView(privacyPolicyLink)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showSignInScreen() {
        showingSignIn = true
        titleText.visibility = LinearLayout.GONE
        demoButton.visibility = LinearLayout.GONE
        signInButton.visibility = LinearLayout.GONE
        signInLayout.visibility = LinearLayout.VISIBLE
        emailInput.requestFocus()
        Log.d("WelcomeActivity", "Switched to sign-in screen")
    }

    private fun showWelcomeScreen() {
        showingSignIn = false
        titleText.visibility = LinearLayout.VISIBLE
        demoButton.visibility = LinearLayout.VISIBLE
        signInButton.visibility = LinearLayout.VISIBLE
        signInLayout.visibility = LinearLayout.GONE
        emailInput.text.clear()
        demoButton.requestFocus()
        Log.d("WelcomeActivity", "Switched to welcome screen")
    }

    private fun handleSignIn() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("WelcomeActivity", "Sign-in attempt with email: $email")
        Toast.makeText(this, "Signing in with: $email", Toast.LENGTH_SHORT).show()

        // Proceed to main activity after sign-in
        launchMainActivity(email)
    }

    private fun openPrivacyPolicy() {
        val privacyUrl = getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
        try {
            startActivity(intent)
            Log.d("WelcomeActivity", "Opening privacy policy: $privacyUrl")
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open privacy policy", Toast.LENGTH_SHORT).show()
            Log.e("WelcomeActivity", "Error opening privacy policy", e)
        }
    }

    private fun launchDemoVideo() {
        Log.d("WelcomeActivity", "Launching demo video mode")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("MODE", "DEMO")
        }
        startActivity(intent)
        finish()
    }

    private fun launchMainActivity(email: String) {
        Log.d("WelcomeActivity", "Launching main activity for user: $email")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("MODE", "SIGNED_IN")
            putExtra("EMAIL", email)
        }
        startActivity(intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                if (showingSignIn) {
                    showWelcomeScreen()
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

