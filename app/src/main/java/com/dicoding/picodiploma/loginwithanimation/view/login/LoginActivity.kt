package com.dicoding.picodiploma.loginwithanimation.view.login

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
import androidx.appcompat.app.AlertDialog
import com.dicoding.picodiploma.loginwithanimation.data.Result
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityLoginBinding
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyButton
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyEditEmail
import com.dicoding.picodiploma.loginwithanimation.view.myeditbutton.MyEditPass
import com.dicoding.picodiploma.loginwithanimation.view.signup.SignupActivity

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private lateinit var myButton: MyButton
    private lateinit var myEditEmail: MyEditEmail
    private lateinit var myEditPass: MyEditPass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
    }

    private fun setupViewModel() {
        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        loginViewModel.loginResponse.observe(this) { handleLoginResponse(it) }
    }

    private fun setupUI() {
        myButton = binding.loginButton
        myButton.isEnabled = true
        myButton.setEnabledText(getString(R.string.button_enabled_login))
        myButton.setDisabledText(getString(R.string.button_text_disabled))

        myEditEmail = binding.emailEditText
        myEditPass = binding.passwordEditText

        setupView()
        setupAction()
        playAnimation()
        setMyButtonEnable()

        myEditPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun handleLoginResponse(result: Result<LoginResponse>) {
        showLoading(result is Result.Loading)
        if (result is Result.Success) showSuccessDialog()
        else if (result is Result.Error) showErrorDialog()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage(getString(R.string.login_dialog_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                startActivity(
                    Intent(this@LoginActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
                finish()
            }
            create()
            show()
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.login_failed_dialog_title))
            setMessage(getString(R.string.login_failed_dialog))
            create()
            show()
        }
    }

    private fun setupView() {
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

    private fun navigateToSignupScreen() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            binding.apply {
                if (emailEditText.error.isNullOrEmpty() && passwordEditText.error.isNullOrEmpty()) {
                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString().trim()
                    loginViewModel.login(email, password)
                }
            }
        }

        binding.textRegister.setOnClickListener {
            navigateToSignupScreen()
        }
    }

    private fun playAnimation() {
        val viewsToAnimate = arrayOf(
            binding.titleTextView,
            binding.emailTextView,
            binding.emailEditTextLayout,
            binding.passwordTextView,
            binding.passwordEditTextLayout,
            binding.loginButton,
            binding.textRegister
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
        myButton.isEnabled = myEditPass.text?.isNotEmpty() == true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

