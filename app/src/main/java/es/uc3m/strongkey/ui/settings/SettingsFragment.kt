package es.uc3m.strongkey.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.Interfaz
import es.uc3m.strongkey.R
import es.uc3m.strongkey.databinding.FragmentSettingsBinding
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.ui.SharedPreference
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Security
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel
    lateinit var globalStatus: GlobalStatus
    var clave: String = ""
    var AES: String =""
    // Request code for creating a PDF document.
    val CREATE_FILE = 1




    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity: iniciado= activity as iniciado
        globalStatus = mainActivity.globalStatus
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)
        val textView: TextView = binding.textSettings
        settingsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        binding.button.setOnClickListener{
            //abrirexplorador()
        }
    }





}