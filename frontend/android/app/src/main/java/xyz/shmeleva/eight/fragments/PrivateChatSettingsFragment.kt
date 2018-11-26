package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_private_chat_settings.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.ChatActivity

class PrivateChatSettingsFragment : Fragment() {

    private var shouldLaunchChat: Boolean? = null

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            shouldLaunchChat = arguments!!.getBoolean(ARG_SHOULD_LAUNCH_CHAT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_private_chat_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privateChatBackButton.setOnClickListener({_ -> activity?.onBackPressed()})
        privateChatStartFab.setOnClickListener({_ ->
            if (shouldLaunchChat == true) {
                val chatActivityIntent = Intent(activity, ChatActivity::class.java)
                startActivity(chatActivityIntent)
                activity?.finishAfterTransition()
            }
            else {
                activity?.onBackPressed()
            }
        })
    }

    fun onButtonPressed(uri: Uri) {
        if (fragmentInteractionListener != null) {
            fragmentInteractionListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            fragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentInteractionListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_SHOULD_LAUNCH_CHAT = "shouldLaunchChat"

        // shouldLaunchChat: true when added from GroupChatSettingsFragment, SearchFragment, CreatePrivateChatFragment; false when added from ChatFragment
        fun newInstance(shouldLaunchChat: Boolean): PrivateChatSettingsFragment {
            val fragment = PrivateChatSettingsFragment()
            val args = Bundle()
            args.putBoolean(ARG_SHOULD_LAUNCH_CHAT, shouldLaunchChat)
            fragment.arguments = args
            return fragment
        }
    }
}
