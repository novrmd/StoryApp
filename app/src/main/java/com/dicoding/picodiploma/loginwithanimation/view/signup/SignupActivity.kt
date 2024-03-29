package com.dicoding.picodiploma.loginwithanimation.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.dicoding.picodiploma.loginwithanimation.data.Result
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySignupBinding
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyButton
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyEditEmail
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyEditPass

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var signUpViewModel: SignUpViewModel

    private lateinit var myButton: MyButton
    private lateinit var myEditEmail: MyEditEmail
    private lateinit var myEditPass: MyEditPass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        signUpViewModel = ViewModelProvider(this, factory)[SignUpViewModel::class.java]

        signUpViewModel.registerResponse.observe(this) {
            when (it) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    AlertDialog.Builder(this).apply {
                        setTitle("Yeah!")
                        setMessage(getString(R.string.register_dialog_message))
                        setCancelable(false)
                        setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                            val intent = Intent(context, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        create()
                        show()
                    }
                }
                is Result.Error -> {
                    registerFailedToast()
                    showLoading(false)
                }
            }
        }

        myButton = binding.signupButton
        myButton.isEnabled = true
        myButton.setEnabledText(getString(R.string.button_enabled_register))
        myButton.setDisabledText(getString(R.string.button_text_disabled))

        myEditEmail = binding.emailEditText
        myEditPass = binding.passwordEditText

        setupView()
        setupAction()
        playAnimation()

        setMyButtonEnable()

        myEditPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            binding.apply {
                if (nameEditText.error.isNullOrEmpty() && emailEditText.error.isNullOrEmpty() && passwordEditText.error.isNullOrEmpty()) {
                    val name = nameEditText.text.toString().trim()
                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString().trim()
                    signUpViewModel.register(name, email, password)
                } else {
                    registerFailedToast()
                }
            }
        }
    }
    private fun playAnimation() {
        val viewsToAnimate = arrayOf(
            binding.titleTextView,
            binding.nameTextView,
            binding.nameEditTextLayout,
            binding.emailTextView,
            binding.emailEditTextLayout,
            binding.passwordTextView,
            binding.passwordEditTextLayout,
            binding.signupButton
        )
        val animationDuration = 100L
        val animatorSet = AnimatorSet()
        val animatorList = viewsToAnimate.map { view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 1f).setDuration(animationDuration)
        }

        animatorSet.playSequentially(*animatorList.toTypedArray())
        animatorSet.startDelay = 100
        animatorSet.start()
    }

    private fun setMyButtonEnable() {
        val result = myEditPass.text
        myButton.isEnabled = result != null && result.toString().isNotEmpty()

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun registerFailedToast() {
        Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show()
    }

}