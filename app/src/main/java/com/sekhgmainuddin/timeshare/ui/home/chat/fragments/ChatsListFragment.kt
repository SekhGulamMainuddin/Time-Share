package com.sekhgmainuddin.timeshare.ui.home.chat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sekhgmainuddin.timeshare.databinding.FragmentChatsListBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.fragments.adapters.ChatsListAdapter
import kotlinx.coroutines.*

class ChatsListFragment : Fragment() {

    private var _binding: FragmentChatsListBinding? = null
    private val binding: FragmentChatsListBinding
        get() = _binding!!

    private var string= "My name is Sekh Gulam Mainuddin I am from India ,Odisha and I like to play football and my aim is to become a successful software engineer"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentChatsListBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter= ChatsListAdapter(requireContext())
        binding.chatsListRV.adapter= adapter

        val list: MutableList<String> = string.split(" ").toMutableList()

        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            withContext(Dispatchers.Main){
                adapter.submitList(list)
            }

            delay(5000)
            withContext(Dispatchers.Main){
                string= "My is Sekh GULAM MAINUDDIN I am from India ,Odisha and I like to play football and my aim is to become a successful software engineer"
                adapter.submitList(string.split(" ")).apply {

                }
            }
        }

        binding.chatsListRV.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    //You should HIDE filter view here
                } else if (dy < 0) {
                    System.out.println("Scrolled Upwards");
                    if ((binding.chatsListRV.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                        //this is the top of the RecyclerView
                        System.out.println("==================================== >>>> Detect top of the item List");
                        //You should visible filter view here
                    } else {
                        //You should HIDE filter view here
                    }
                } else {
                    System.out.println("No Vertical Scrolled");
                    //You should HIDE filter view here
                }
            }
        })

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}