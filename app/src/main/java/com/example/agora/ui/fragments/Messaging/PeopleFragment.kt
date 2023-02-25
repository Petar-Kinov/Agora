package com.example.agora.ui.fragments.Messaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.ChatActivity
import com.example.agora.data.Messaging.Model.Person
import com.example.agora.databinding.FragmentChatsBinding
import com.example.agora.domain.Messaging.ViewModel.ChatsViewModel
import com.example.agora.domain.Messaging.ViewModel.ChatsViewModelFactory
import com.example.agora.util.AppConstants
import com.example.agora.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section

private const val TAG = "MessagesFragment"
class PeopleFragment : Fragment() {

    private var _binding :FragmentChatsBinding? =null
    private val binding get() = _binding!!
    private lateinit var viewModel : ChatsViewModel
    private var recyclerView : RecyclerView? =null
    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is Person) {
            val intent = Intent(requireContext(),ChatActivity::class.java)
            intent.putExtra(AppConstants.USER_NAME,item.name)
            intent.putExtra(AppConstants.USER_ID,item.firestoreUserId)
            startActivity(intent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userListenerRegistration = FirestoreUtil.addUsersListener(this.requireActivity(),this::updateRecyclerView)
//        viewModel = ViewModelProvider(requireActivity(),ChatsViewModelFactory())[ChatsViewModel::class.java]
//        viewModel.getChats()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater ,container,false)
        val view = binding.root

        recyclerView = binding.chatsRV
//        val adapter = GroupieAdapter().apply {
//            setOnItemClickListener(onItemClick)
//        }
//        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView!!.adapter = adapter

//        viewModel.chatsList.observe(viewLifecycleOwner){
//            adapter.update(it)
//        }


        return view
    }

    private fun updateRecyclerView(items : List<Person>){
        fun init(){
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupieAdapter().apply {
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems () = peopleSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }
}