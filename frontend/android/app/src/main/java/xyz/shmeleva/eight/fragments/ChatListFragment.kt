package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.adapters.ChatListAdapter
import xyz.shmeleva.eight.models.Chat
import kotlinx.android.synthetic.main.fragment_chat_list.*
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.activities.SettingsActivity

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChatListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    private var isFabOpen = false

    private var fabOpenAnimation: Animation? = null
    private var fabCloseAnimation: Animation? = null
    private var rotateForwardAnimation: Animation? = null
    private var rotateBackwardAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabOpenAnimation = AnimationUtils.loadAnimation(activity, R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_close)
        rotateForwardAnimation = AnimationUtils.loadAnimation(activity,R.anim.rotate_forward)
        rotateBackwardAnimation = AnimationUtils.loadAnimation(activity,R.anim.rotate_backward)


        chatListStartChatFab.setOnClickListener { view ->
            if(isFabOpen){
                chatListStartChatFab.startAnimation(rotateBackwardAnimation);
                chatListStartGroupChatFab.startAnimation(fabCloseAnimation);
                chatListStartPrivateChatFab.startAnimation(fabCloseAnimation);
                //
                chatListStartGroupChatFab.isClickable = false;
                chatListStartPrivateChatFab.isClickable = false;
                //
                isFabOpen = false;
            } else {
                chatListStartChatFab.startAnimation(rotateForwardAnimation);
                chatListStartGroupChatFab.startAnimation(fabOpenAnimation);
                chatListStartPrivateChatFab.startAnimation(fabOpenAnimation);
                //
                chatListStartGroupChatFab.isClickable = true;
                chatListStartPrivateChatFab.isClickable = true;
                //
                isFabOpen = true;

            }
        }

        chatListStartGroupChatFab.setOnClickListener { _ ->

        }

        chatListStartPrivateChatFab.setOnClickListener { _ ->

        }

        chatListSettingsButton.setOnClickListener { _ ->
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        chatListSearchButton.setOnClickListener({ _ ->
            (activity as BaseFragmentActivity).addFragment(SearchFragment.newInstance("", ""))
        })

        chatListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val chats = ArrayList<Chat>(listOf(Chat(0), Chat(1), Chat(2), Chat(3), Chat(4), Chat(5), Chat(6), Chat(7), Chat(8), Chat(9), Chat(10)))
        var adapter = ChatListAdapter(chats, { chat : Chat -> onChatClicked(chat) })
        chatListRecyclerView.adapter = adapter
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    */

    private fun onChatClicked(chat : Chat) {
        val chatActivityIntent = Intent(activity, ChatActivity::class.java)
        startActivity(chatActivityIntent)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other xyz.shmeleva.eight.fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/xyz.shmeleva.eight.fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatListFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): ChatListFragment {
            val fragment = ChatListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
