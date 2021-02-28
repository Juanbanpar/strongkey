package es.uc3m.strongkey.ui.strongboxes

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
import es.uc3m.strongkey.R
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.ui.SharedPreference

class StrongboxesFragment : Fragment() {
    lateinit var globalStatus: GlobalStatus
    private lateinit var strongboxesViewModel: StrongboxesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val mainActivity: iniciado = activity as iniciado
        globalStatus = mainActivity.globalStatus
        strongboxesViewModel =
                ViewModelProvider(this).get(StrongboxesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_strongboxes, container, false)
        val textView: TextView = root.findViewById(R.id.text_strongboxes)
        strongboxesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        var sharedPreference: SharedPreference = SharedPreference(requireContext())
        //Toast.makeText(requireContext(), globalStatus.mapa.get("1").toString(), Toast.LENGTH_SHORT).show()
        if(sharedPreference.getValueString("1")!=null){
            Toast.makeText(requireContext(), sharedPreference.getValueString("1"), Toast.LENGTH_SHORT).show()
        }

        return root
    }
}