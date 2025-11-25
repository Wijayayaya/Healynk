package com.example.healynk.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.healynk.R
import com.example.healynk.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isAuthenticated: Boolean,
    hasPin: Boolean,
    pinVerified: Boolean,
    onNavigate: (String) -> Unit,
    onCheckState: () -> Unit
) {
    LaunchedEffect(Unit) {
        onCheckState()
        delay(1500)
        val nextRoute = when {
            !isAuthenticated -> Constants.ROUTE_LOGIN
            !hasPin -> Constants.ROUTE_PIN_SETUP
            !pinVerified -> Constants.ROUTE_PIN_AUTH
            else -> Constants.ROUTE_MAIN
        }
        onNavigate(nextRoute)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_healynk_logo),
            contentDescription = stringResource(id = R.string.app_name)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 32.dp)
                .semantics { contentDescription = "Loading" },
            color = MaterialTheme.colorScheme.primary
        )
    }
}

