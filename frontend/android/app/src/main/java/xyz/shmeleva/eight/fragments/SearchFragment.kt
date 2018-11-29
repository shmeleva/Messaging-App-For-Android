package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_search.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.adapters.UserListAdapter
import xyz.shmeleva.eight.models.User

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {

    private var source: Int? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            source = arguments!!.getInt(ARG_SOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBackButton.setOnClickListener({ _ -> activity?.onBackPressed()})

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                // TODO
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // TODO
                return false
            }

        })

        searchRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val users = ArrayList<User>(listOf(
                User("Екатерина Шмелева"),
                User("Больше Шмеля"),
                User("Шмели захватят мир"),
                User("Мы будем есть ваши души"),
                User("И закусывать цветочками"),
                User("Ням-ням"),
                User("Ekaterina Shmeleva"),
                User("What am I doing with my life?"),
                User("I need more users..."),
                User("Feel the Russian style"),
                User("Very big soul, you know"),
                User("Catch the Russian style"),
                User("From really simple Russian guy")))
        var adapter = UserListAdapter(users, { user : User -> onUserClicked(user) }, source == SOURCE_NEW_GROUP_CHAT)
        searchRecyclerView.adapter = adapter
    }

    private fun onUserClicked(chat : User) {
        if (source == SOURCE_SEARCH) {
            (activity as BaseFragmentActivity).addFragment(PrivateChatSettingsFragment.newInstance(true))
            return
        }

        if (source == SOURCE_NEW_PRIVATE_CHAT) {
            val chatActivityIntent = Intent(activity, ChatActivity::class.java)
            startActivity(chatActivityIntent)
            activity?.finishAfterTransition()
            return
        }
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
        val ARG_SOURCE = "param1"

        val SOURCE_SEARCH = 0
        val SOURCE_NEW_PRIVATE_CHAT = 1
        val SOURCE_NEW_GROUP_CHAT = 2

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param source Caller.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(source: Int): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            args.putInt(ARG_SOURCE, source)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
