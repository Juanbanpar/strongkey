package es.uc3m.strongkey

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import es.uc3m.strongkey.ui.SharedPreference
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private var identificacion: String="null"
    private var emilio: String="null"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val executor = ContextCompat.getMainExecutor(this)
        val biometricManager = BiometricManager.from(this)
        val sharedPreference: SharedPreference = SharedPreference(this)
        var eMAIL: String? = sharedPreference.getValueString("EMAIL")
        var iD: String? = sharedPreference.getValueString("ID")


        println(iD)

        if(iD!=null){
            identificacion=iD!!
            emilio=eMAIL!!
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    authUser(executor)
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Toast.makeText(
                            this,
                            "R.string.error_msg_no_biometric_hardware",
                            Toast.LENGTH_LONG
                    ).show()
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Toast.makeText(
                            this,
                            "R.string.error_msg_biometric_hw_unavailable",
                            Toast.LENGTH_LONG
                    ).show()
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    Toast.makeText(
                            this,
                            "R.string.error_msg_biometric_not_setup",
                            Toast.LENGTH_LONG
                    ).show()
            }
        }


        findViewById<Button>(R.id.googleButton).setOnClickListener {
            //configuracion

            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun showHome(email: String, provider: ProviderType,id:String) {
        val homeIntent = Intent(this, iniciado::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("ID", id)
        }

        startActivity(homeIntent)

    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            // 2
            .setTitle("R.string.auth_title")
            // 3
            .setSubtitle("R.string.auth_subtitle")
            // 4
            .setDescription("R.string.auth_description")
            // 5
            .setDeviceCredentialAllowed(true)
            // 6
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                // 2
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    //main_layout.visibility = View.VISIBLE
                    var provider: ProviderType=ProviderType.GOOGLE
                    showHome(emilio,provider,identificacion)
                    //println("VAMONOS ATOMOS")
                }
                // 3
                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "R.string.error_msg_auth_error, errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // 4
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext,
                        "R.string.error_msg_auth_failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account : GoogleSignInAccount? = task.getResult(ApiException::class.java)

                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if(it.isSuccessful){
                            showHome(account.email!!,ProviderType.GOOGLE, account.id!!)
                        }else{
                            println("ERRORRRRR")
                        }

                    }


                }
            }catch (e : ApiException){

            }



        }
    }
}