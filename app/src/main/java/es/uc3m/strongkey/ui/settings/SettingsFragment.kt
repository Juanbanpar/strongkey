package es.uc3m.strongkey.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.MainActivity
import es.uc3m.strongkey.databinding.FragmentSettingsBinding
import es.uc3m.strongkey.ui.SharedPreference
import kotlinx.coroutines.currentCoroutineContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel
    lateinit var globalStatus: GlobalStatus


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity: MainActivity= activity as MainActivity
        globalStatus = mainActivity.globalStatus
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)
        val textView: TextView = binding.textSettings
        settingsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        binding.button.setOnClickListener{
            abrirexplorador()
        }
    }

    private fun abrirexplorador(){
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 777) {
            val filePath = data?.data?.path
            globalStatus.ruta=filePath!!
            var sharedPreference:SharedPreference= SharedPreference(requireContext())
            sharedPreference.save("1",filePath)
            globalStatus.mapa.put("1",filePath)
            /*val folder = filePath
            val f = File(folder, "folder_name")
            f.mkdir()*/
            var filename = "test.txt"
            val folder = File("/sdcard/MetroPol/")
            folder.mkdirs()
            val outputFile = File(folder, filename)
            try {
                val fos = FileOutputStream(outputFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            Toast.makeText(requireContext(), filePath, Toast.LENGTH_SHORT).show()
        }
    }

}