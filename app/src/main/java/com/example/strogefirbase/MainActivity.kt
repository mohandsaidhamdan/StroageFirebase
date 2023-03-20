package com.example.strogefirbase

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class MainActivity : AppCompatActivity() {
    private val PICK_PDF_FILE = 1
    lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
//    val file_name_textview = findViewById<TextView>(R.id.textView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = ProgressDialog(this)

//        val storage = Firebase.storage
//        val ref = storage.reference

        val select_file_button = findViewById<Button>(R.id.select_file)


        val filesRecyclerView = findViewById<RecyclerView>(R.id.files_recycler_view)

        filesRecyclerView.layoutManager = LinearLayoutManager(this)

        val storageRef = FirebaseStorage.getInstance().reference

        val filesQuery = storageRef.child("pdfs").listAll()

        filesQuery.addOnSuccessListener { result ->
            val filesAdapter = FilesAdapter(this ,result.items)
            filesRecyclerView.adapter = filesAdapter
        }.addOnFailureListener { exception ->
            Log.e("FilesActivity", "Error getting files", exception)
        }











        select_file_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PICK_PDF_FILE)
            progressDialog.show()
            progressDialog.setContentView(R.layout.progress_dialog)
        }




    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedFileUri: Uri = data.data!!
//            file_name_textview.text = selectedFileUri.lastPathSegment
            //Step 3: Store the PDF file in Firebase Storage

            val storageRef = FirebaseStorage.getInstance().reference
            val pdfRef = storageRef.child("pdfs/${selectedFileUri.lastPathSegment}")
            pdfRef.putFile(selectedFileUri)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "تم التحميل الملف بنجاح", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {

                }
        }

    }

    class FilesAdapter(val contexts: Context , private val files: List<StorageReference>) :
        RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.fileNameTextView.text = file.name


            holder.btnDwonload.setOnClickListener {
                val progressDialog = ProgressDialog(contexts)
                progressDialog.show()
                progressDialog.setContentView(R.layout.progress_dialog)
                val storageRef = FirebaseStorage.getInstance().reference
                val pdfRef = storageRef.child("pdfs/${file.name}")
                val localFile = File.createTempFile("file", "pdf")
                pdfRef.getFile(localFile)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(contexts, "تم تحميل الملف", Toast.LENGTH_SHORT).show()
                    }

            }
        }

        override fun getItemCount() = files.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val fileNameTextView: TextView = itemView.findViewById(R.id.itemTextname)
            val btnDwonload: Button = itemView.findViewById(R.id.btn_dwnload)
        }
    }
}