package es.uc3m.strongkey.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import es.uc3m.strongkey.R
import es.uc3m.strongkey.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel
    

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
}