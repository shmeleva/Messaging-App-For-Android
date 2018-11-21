package xyz.shmeleva.eight.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_chat.*

import xyz.shmeleva.eight.R
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import com.stfalcon.multiimageview.MultiImageView


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChatFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //val binding : ChatFragmentBinding = ChatFragmentBinding.inflate(inflater, R.layout.fragment_chat, container,false)
        //return binding.root
        var viewRoot = inflater.inflate(R.layout.fragment_chat, container,false)
        return  viewRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatMultiImageView.shape = MultiImageView.Shape.CIRCLE

        chatBackImageView.setOnClickListener({view -> activity?.onBackPressed()})
        chatMultiImageView.setOnClickListener({view -> openChatSettings(view)})
        chatTitleTextView.setOnClickListener({view -> openChatSettings(view)})
        chatActionImageButton.setOnClickListener({view -> onActionButtonClicked(view)})
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

    fun navigateBack(view: View) {
        activity?.onBackPressed()
    }

    fun openChatSettings(view: View) {
        // Private chat:
        val chatSettingsFragment = PrivateChatSettingsFragment()
        activity?.supportFragmentManager
                ?.beginTransaction()
                ?.add(R.id.chatFragmentContainer, chatSettingsFragment as android.support.v4.app.Fragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    fun onActionButtonClicked(view: View) {
        val message = chatEditText.text
        if (message.isNullOrBlank()) {
            // Open image selection...
        }
        else {
            // Send message...
        }
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
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
