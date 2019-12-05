package com.wydgettech.contextualwalls.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.wydgettech.contextualwalls.R
import android.content.Intent
import android.net.Uri
import kotlinx.android.synthetic.main.fragment_upload.*
import android.widget.Toast


class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uploadViewModel =
            ViewModelProviders.of(this).get(UploadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_upload, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        val uploadBtn: Button = root.findViewById(R.id.upload_btn)


        uploadBtn.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK
            )
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
        return root

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if(requestCode == 1) {
                var image: Uri = data!!.getData()!!
                upload_imageView.setImageURI(image);
                uploadViewModel.uploadImage(context!!, image, {
                    spin_kit.visibility = View.INVISIBLE
                    if(it)
                        Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Failed Uploading", Toast.LENGTH_SHORT).show()
                    upload_imageView.setImageDrawable(resources.getDrawable(R.drawable.no_image_available))
                })
                spin_kit.visibility = View.VISIBLE
            }
    }


}

