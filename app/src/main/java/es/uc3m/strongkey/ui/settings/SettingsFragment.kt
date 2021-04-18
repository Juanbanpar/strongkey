package es.uc3m.strongkey.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import es.uc3m.strongkey.AuthActivity
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.databinding.FragmentSettingsBinding
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.ui.SharedPreference

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
        val mainActivity: iniciado= activity as iniciado
        globalStatus = mainActivity.globalStatus
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        binding.button.setOnClickListener{
            //logout
            val sharedPreference: SharedPreference = SharedPreference(requireContext())
            sharedPreference.save("EMAIL", "")
            sharedPreference.save("ID", "")

            val homeIntent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(homeIntent)
        }
    }





}