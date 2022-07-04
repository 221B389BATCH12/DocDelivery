package com.priyank.drdelivery.authentication.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.common.api.ApiException
import com.priyank.drdelivery.R
import com.priyank.drdelivery.authentication.GoogleApiContract
import com.priyank.drdelivery.authentication.LoginViewModel
import com.priyank.drdelivery.feature_track_pacakges.presentation.ShipmentDetailsActivity
import com.priyank.drdelivery.ui.theme.DrDeliveryTheme
import com.priyank.drdelivery.ui.theme.LightBlue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrDeliveryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    LoginUi()
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LoginUi(viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val openShipmentDetailsPage = viewModel.triggerEvent.observeAsState(initial = false)
    val signInRequestCode = 1

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = GoogleApiContract()) { task ->
            try {
                val gsa = task?.getResult(ApiException::class.java)

                if (gsa != null) {
                    viewModel.fetchSignInUser(
                        email = gsa.email,
                        name = gsa.displayName,
                        profilePhotoUrl = gsa.photoUrl.toString()
                    )
                } else {
                }
            } catch (e: ApiException) {
                Log.e("Error in AuthScreen%s", e.toString())
            }
        }

    val pagerState = rememberPagerState(pageCount = 3)
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->

            Slider(info = viewModel.data()[page])

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 700.dp),
                indicatorShape = RectangleShape,
                activeColor = LightBlue
            )
        }
        LaunchedEffect(key1 = pagerState.currentPage) {
            launch {
                delay(3000)
                with(pagerState) {
                    val target = if (currentPage < pageCount - 1) currentPage + 1 else 0

                    animateScrollToPage(
                        page = target,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
            if (openShipmentDetailsPage.value) {
                context.startActivity(Intent(context, ShipmentDetailsActivity::class.java))
            }
        }

        SignInButton(
            text = "Sign in with Google",
            loadingText = "Signing in...",
            isLoading = false,
            icon = painterResource(id = R.drawable.ic_google_login),
            onClick = { authResultLauncher.launch(signInRequestCode) },
        )
    }
}