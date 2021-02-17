package es.uc3m.strongkey.ui.strongboxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import es.uc3m.strongkey.R

class StrongboxesFragment : Fragment() {

    private lateinit var strongboxesViewModel: StrongboxesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        strongboxesViewModel =
                ViewModelProvider(this).get(StrongboxesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_strongboxes, container, false)
        val textView: TextView = root.findViewById(R.id.text_strongboxes)
        strongboxesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}