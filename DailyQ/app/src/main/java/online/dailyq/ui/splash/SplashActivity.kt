package online.dailyq.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import online.dailyq.AuthManager
import online.dailyq.R
import online.dailyq.ui.base.BaseActivity
import online.dailyq.ui.login.LoginActivity
import online.dailyq.ui.main.MainActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(1000)

            if (AuthManager.accessToken.isNullOrEmpty()) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            } else {
                startActivity((Intent(this@SplashActivity, MainActivity::class.java)))
            }
            finish()
        }
    }
}