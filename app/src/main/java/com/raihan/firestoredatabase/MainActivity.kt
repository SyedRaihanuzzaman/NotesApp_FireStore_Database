package com.raihan.firestoredatabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.raihan.firestoredatabase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),DataAdapter.ItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val dataCollection = db.collection("data")
    private val data = mutableListOf<Data>()
    private lateinit var adapter: DataAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = DataAdapter(data,this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)



        binding.addBtn.setOnClickListener {
            val title = binding.tittleEtxt.text.toString()
            val description = binding.descriptionEtxt.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()){
                addData(title,description)
            }
        }

        fetchData()

    }

    private fun fetchData() {
        dataCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                data.clear()
                for(document in it){
                    val item = document.toObject(Data::class.java)
                    item.id = document.id
                    data.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Data Fetch Failed",Toast.LENGTH_SHORT).show()
            }
    }

    private fun addData(title: String, description: String) {
        val newData = Data(title=title, description = description,timestamp = Timestamp.now())
        dataCollection.add(newData)
            .addOnSuccessListener {
                newData.id = it.id
                data.add(newData)
                adapter.notifyDataSetChanged()
                Toast.makeText(this,"Data added Successfully",Toast.LENGTH_SHORT).show()
                binding.tittleEtxt.text?.clear()
                binding.descriptionEtxt.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Data added Failed",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEditItemClick(data: Data) {
        binding.tittleEtxt.setText(data.title)
        binding.descriptionEtxt.setText(data.description)
        binding.addBtn.text = "Update"
            binding.addBtn.setOnClickListener {
                    val updateTitle = binding.tittleEtxt.text.toString()
                    val updateDescription = binding.descriptionEtxt.text.toString()

                    if (updateTitle.isNotEmpty() && updateDescription.isNotEmpty()){
                        val updateData = Data(data.id, updateTitle,updateDescription,Timestamp.now())
                        dataCollection.document(data.id!!)
                            .set(updateData)
                            .addOnSuccessListener {
                                binding.tittleEtxt.text?.clear()
                                binding.descriptionEtxt.text?.clear()
                                adapter.notifyDataSetChanged()
                                Toast.makeText(this,"Data Updated",Toast.LENGTH_SHORT).show()
                                startActivity((Intent(this,MainActivity::class.java)))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,"Data updated Failed",Toast.LENGTH_SHORT).show()
                            }
                    }


            }


    }

    override fun onDeleteItemClick(data: Data) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Files")
        dialog.setMessage("Do You want to Delete Files")
        dialog.setIcon(R.drawable.img)
        dialog.setPositiveButton("YES"){dialogInterface, which->
            dataCollection.document(data.id!!)
                .delete()
                .addOnSuccessListener {
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this,"Data Delete Successfully",Toast.LENGTH_SHORT).show()
                    fetchData()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Data Deletion Failed",Toast.LENGTH_SHORT).show()
                }
        }
        dialog.setNegativeButton("No"){dialogInterface, which->
            //startActivity((Intent(this,MainActivity::class.java)))
        }

        val alertDialog:AlertDialog = dialog.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }
}