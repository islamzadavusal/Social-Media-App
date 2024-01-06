package com.islamzada.socialmediaapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.islamzada.socialmediaapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.signinLinkBtn.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }

        binding.signupBtn.setOnClickListener {
            CreateAccount()
        }

    }

    private fun CreateAccount() {
        val fullName = binding.fullnameSignup.text.toString()
        val userName = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when {

            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Full name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

        else -> {

            val progressDialog = ProgressDialog(this@SignUpActivity)
            progressDialog.setTitle("SignUp")
            progressDialog.setMessage("Please wait, this may take a while...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {

                    saveUserInfo(fullName, userName, email, password, progressDialog)

                }else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "ERROR: $message", Toast.LENGTH_LONG).show()
                    mAuth.signOut()
                    progressDialog.dismiss()

                }
            }
        }

        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, password: String, progressDialog: ProgressDialog) {

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "Hey, I am using Instagram"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/islamzadaapp.appspot.com/o/Default%20Images%2Fprofile_photo.jpg?alt=media&token=929a292f-64a6-464a-a642-5412b2ed41f4"

        usersRef.child(currentUserID).setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful){

                progressDialog.dismiss()
                Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_LONG).show()


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)


                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }else{

                val message = task.exception!!.toString()
                Toast.makeText(this, "ERROR: $message", Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                progressDialog.dismiss()
            }
        }
    }
}