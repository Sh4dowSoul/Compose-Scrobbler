package de.schnettler.scrobbler

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.getValue
import androidx.ui.animation.Crossfade
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.livedata.observeAsState
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import com.github.zsoltk.compose.backpress.AmbientBackPressHandler
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router
import de.schnettler.repo.Repository
import de.schnettler.scrobbler.components.BottomNavigationBar
import de.schnettler.scrobbler.screens.*
import de.schnettler.scrobbler.util.SessionStatus
import de.schnettler.scrobbler.util.getViewModel
import de.schnettler.scrobbler.viewmodels.ChartsViewModel
import de.schnettler.scrobbler.viewmodels.MainViewModel
import de.schnettler.scrobbler.viewmodels.UserViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var repo: Repository
   private val model by lazy {
       getViewModel { MainViewModel(repo) }
    }
    private var userViewModel: UserViewModel? = null
    private var chartsViewModel: ChartsViewModel? = null

    private val backPressHandler = BackPressHandler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repo = Repository(this)

        setContent {
            Providers(
                AmbientBackPressHandler provides backPressHandler
            ) {
                MaterialTheme {
                    Router(defaultRouting = Screen.Local as Screen) {backStack ->
                        Scaffold(
                            topAppBar = {
                                TopAppBar(
                                    title = { Text(text = "Scrobbler") }
                                )
                            },
                            bodyContent = {
                                AppContent(backStack = backStack)
                            },
                            bottomAppBar = {
                                BottomNavigationBar(backStack = backStack, items = listOf(
                                    Screen.Charts,
                                    Screen.Local,
                                    Screen.History,
                                    Screen.Profile
                                ))
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AppContent(backStack: BackStack<Screen>) {
        val currentScreen = backStack.last()
        val status by model.sessionStatus.observeAsState()
        
        Crossfade(currentScreen) {screen ->
            when(screen) {
                is Screen.Charts -> {
                    if (chartsViewModel == null) {
                        chartsViewModel = getViewModel { ChartsViewModel(repo) }
                    }
                    ChartScreen(artistResponse = chartsViewModel!!.test)
                }
                is Screen.History -> HistoryScreen()
                is Screen.Local -> LocalScreen()
                is Screen.Profile -> {
                    Timber.d(status.toString())
                    when(status) {
                        is SessionStatus.LoggedOut -> LoginScreen(context = this)
                        is SessionStatus.LoggedIn -> {
                            if (userViewModel == null) {
                                userViewModel = getViewModel {
                                    UserViewModel(
                                        (status as SessionStatus.LoggedIn).session, repo
                                    )
                                }
                            }
                            ProfileScreen(getViewModel { userViewModel!! })
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!backPressHandler.handle()) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if ("${intent?.data?.scheme}://${intent?.data?.host}" == REDIRECT_URL) {
            //AuthResponse
            intent?.data?.getQueryParameter("token")?.let {token ->
                Timber.i("TOKEN: $token")
                model.onTokenReceived(token)
            }
        }
    }
}

const val AUTH_ENDPOINT = "https://www.last.fm/api/auth/"
const val REDIRECT_URL = "de.schnettler.scrobble://auth"
const val API_KEY = "***REPLACE_WITH_LASTFM_API_KEY***"