package com.islamzada.socialmediaapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.islamzada.socialmediaapp.R
import com.islamzada.socialmediaapp.adapter.PostAdapter
import com.islamzada.socialmediaapp.databinding.FragmentHomeBinding
import com.islamzada.socialmediaapp.databinding.FragmentSearchBinding
import com.islamzada.socialmediaapp.model.Post

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<Post>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        var recyclerView: RecyclerView? = null
        recyclerView = binding.recyclerViewHome

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        checkFollowings()

        return binding.root

    }

    private fun checkFollowings() {

        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapshot in p0.children) {

                    val post = snapshot.getValue(Post::class.java)

                    for (id in (followingList as ArrayList<String>)) {
                        if (post!!.getPublisher() == id) {

                            postList!!.add(post)

                        }

                        postAdapter!!.notifyDataSetChanged()
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}


