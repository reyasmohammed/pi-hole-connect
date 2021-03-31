package com.tien.piholeconnect.ui.screen.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Router
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.tien.piholeconnect.model.Screen
import com.tien.piholeconnect.model.TemperatureUnit
import com.tien.piholeconnect.model.Theme
import com.tien.piholeconnect.model.UserPreferences
import kotlinx.coroutines.launch
import androidx.compose.ui.text.intl.Locale.Companion as Locale

private val PreferenceItemModifier = Modifier
    .fillMaxWidth()
    .height(56.dp)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = viewModel(),
    navController: NavHostController
) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = UserPreferences.getDefaultInstance())

    if (userPreferences === UserPreferences.getDefaultInstance()) return

    Column(Modifier.padding(vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            "My Pi-holes",
            modifier = Modifier.padding(horizontal = 15.dp),
            style = MaterialTheme.typography.caption
        )
        userPreferences.piHoleConnectionsList.forEach {
            ListItem(
                Modifier.clickable { navController.navigate("${Screen.PiHoleConnection.route}?id=${it.id}") },
                icon = { Icon(Icons.Default.Router, contentDescription = "Pi-hole ${it.name}") },
                text = { Text(it.name) },
                secondaryText = { Text(it.host) })
        }
        ListItem(
            Modifier.clickable { navController.navigate(Screen.PiHoleConnection.route) },
            icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Pi-hole") },
            text = { Text("Add Pi-hole") })
        Column(
            Modifier
                .padding(horizontal = 15.dp)
                .selectableGroup()
        ) {
            Text("Theme", style = MaterialTheme.typography.caption)
            Theme.values()
                .filter { it != Theme.UNRECOGNIZED }
                .forEach { theme ->
                    Row(
                        PreferenceItemModifier
                            .selectable(
                                selected = theme == userPreferences.theme,
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.updateUserPreferences {
                                            it
                                                .toBuilder()
                                                .setTheme(theme)
                                                .build()
                                        }
                                    }
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(end = 32.dp),
                            selected = theme == userPreferences.theme,
                            onClick = null
                        )
                        Text(
                            text = theme.name.toLowerCase(Locale.current)
                                .capitalize(Locale.current)
                        )
                    }
                }
        }
        Column(
            Modifier
                .padding(horizontal = 15.dp)
                .selectableGroup()
        ) {
            Text("Temperature display", style = MaterialTheme.typography.caption)
            TemperatureUnit.values()
                .filter { it != TemperatureUnit.UNRECOGNIZED }
                .forEach { temperatureUnit ->
                    Row(
                        PreferenceItemModifier
                            .selectable(
                                selected = temperatureUnit == userPreferences.temperatureUnit,
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.updateUserPreferences {
                                            it
                                                .toBuilder()
                                                .setTemperatureUnit(temperatureUnit)
                                                .build()
                                        }
                                    }
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(end = 32.dp),
                            selected = temperatureUnit == userPreferences.temperatureUnit,
                            onClick = null
                        )
                        Text(
                            text = temperatureUnit.name.toLowerCase(Locale.current)
                                .capitalize(Locale.current)
                        )
                    }
                }
        }
    }
}