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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import es.uc3m.strongkey.R
import es.uc3m.strongkey.databinding.RecyclerViewItemBinding
import es.uc3m.strongkey.models.Wallet
import es.uc3m.strongkey.ui.wallet.WalletViewModel
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
import org.bouncycastle.jce.provider.BouncyCastleProvider


class ListAdapterWallet:RecyclerView.Adapter<ListAdapterWallet.MyViewHolder>()  {
    private var studentList = emptyList<Wallet>()

    class MyViewHolder(val mContext: Context, val binding: RecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            var navController: NavController? = null
            binding.firstName.setOnClickListener{
                //val position: Int = adapterPosition
                //navController = Navigation.findNavController(itemView)

                //navController!!.navigate(R.id.action_listFragment_to_saveFragment)
                Toast.makeText(itemView.context, "You clicked on item ${position +1}", Toast.LENGTH_SHORT).show()

                var temp:String=binding.firstName.text as String
                val intent = Intent(Intent.ACTION_VIEW, temp.toUri())
                mContext.startActivity(intent)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapterWallet.MyViewHolder {
        val binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent,
            false)

        return ListAdapterWallet.MyViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: ListAdapterWallet.MyViewHolder, position: Int) {
        val currentItem = studentList[position]
        with(holder){
            binding.firstName.text = currentItem.path

        }
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    fun setData(studentList: List<Wallet>){
        this.studentList = studentList
        notifyDataSetChanged()
    }
}