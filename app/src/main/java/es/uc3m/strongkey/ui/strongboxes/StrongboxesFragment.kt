package es.uc3m.strongkey.ui.strongboxes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.Interfaz
import es.uc3m.strongkey.R
import es.uc3m.strongkey.databinding.FragmentStrongboxesBinding
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.models.AESFile
import es.uc3m.strongkey.models.AESFileRepository
import es.uc3m.strongkey.ui.ListAdapter
import es.uc3m.strongkey.ui.SharedPreference
import es.uc3m.strongkey.ui.settings.SettingsViewModel
import okhttp3.CertificatePinner
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
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class StrongboxesFragment : Fragment() {
    private var _binding: FragmentStrongboxesBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel
    lateinit var globalStatus: GlobalStatus
    var clave: String = ""
    var AES: String = ""
    var sha1: String = ""
    val CREATE_FILE = 1
    val out = StringBuilder()
    var extension:String=""
    lateinit var fichero: AESFile
    var id: String=""
    lateinit var Repository: AESFileRepository
    private lateinit var strongboxesViewModel: StrongboxesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStrongboxesBinding.inflate(inflater, container, false)
        val adapter = ListAdapter()
        val recyclerView = _binding!!.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        strongboxesViewModel = ViewModelProvider(this).get(StrongboxesViewModel::class.java)
        strongboxesViewModel.readAll.observe(viewLifecycleOwner, { file ->
            adapter.setData(file)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity: iniciado= activity as iniciado
        globalStatus = mainActivity.globalStatus
        super.onViewCreated(view, savedInstanceState)
        strongboxesViewModel =
                ViewModelProvider(this).get(StrongboxesViewModel::class.java)
        //val textView: TextView = binding.textStrongboxes
        /*strongboxesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
       binding.encriptar.setOnClickListener{
            abrirexplorador()
        }

    }

    private fun abrirexplorador(){
        val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
    }

    suspend fun addDatos(fichero: AESFile){
        Repository.addAESFile(fichero)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1) {
            var path = data?.data!!
            val contentResolver = requireContext().contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(path, takeFlags)

            var fileName: String=""
            if (path.getScheme().equals("file")) {
                fileName = path.getLastPathSegment().toString()
            } else {
                var cursor: Cursor? = null
                try {
                    cursor = requireContext().getContentResolver().query(path, arrayOf(
                            MediaStore.Images.ImageColumns.DISPLAY_NAME
                    ), null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                        println("NOMBRE DEL FICHERO: " + fileName)
                    }
                } finally {
                    cursor?.close()
                }
            }

            alterDocument(path, AES)
            AES="0"
            fichero= AESFile(path.toString(), id, fileName, hashString("SHA-256", clave))
            strongboxesViewModel.addFile(fichero)
        }

        if (requestCode == 777) {
            val filePath = data?.data!!
            var sharedPreference:SharedPreference= SharedPreference(requireContext())
            sharedPreference.save("1", filePath.toString())
            globalStatus.mapa.put("1", filePath.toString())
            //var Correo:String = globalStatus.mapa.get("email") as String
            id= globalStatus.mapa.get("ID") as String
            val lineList = mutableListOf<String>()
            extension=filePath.toString().substringAfterLast('.', "")
            val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(filePath, "r", null)
            parcelFileDescriptor?.let {
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val bufferSize = 1024
                val buffer = CharArray(bufferSize)

                val `in`: Reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
                var charsRead: Int
                while (`in`.read(buffer, 0, buffer.size).also { charsRead = it } > 0) {
                    out.append(buffer, 0, charsRead)
                }
                inputStream.close()
                //PARA LEER EL CONTENIDO
            }

            val editAlert = AlertDialog.Builder(requireContext()).create();
            val editView = layoutInflater.inflate(R.layout.edit_text_layout, null);
            editAlert.setView(editView)
            editAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
                val text = editAlert.findViewById<EditText>(R.id.alert_dialog_edit_text).text
                sha1 = text.toString()
                Toast.makeText(requireContext(), "Tu texto es:\n$text", Toast.LENGTH_LONG).show()
                clave = text.toString()
                if (text.toString().length < 32) {
                    println("HOLA")
                    var restantes: Int = 32 - text.toString().length;
                    var contador: Int = 0;
                    while (contador < restantes) {
                        clave += "0"
                        contador++
                    }
                }
                //println(encrypt(out.toString(), clave))
                llamar(hashString("SHA-1", sha1).take(5))

            }
            editAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { _, _ ->
                Toast.makeText(requireContext(), "NOPE", Toast.LENGTH_SHORT).show()
            }

            editAlert.show()
            //println(encrypt(out.toString(), clave))
            Toast.makeText(requireContext(), filePath.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    //RETROFIT API PWND
    fun llamar(hash: String){

        val certificatePinner: CertificatePinner = CertificatePinner.Builder()
                .add("api.pwnedpasswords.com", "sha256/Y9mvm0exBk1JoQ57f9Vm28jKo5lFm/woKcVxrYxu80o=")
                .build()

        val client = OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.pwnedpasswords.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        val service =  retrofit.create(Interfaz::class.java)
        val result: Call<ResponseBody> = service.getPWND(hash)

        result.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.body() != null) {
                    var hashes: String? = response.body()?.string();
                    var lines: Array<String> = emptyArray()
                    if (hashes != null) {
                        lines = hashes.split("\\r?\\n").toTypedArray()
                    };
                    var temporal: String = hashString("SHA-1", sha1)
                    if (lines != null) {
                        for (line in lines) {
                            if (line.contains(temporal.substring(5), ignoreCase = true)) {
                                //lines = emptyArray()
                                //System.out.println(
                                //"password found, count: " + line.substring(line.indexOf(":") + 1));
                                println("contraseña REPE")
                                val builder = AlertDialog.Builder(requireContext())
                                //set title for alert dialog
                                builder.setTitle("Upsie dupsie")
                                //set message for alert dialog
                                builder.setMessage("Contraseña PWND, ¿deseas continuar?")
                                //builder.setIcon(android.R.drawable.ic_dialog_alert)

                                //performing positive action
                                builder.setPositiveButton("Continuar") { dialogInterface, which ->
                                    AES = encrypt(out.toString(), clave)!!
                                    createFile()
                                }

                                //performing negative action
                                builder.setNegativeButton("Cancelar") { dialogInterface, which ->
                                    Toast.makeText(requireContext(), "Operacion cancelada", Toast.LENGTH_LONG).show()
                                }
                                // Create the AlertDialog
                                val alertDialog: AlertDialog = builder.create()
                                // Set other dialog properties
                                alertDialog.setCancelable(false)
                                alertDialog.show()

                                return;
                            }
                        }
                    }
                    //lines= emptyArray()
                    System.out.println("password not found");
                    AES = encrypt(out.toString(), clave)!!
                    createFile()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                println("ERROR")
            }
        })


    }

    //ABRE EXPLORADOR PARA CREAR FICHERO POR DEFECTO TIPO AES
    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/aes"
            putExtra(Intent.EXTRA_TITLE, "crypt.aes")


            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

        startActivityForResult(intent, CREATE_FILE)
    }

    //PARA ESCRIBIR DENTRO DE UN FICHERO
    private fun alterDocument(uri: Uri, contenido: String) {
        //val contentResolver = requireContext().contentResolver
        try {
            context?.contentResolver?.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                            ("${contenido}")
                                    .toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //ENCRIPTAR CON AES
    fun encrypt(strToEncrypt: String, secret_key: String): String? {
        Security.addProvider(BouncyCastleProvider())
        var keyBytes: ByteArray

        try {
            keyBytes = secret_key.toByteArray(charset("UTF8"))
            val skey = SecretKeySpec(keyBytes, "AES")
            val iv = IvParameterSpec(secret_key.substring(0, 16).toByteArray(Charsets.UTF_8))
            val input = strToEncrypt.toByteArray(charset("UTF8"))

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, skey, iv)

                val cipherText = ByteArray(cipher.getOutputSize(input.size))
                var ctLength = cipher.update(
                        input, 0, input.size,
                        cipherText, 0
                )
                ctLength += cipher.doFinal(cipherText, ctLength)
                return String(
                        Base64.encode(cipherText)
                )
            }
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
        } catch (ibse: IllegalBlockSizeException) {
            ibse.printStackTrace()
        } catch (bpe: BadPaddingException) {
            bpe.printStackTrace()
        } catch (ike: InvalidKeyException) {
            ike.printStackTrace()
        } catch (nspe: NoSuchPaddingException) {
            nspe.printStackTrace()
        } catch (nsae: NoSuchAlgorithmException) {
            nsae.printStackTrace()
        } catch (e: ShortBufferException) {
            e.printStackTrace()
        }

        return null
    }

    //HACER HASH DE LA CLAVE AES
    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
                .getInstance(type)
                .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}