package com.islamzada.socialmediaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.islamzada.socialmediaapp.databinding.ActivityAccountSettingsBinding
import com.islamzada.socialmediaapp.model.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private lateinit var binding: ActivityAccountSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        setContentView(binding.root)

        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        binding.changeImageTextBtn.setOnClickListener {

            checker = "clicked"

            CropImage.activity().setAspectRatio(1,1).start(this@AccountSettingsActivity)
        }


        binding.saveProfileBtn.setOnClickListener {
            if (checker == "clicked") {

                uploadImageAndUpdateInfo()

            } else {
                updateUserInfoOnly()
            }

        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.profileImageViewProfile.setImageURI(imageUri)

        }
    }

    private fun updateUserInfoOnly() {

        if (TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString())) {
            Toast.makeText(this, "Please write full name first", Toast.LENGTH_LONG).show()
        }

        else if (TextUtils.isEmpty(binding.usernameProfileFrag.text.toString())) {
            Toast.makeText(this, "Please write user name first", Toast.LENGTH_LONG).show()
        }

        else if (TextUtils.isEmpty(binding.bioProfileFrag.text.toString())) {
            Toast.makeText(this, "Please write bio first", Toast.LENGTH_LONG).show()

        } else {


        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["fullname"] = binding.fullNameProfileFrag.text.toString().toLowerCase()
        userMap["username"] = binding.usernameProfileFrag.text.toString().toLowerCase()
        userMap["bio"] = binding.bioProfileFrag.text.toString()

            usersRef.child(firebaseUser.uid).updateChildren(userMap)

            Toast.makeText(this, "Account information has been updated successfully.", Toast.LENGTH_LONG).show()

            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
    }
}

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile_photo).into(binding.profileImageViewProfile)
                    binding.usernameProfileFrag.setText(user.getUsername())
                    binding.fullNameProfileFrag.setText(user.getFullname())
                    binding.bioProfileFrag.setText(user.getBio())
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    private fun uploadImageAndUpdateInfo() {

        when {

            imageUri == null ->
                Toast.makeText(this, "Please select image first", Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) ->
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_LONG).show()

            binding.usernameProfileFrag.text.toString() == "" ->
                Toast.makeText(this, "Please write user name first", Toast.LENGTH_LONG).show()

            binding.bioProfileFrag.text.toString() == "" ->
                Toast.makeText(this, "Please write bio first", Toast.LENGTH_LONG).show()


            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()

                        userMap["fullname"] = binding.fullNameProfileFrag.text.toString().toLowerCase()
                        userMap["username"] = binding.usernameProfileFrag.text.toString().toLowerCase()
                        userMap["bio"] = binding.bioProfileFrag.text.toString().toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account Information has been updated successfully.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }
}