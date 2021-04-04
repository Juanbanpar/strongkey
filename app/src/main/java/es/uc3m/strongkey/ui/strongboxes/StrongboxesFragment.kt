package es.uc3m.strongkey.ui.strongboxes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
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
        strongboxesViewModel.readAll.observe(viewLifecycleOwner, { student ->
            adapter.setData(student)
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
            //Para extraer la extension
            /*
            val extension2: String?

            //Check uri format to avoid null

            //Check uri format to avoid null
            extension2 = if (path.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(requireContext().contentResolver.getType(path))
            } else {
                //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(path.getPath())).toString())
            }
            println("JHKGKJHGKJH:" + extension2)*/

            val fileName: String
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
            fichero= AESFile(path.toString(), id, extension, hashString("SHA-256", clave))
            strongboxesViewModel.addFile(fichero)
        }

        if (requestCode === 2) {
            var path = data?.data!!

           /* val fileName: String
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
            }*/
            val globalStatus: GlobalStatus = GlobalStatus
            var final: String= globalStatus.mapa.get(("FICHERO")) as String
            println("ESTE ES EL FINAL: " +final)

            alterDocument(path, final)
        }

        if (requestCode == 777) {
            val filePath = data?.data!!
            //globalStatus.ruta=filePath!!
            var sharedPreference:SharedPreference= SharedPreference(requireContext())
            sharedPreference.save("1", filePath.toString())
            globalStatus.mapa.put("1", filePath.toString())
            var Correo:String = globalStatus.mapa.get("email") as String
            id= globalStatus.mapa.get("ID") as String
            val lineList = mutableListOf<String>()
            extension=filePath.toString().substringAfterLast('.', "")
            val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(filePath, "r", null)
            parcelFileDescriptor?.let {
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                /*val file = File(context.cacheDir, context.contentResolver.getFileName(filePath))
                val outputStream = FileOutputStream(file)
                IOUtils.copy(inputStream, outputStream)*/
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


    //POPUP PARA INTRODUCIR LA CLAVE AES
    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        val dialog: AlertDialog = AlertDialog.Builder(c)
                .setTitle("Add a new task")
                .setMessage("What do you want to do next?")
                .setView(taskEditText)
                .setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which -> clave = taskEditText.text.toString() })
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }

    //RETROFIT API PWND
    fun llamar(hash: String){
        val client = OkHttpClient.Builder().build()

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

    //funcion dialogo contraseña
    fun dialogo(checkHash: String){
        val editAlert = AlertDialog.Builder(requireContext()).create()
        val editView = layoutInflater.inflate(R.layout.edit_text_layout, null);
        editAlert.setView(editView)
        var temp:String=""
        editAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            val text = editAlert.findViewById<EditText>(R.id.alert_dialog_edit_text).text
            //sha1 = text.toString()
            Toast.makeText(requireContext(), "Tu texto es:\n$text", Toast.LENGTH_LONG).show()
            temp = text.toString()
            if (text.toString().length < 32) {
                var restantes: Int = 32 - text.toString().length;
                var contador: Int = 0;
                while (contador < restantes) {
                    temp += "0"
                    contador++
                }
            }

        }
        editAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL") { _, _ ->
            Toast.makeText(requireContext(), "NOPE", Toast.LENGTH_SHORT).show()
        }

        editAlert.show()
        println("CONTRASEÑA POPUP: " + temp)
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
            val input = strToEncrypt.toByteArray(charset("UTF8"))

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, skey)

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

    //DESENCRIPTAR AES
    fun decryptWithAES(key: String, strToDecrypt: String?): String? {
        Security.addProvider(BouncyCastleProvider())
        var keyBytes: ByteArray

        try {
            keyBytes = key.toByteArray(charset("UTF8"))
            val skey = SecretKeySpec(keyBytes, "AES")
            val input = org.bouncycastle.util.encoders.Base64
                    .decode(strToDecrypt?.trim { it <= ' ' }?.toByteArray(charset("UTF8")))

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
                cipher.init(Cipher.DECRYPT_MODE, skey)

                val plainText = ByteArray(cipher.getOutputSize(input.size))
                var ptLength = cipher.update(input, 0, input.size, plainText, 0)
                ptLength += cipher.doFinal(plainText, ptLength)
                val decryptedString = String(plainText)
                return decryptedString.trim { it <= ' ' }
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