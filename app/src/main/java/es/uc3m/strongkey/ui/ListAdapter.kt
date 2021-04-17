package es.uc3m.strongkey.ui

import android.R.attr.key
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.databinding.RecyclerViewItemBinding
import es.uc3m.strongkey.models.AESFile
import es.uc3m.strongkey.ui.strongboxes.StrongboxesViewModel
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec


class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    private var fileList = emptyList<AESFile>()

    class MyViewHolder(val mContext: Context, val binding: RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root){
        val out2=StringBuilder()
        var descifrado: String=""
        var nombre: String=""
        var rutaa: String=""
        init {
            binding.name.setOnClickListener{
                var clave: String= binding.clave.text as String
                var ruta: String=binding.ruta.text as String
                var tipo: String=binding.name.text as String
                nombre=tipo
                rutaa=ruta

                var input: String=""
                var hash:String=""
                val taskEditText = EditText(mContext)
                val dialog: AlertDialog = AlertDialog.Builder(mContext)
                        .setTitle("Introduce la contraseña")
                        .setMessage("En el siguiente campo")
                        .setView(taskEditText)
                        .setPositiveButton("Desencriptar", DialogInterface.OnClickListener { dialog, which ->
                            input = taskEditText.text.toString()
                            println("PRE 0: " + input)
                            if (input.length < 32) {
                                var restantes: Int = 32 - input.length;
                                var contador: Int = 0;
                                while (contador < restantes) {
                                    input += "0"
                                    contador++
                                }
                            }
                            println("Post 0: " + input)
                            val HEX_CHARS = "0123456789ABCDEF"
                            val bytes = MessageDigest
                                    .getInstance("SHA-256")
                                    .digest(input.toByteArray())
                            val result = StringBuilder(bytes.size * 2)

                            bytes.forEach {
                                val i = it.toInt()
                                result.append(HEX_CHARS[i shr 4 and 0x0f])
                                result.append(HEX_CHARS[i and 0x0f])
                            }
                            hash = result.toString()
                            if (hash.equals(clave)) {
                                val uri = ruta.toUri()
                                val parcelFileDescriptor = mContext?.contentResolver?.openFileDescriptor(uri, "r", null)
                                parcelFileDescriptor?.let {
                                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                                    val bufferSize = 1024
                                    val buffer = CharArray(bufferSize)

                                    val `in`: Reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
                                    var charsRead: Int
                                    while (`in`.read(buffer, 0, buffer.size).also { charsRead = it } > 0) {
                                        out2.append(buffer, 0, charsRead)
                                    }
                                    inputStream.close()
                                    //PARA LEER EL CONTENIDO
                                }
                                descifrado = decryptWithAES(input, out2.toString())!!
                                println("descifrado " + descifrado)
                                createFile()
                            } else {
                                Toast.makeText(mContext, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create()
                dialog.show()

                val position: Int = adapterPosition
                Toast.makeText(itemView.context, "You clicked on item ${position + 1}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun createFile() {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                val X: String = nombre.substring(0, nombre.length - 4)
                putExtra(Intent.EXTRA_TITLE, X)

            }
            val globalStatus: GlobalStatus = GlobalStatus
            globalStatus.mapa.put("FICHERO", descifrado)
            globalStatus.mapa.put("RUTAA", rutaa)
            (mContext as Activity).startActivityForResult(intent, 2)
        }
        private fun decryptWithAES(key: String, strToDecrypt: String?): String? {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)

        return MyViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = fileList[position]
        with(holder){
            binding.name.text = currentItem.extension
            binding.clave.text = currentItem.password
            binding.ruta.text = currentItem.path
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }


    fun setData(fileList: List<AESFile>){
        this.fileList = fileList
        notifyDataSetChanged()
    }

}