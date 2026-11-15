package com.foodpal.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.foodpal.FoodPalApp

private enum class SettingsSubScreen { Main, Goals, Profile }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(section: String = "", onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as FoodPalApp
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))
    val state by viewModel.state.collectAsStateWithLifecycle()

    val initialSub = when (section) {
        "macros", "micros" -> SettingsSubScreen.Goals
        "profile" -> SettingsSubScreen.Profile
        else -> SettingsSubScreen.Main
    }
    var subScreen by remember { mutableStateOf(initialSub) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(when (subScreen) {
                    SettingsSubScreen.Main -> "Settings"
                    SettingsSubScreen.Goals -> "Goals"
                    SettingsSubScreen.Profile -> "Profile"
                }) },
                navigationIcon = {
                    if (subScreen != SettingsSubScreen.Main) {
                        androidx.compose.material3.IconButton(onClick = { subScreen = SettingsSubScreen.Main }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (subScreen) {
            SettingsSubScreen.Main -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsMenuItem(
                        icon = Icons.Filled.Flag,
                        title = "Goals",
                        subtitle = "Calorie, macro & micronutrient targets",
                        onClick = { subScreen = SettingsSubScreen.Goals },
                    )
                    SettingsMenuItem(
                        icon = Icons.Filled.Person,
                        title = "Profile",
                        subtitle = "Nickname, age, height, weight, lifestyle & goals",
                        onClick = { subScreen = SettingsSubScreen.Profile },
                    )
                }
            }
            SettingsSubScreen.Goals -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                ) {
                    GoalsSubScreen(
                        state = state,
                        scrollTarget = if (section == "macros" || section == "micros") section else null,
                        onUpdate = viewModel::update,
                    )
                }
            }
            SettingsSubScreen.Profile -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                ) {
                    ProfileSubScreen(
                        profile = state.profile,
                        onUpdate = viewModel::updateProfile,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
