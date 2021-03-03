package es.uc3m.strongkey.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import es.uc3m.strongkey.R
import es.uc3m.strongkey.databinding.FragmentSettingsBinding
import es.uc3m.strongkey.iniciado
import es.uc3m.strongkey.ui.SharedPreference
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
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
            val filePath = data?.data!!
            //globalStatus.ruta=filePath!!
            var sharedPreference:SharedPreference= SharedPreference(requireContext())
            sharedPreference.save("1", filePath.toString())
            globalStatus.mapa.put("1", filePath.toString())
            val lineList = mutableListOf<String>()
            //var word: String = "primary:"
            val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(filePath, "r", null)
            val out = StringBuilder()
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
                //PARA LEER EL CONTENIDO
            }

            val editAlert = AlertDialog.Builder(requireContext()).create();
            val editView = layoutInflater.inflate(R.layout.edit_text_layout,null);
            editAlert.setView(editView)
            editAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",{
                _,_->
                val text = editAlert.findViewById<EditText>(R.id.alert_dialog_edit_text).text
                Toast.makeText(requireContext(), "Tu texto es:\n$text",Toast.LENGTH_LONG).show()
                var clave: String = text.toString()
                if(text.toString().length<32){
                    println("HOLA")
                    var restantes : Int =  32- text.toString().length ;
                    var contador : Int=0;
                    while(contador<restantes){
                        clave+="0"
                        contador++
                    }

                }
                println(encrypt(out.toString(), clave))
            })
            editAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",{_,_->
                Toast.makeText(requireContext(), "NOPE", Toast.LENGTH_SHORT).show()
            })

            editAlert.show()


            println(clave)

            //println(encrypt(out.toString(), clave))

            Toast.makeText(requireContext(), filePath.toString(), Toast.LENGTH_SHORT).show()
        }
    }

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


    /*fun ContentResolver.getFileName(fileUri: Uri): String {

        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }*/

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


}