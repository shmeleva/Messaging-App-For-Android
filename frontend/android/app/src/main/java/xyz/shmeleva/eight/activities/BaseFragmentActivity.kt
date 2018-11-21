package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment

open class BaseFragmentActivity(private val containerViewId: Int) : AppCompatActivity() {

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
                ?.beginTransaction()
                ?.replace(containerViewId, fragment)
                ?.commit()
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager
                ?.beginTransaction()
                ?.add(containerViewId, fragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    override fun onBackPressed() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            finishAfterTransition()
        }
    }
}
