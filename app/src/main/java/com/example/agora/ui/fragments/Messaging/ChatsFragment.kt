package com.example.agora.ui.fragments.Messaging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.data.Messaging.Model.Chat
import com.example.agora.databinding.FragmentChatsBinding
import com.example.agora.domain.Messaging.ViewModel.ChatsViewModel
import com.example.agora.domain.Messaging.ViewModel.ChatsViewModelFactory
import com.xwray.groupie.GroupieAdapter

private const val TAG = "MessagesFragment"
class ChatsFragment : Fragment() {

    private var _binding :FragmentChatsBinding? =null
    private val binding get() = _binding!!

    private lateinit var viewModel : ChatsViewModel

    private var recyclerView : RecyclerView? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(),ChatsViewModelFactory())[ChatsViewModel::class.java]
        viewModel.getChats()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater ,container,false)
        val view = binding.root

        recyclerView = binding.chatsRV
        val adapter = GroupieAdapter().apply {
            this.setOnItemClickListener { chat, view ->
                TODO("open ChatActivity and pass the person")
            }
        }
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.adapter = adapter

        val chatLists = listOf<Chat>(Chat("Person 1", "userId 1"),Chat("Person 2", "userId 2"),Chat("Person 3", "userId 3"),Chat("Person 4", "userId 4"))


        viewModel.chatsList.observe(viewLifecycleOwner){
            adapter.update(it)
        }



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }
}