package xyz.shmeleva.eight.activities

import android.net.Uri
import android.os.Bundle
import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.fragments.*

class SearchActivity : BaseFragmentActivity(R.id.searchFragmentContainer),
        SearchFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        PrivateChatSettingsFragment.OnFragmentInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val source = intent?.extras?.getInt(SearchFragment.ARG_SOURCE) ?: 0
        val usersToExclude = intent?.extras?.getStringArrayList(SearchFragment.ARG_USERS_TO_EXCLUDE) ?: ArrayList<String>()
        replaceFragment(SearchFragment.newInstance(source, usersToExclude) as android.support.v4.app.Fragment)
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
