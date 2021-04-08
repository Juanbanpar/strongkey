package es.uc3m.strongkey.ui.wallet

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.databinding.FragmentWalletBinding
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.models.Wallet
import es.uc3m.strongkey.ui.ListAdapter
import es.uc3m.strongkey.ui.ListAdapterWallet
import es.uc3m.strongkey.ui.strongboxes.StrongboxesViewModel


class WalletFragment : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    lateinit var globalStatus: GlobalStatus
    private lateinit var walletViewModel: WalletViewModel
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    val adapter = ListAdapterWallet()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        val recyclerView = _binding!!.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)
        walletViewModel.readAll.observe(viewLifecycleOwner, { student ->
            adapter.setData(student)
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity: iniciado = activity as iniciado
        globalStatus = mainActivity.globalStatus
        super.onViewCreated(view, savedInstanceState)
        walletViewModel =
                ViewModelProvider(this).get(WalletViewModel::class.java)
        /* val textView: TextView = root.findViewById(R.id.text_wallet)
        walletViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
        binding.cartera.setOnClickListener {
            abrirexplorador()
        }
        binding.descarga.setOnClickListener {
            descargar()
        }
    }

    private fun abrirexplorador(){
        val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
    }

    private fun descargar(){
        val storageRef = storage.reference
        var uid = globalStatus.mapa.get("ID") as String
        var islandRef = storageRef.child("users/${uid}")

        islandRef.listAll()
            .addOnSuccessListener(OnSuccessListener<ListResult> { listResult ->
                for (fileRef in listResult.items) {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        var cart: Wallet
                        cart=Wallet(uri.toString())
                        walletViewModel.addFile(cart)
                        Log.d("item", uri.toString())
                    }.addOnSuccessListener {
                        //recyclerView.setAdapter(adapter)
                        //progressBar.setVisibility(View.GONE)
                    }
                }
            })


        /*val localFile = File.createTempFile("text", "pub")
        islandRef.getFile(localFile).addOnSuccessListener {
            // Local temp file has been created
        }.addOnFailureListener {
            // Handle any errors
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 777) {
            val filePath = data?.data!!
            var fileName: String=""
            if (filePath.getScheme().equals("file")) {
                fileName = filePath.getLastPathSegment().toString()
            } else {
                var cursor: Cursor? = null
                try {
                    cursor = requireContext().getContentResolver().query(
                        filePath, arrayOf(
                            MediaStore.Images.ImageColumns.DISPLAY_NAME
                        ), null, null, null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                        println("NOMBRE DEL FICHERO: " + fileName)
                    }
                } finally {
                    cursor?.close()
                }
            }
            if((globalStatus.mapa.get("email") as String)== null){
                Toast.makeText(
                    requireContext(),
                    "Para usar esta función necesitas Google",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                val storageRef = storage.reference
                var uid = globalStatus.mapa.get("ID") as String
                val walletRef = storageRef.child("/users/${uid}/${fileName}")
                var uploadTask = walletRef.putFile(filePath)
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener {
                    println("patata error")
                }.addOnSuccessListener { taskSnapshot ->
                    print("Acierto")
                }

            }
        }
    }
}

