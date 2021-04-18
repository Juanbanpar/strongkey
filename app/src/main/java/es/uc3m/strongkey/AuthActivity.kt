package es.uc3m.strongkey

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import es.uc3m.strongkey.ui.SharedPreference
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private var identificacion: String="null"
    private var emilio: String="null"
    private var vid:String="null"
    private var resend:String="null"
    private var numeroT:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val executor = ContextCompat.getMainExecutor(this)
        val biometricManager = BiometricManager.from(this)
        val sharedPreference: SharedPreference = SharedPreference(this)
        var eMAIL: String? = sharedPreference.getValueString("EMAIL")
        var iD: String? = sharedPreference.getValueString("ID")


        println(iD)

        if(iD!=null && iD != ""){
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

        findViewById<Button>(R.id.SMS).setOnClickListener{
            var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    //Log.d(TAG, "onVerificationCompleted:$credential")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    //Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }

                    // Show a message and update the UI
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                   // Log.d(TAG, "onCodeSent:$verificationId")

                    // Save verification ID and resending token so we can use them later
                    /*var prefijo:String= findViewById<TextView>(R.id.prefijo).text.toString()
                    if(prefijo.equals(verificationId)){
                        signInWithPhoneAuth()
                    }*/

                }
            }
            numeroT= findViewById<TextView>(R.id.telefono).text.toString()
            //var prefijo:String= findViewById<TextView>(R.id.prefijo).text.toString()
            val options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(numeroT)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            val firebaseAuth = Firebase.auth
            val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings

            //Las dos líneas siguientes son para pruebas en el emulador, deben comentarse para casos reales
            FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(false);
            firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(numeroT, "654321")

            PhoneAuthProvider.verifyPhoneNumber(options)

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

    private fun signInWithPhoneAuthCredential(patata:PhoneAuthCredential){
        val homeIntent = Intent(this, iniciado::class.java).apply {
            putExtra("provider", "BASIC")
            putExtra("ID",numeroT)
        }
        patata.provider
        startActivity(homeIntent)
    }

    private fun signInWithPhoneAuth(){
        val homeIntent = Intent(this, iniciado::class.java).apply {
            putExtra("provider", "BASIC")
        }

        startActivity(homeIntent)
    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            // 2
            .setTitle("Introduzca la huella")
            // 3
            .setSubtitle("Alternativa patrón")
            // 4
            .setDescription("Presione su dedo")
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