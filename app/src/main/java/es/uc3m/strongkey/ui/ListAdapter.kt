package es.uc3m.strongkey.ui

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
import androidx.recyclerview.widget.RecyclerView
import es.uc3m.strongkey.GlobalStatus
import es.uc3m.strongkey.databinding.RecyclerViewItemBinding
import es.uc3m.strongkey.models.AESFile
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
    private var studentList = emptyList<AESFile>()

    class MyViewHolder(val mContext: Context, val binding: RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root){
        val out2=StringBuilder()
        var descifrado: String=""
        init {
            binding.firstName.setOnClickListener{
                var clave: String= binding.LastName.text as String
                var ruta: String=binding.age.text as String
                var tipo: String=binding.firstName.text as String


                /*var diag = StrongboxesFragment()
                diag.dialogo(clave)*/
                var input: String=""
                var hash:String=""
                val taskEditText = EditText(mContext)
                val dialog: AlertDialog = AlertDialog.Builder(mContext)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which ->
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
                                println("EUREKA")
                                val uri= ruta.toUri()
                                val parcelFileDescriptor = mContext?.contentResolver?.openFileDescriptor(uri, "r", null)
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
                                        out2.append(buffer, 0, charsRead)
                                    }
                                    inputStream.close()
                                    //PARA LEER EL CONTENIDO
                                }
                                descifrado=decryptWithAES(input,out2.toString())!!
                                println("descifrado "+ descifrado)
                                createFile()
                                //abrirexplorador()
                                //decryptWithAES()
                            } else {
                                println("NO COINCIDE")
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                dialog.show()

                val position: Int = adapterPosition
                Toast.makeText(itemView.context, "You clicked on item ${position + 1}", Toast.LENGTH_SHORT).show()

                /*val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                mContext.startActivity(intent)*/
            }
        }

        private fun abrirexplorador(){
            val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
            (mContext as Activity).startActivityForResult(intent, 775)
            //startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
        }

        private fun createFile() {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/jpg"
                putExtra(Intent.EXTRA_TITLE, "c15.jpg")


                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker before your app creates the document.
                //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
            val globalStatus: GlobalStatus = GlobalStatus
            globalStatus.mapa.put("FICHERO",descifrado)
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
        val currentItem = studentList[position]
        with(holder){
            binding.firstName.text = currentItem.extension
            binding.LastName.text = currentItem.password
            binding.age.text = currentItem.path
        }
    }

    override fun getItemCount(): Int {
        return studentList.size
    }


    fun setData(studentList: List<AESFile>){
        this.studentList = studentList
        notifyDataSetChanged()
    }

}