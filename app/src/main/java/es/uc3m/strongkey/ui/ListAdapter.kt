package es.uc3m.strongkey.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import es.uc3m.strongkey.models.AESFile
import es.uc3m.strongkey.databinding.RecyclerViewItemBinding

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var studentList = emptyList<AESFile>()

    class MyViewHolder(val mContext: Context, val binding: RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.firstName.setOnClickListener{
                val position: Int = adapterPosition
                Toast.makeText(itemView.context, "You clicked on item ${position +1}", Toast.LENGTH_SHORT).show()

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                mContext.startActivity(intent)
            }
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