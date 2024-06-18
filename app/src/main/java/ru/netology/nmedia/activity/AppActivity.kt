package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewAndEditPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PreviewPostFragment.Companion.longArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel


class AppActivity : AppCompatActivity(R.layout.activity_app) {

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment_container)
                .navigate(
                    R.id.action_feedFragment_to_newAndEditPostFragment,
                    Bundle().apply {
                        textArg = text
                        longArg = 0L
                    }
                )
        }

        checkGoogleApiAvailability()
        requestNotificationsPermission()

        var currentMenuProvider: MenuProvider? = null
        viewModel.auth.observe(this) {
            val authenticated = viewModel.authenticated

            currentMenuProvider?.let {
                removeMenuProvider(it)
            }
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.authenticated, authenticated)
                    menu.setGroupVisible(R.id.unauthenticated, !authenticated)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sign_in -> {
                            findNavController(R.id.nav_host_fragment_container).navigate(R.id.authFragment)
                            true
                        }

                        R.id.sign_up -> {
                            findNavController(R.id.nav_host_fragment_container).navigate(R.id.signUpFragment)
                            true
                        }

                        R.id.logout -> {
                            if (findNavController(R.id.nav_host_fragment_container).currentDestination?.id == R.id.newAndEditPostFragment) {
                                findNavController(R.id.nav_host_fragment_container).navigate(R.id.signOutDialogFragment)
                            } else AppAuth.getInstance().clearAuth()
                            true
                        }

                        else -> false
                    }
            }.also {
                currentMenuProvider = it
            })
        }
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}