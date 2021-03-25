package es.uc3m.strongkey

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.uc3m.strongkey.ui.SharedPreference

enum class ProviderType{
    BASIC,
    GOOGLE
}


class iniciado : AppCompatActivity() {
    val globalStatus: GlobalStatus = GlobalStatus
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val i = intent
        val extras = i.extras
        val sharedPreference: SharedPreference = SharedPreference(this)
        if (extras!!.containsKey("email")) {
            val something = i.getStringExtra("email")
            if (something != null) {
                globalStatus.mapa.put("email",something)
                sharedPreference.save("EMAIL", something)
            }
        }
        if (extras!!.containsKey("ID")) {
            val something = i.getStringExtra("ID")
            if (something != null) {
                globalStatus.mapa.put("ID",something)
                sharedPreference.save("ID", something)
            }
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.navigation_strongboxes, R.id.navigation_wallet, R.id.navigation_settings
                )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para leer.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 225)
        } else {
            Log.i("Mensaje", "Se tiene permiso para leer!")
        }
    }
}