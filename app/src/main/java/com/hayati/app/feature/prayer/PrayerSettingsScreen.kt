package com.hayati.app.feature.prayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.AppViewModelProvider
import com.hayati.app.R
import com.hayati.app.core.common.labelRes
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.core.notifications.PrayerNotificationPresenter
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.KnownCities
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerNotificationConfig
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.usecase.ManualLocationError
import java.time.ZoneId

@Composable
fun PrayerSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshEnvironment() }

    Column(modifier = modifier.fillMaxSize()) {
        HayatiTopBar(title = stringResource(R.string.ps_title), onBack = onBack)
        if (settings == null) return@Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(HayatiSpacing.ScreenHorizontal, HayatiSpacing.ScreenVertical),
            verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
        ) {
            LocationSection(state, settings, viewModel)
            CalculationSection(settings, viewModel)
            NotificationSection(state, settings, viewModel)
            DisplaySection(settings, viewModel)
        }
    }
}

// ------------------------------------------------------------------ Location

@Composable
private fun LocationSection(state: PrayerSettingsUiState, settings: PrayerSettings, vm: PrayerSettingsViewModel) {
    val context = LocalContext.current
    var showManual by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.detectLocation() else vm.onLocationPermissionDenied()
    }

    SettingsCard(stringResource(R.string.ps_location_title)) {
        val location = settings.location
        Text(
            text = if (location == null) {
                stringResource(R.string.ps_location_none)
            } else {
                locationTitle(location) + " · " + locationSubtitle(location)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Note(stringResource(R.string.ps_location_privacy))
        state.locationMessage?.let {
            Text(
                text = stringResource(
                    when (it) {
                        LocationMessage.PERMISSION_DENIED -> R.string.ps_location_denied
                        LocationMessage.SERVICES_DISABLED -> R.string.ps_location_disabled
                        LocationMessage.UNAVAILABLE -> R.string.ps_location_unavailable
                    },
                ),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !state.detecting,
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                    if (granted) vm.detectLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
            ) { Text(stringResource(if (state.detecting) R.string.ps_detecting else R.string.ps_detect)) }
            OutlinedButton(onClick = { showManual = true }) { Text(stringResource(R.string.ps_manual)) }
        }
    }

    if (showManual) {
        ManualLocationDialog(
            errors = state.manualErrors,
            onDismiss = {
                vm.clearManualErrors()
                showManual = false
            },
            onCity = {
                vm.selectCity(it)
                showManual = false
            },
            onSave = { name, country, lat, lon, zone ->
                if (vm.saveManualLocation(name, country, lat, lon, zone)) showManual = false
            },
        )
    }
}

@Composable
private fun ManualLocationDialog(
    errors: Set<ManualLocationError>,
    onDismiss: () -> Unit,
    onCity: (com.hayati.app.domain.prayer.model.KnownCity) -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf("") }
    var lon by rememberSaveable { mutableStateOf("") }
    var zone by rememberSaveable { mutableStateOf(ZoneId.systemDefault().id) }
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ps_manual_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.ps_manual_cities), style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Column(modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                    KnownCities.search(query).forEach { city ->
                        TextButton(onClick = { onCity(city) }, modifier = Modifier.fillMaxWidth()) {
                            Text(city.name + " · " + city.country, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                HorizontalDivider()
                Text(stringResource(R.string.ps_manual_or), style = MaterialTheme.typography.titleSmall)
                Field(name, { name = it }, stringResource(R.string.ps_manual_name))
                Field(country, { country = it }, stringResource(R.string.ps_manual_country))
                Field(lat, { lat = it }, stringResource(R.string.ps_manual_lat), decimal, ManualLocationError.LATITUDE in errors, R.string.ps_manual_error_lat)
                Field(lon, { lon = it }, stringResource(R.string.ps_manual_lon), decimal, ManualLocationError.LONGITUDE in errors, R.string.ps_manual_error_lon)
                Field(zone, { zone = it }, stringResource(R.string.ps_manual_zone), KeyboardOptions.Default, ManualLocationError.ZONE in errors, R.string.ps_manual_error_zone)
            }
        },
        confirmButton = { Button(onClick = { onSave(name, country, lat, lon, zone) }) { Text(stringResource(R.string.ps_manual_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ps_cancel)) } },
    )
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorRes: Int = 0,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboard,
        supportingText = if (isError && errorRes != 0) ({ Text(stringResource(errorRes)) }) else null,
        modifier = Modifier.fillMaxWidth(),
    )
}

// --------------------------------------------------------------- Calculation

@Composable
private fun CalculationSection(settings: PrayerSettings, vm: PrayerSettingsViewModel) {
    var showMethods by rememberSaveable { mutableStateOf(false) }
    SettingsCard(stringResource(R.string.ps_calc_title)) {
        Text(stringResource(R.string.ps_method_label), style = MaterialTheme.typography.titleSmall, color = LocalHayatiColors.current.textSecondary)
        OutlinedButton(onClick = { showMethods = true }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(stringResource(settings.calculation.method.labelRes()))
        }
        Text(
            stringResource(R.string.ps_asr_label),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            color = LocalHayatiColors.current.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            AsrMethod.entries.forEach { asr ->
                FilterChip(
                    selected = settings.calculation.asr == asr,
                    onClick = { vm.setAsr(asr) },
                    label = { Text(stringResource(asr.labelRes())) },
                )
            }
        }
        Text(
            stringResource(R.string.ps_adjust_title),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            color = LocalHayatiColors.current.textSecondary,
        )
        Prayer.entries.forEach { prayer ->
            val minutes = settings.calculation.adjustments.of(prayer)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(prayer.labelRes()), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = { vm.adjust(prayer, -1) }) { Text("−") }
                Text(stringResource(R.string.ps_adjust_value, minutes), color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = { vm.adjust(prayer, 1) }) { Text("+") }
            }
        }
    }

    if (showMethods) {
        AlertDialog(
            onDismissRequest = { showMethods = false },
            title = { Text(stringResource(R.string.ps_method_label)) },
            text = {
                Column(modifier = Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.calculation.method == method,
                                    onClick = {
                                        vm.setMethod(method)
                                        showMethods = false
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = settings.calculation.method == method, onClick = null)
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(stringResource(method.labelRes()))
                                if (method == CalculationMethod.EGYPTIAN) {
                                    Text(
                                        stringResource(R.string.ps_method_recommended),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LocalHayatiColors.current.gold,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMethods = false }) { Text(stringResource(R.string.ps_cancel)) } },
        )
    }
}

// ------------------------------------------------------------- Notifications

@Composable
private fun NotificationSection(state: PrayerSettingsUiState, settings: PrayerSettings, vm: PrayerSettingsViewModel) {
    val context = LocalContext.current
    val notifications = settings.notifications
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.setNotificationsEnabled(true) else vm.onNotificationPermissionDenied()
    }

    SettingsCard(stringResource(R.string.ps_notif_title)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ps_notif_master), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Note(stringResource(R.string.ps_notif_master_desc))
            }
            Switch(
                checked = notifications.enabled,
                onCheckedChange = { wantOn ->
                    if (!wantOn) {
                        vm.setNotificationsEnabled(false)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.setNotificationsEnabled(true)
                    }
                },
            )
        }
        if (notifications.enabled && (!state.notificationsAllowed || state.notificationPermissionDenied)) {
            Text(
                stringResource(R.string.ps_notif_denied),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { context.startActivity(appNotificationSettingsIntent(context)) }) {
                Text(stringResource(R.string.ps_notif_open_system))
            }
        }
        if (notifications.enabled) {
            ExactAlarmStatus(state.exactAlarmsAvailable, context)
            Prayer.entries.forEach { prayer -> PrayerNotificationRow(prayer, notifications.config(prayer), vm) }
            Note(stringResource(R.string.ps_notif_sound_note))
            OutlinedButton(onClick = { context.startActivity(channelSettingsIntent(context)) }) {
                Text(stringResource(R.string.ps_notif_open_system))
            }
        }
    }
}

@Composable
private fun ExactAlarmStatus(exactAvailable: Boolean, context: Context) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LocalHayatiColors.current.border)
    Text(stringResource(R.string.ps_exact_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    Text(
        stringResource(if (exactAvailable) R.string.ps_exact_ok else R.string.ps_exact_off),
        style = MaterialTheme.typography.bodySmall,
        color = if (exactAvailable) LocalHayatiColors.current.textSecondary else LocalHayatiColors.current.gold,
    )
    if (!exactAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        OutlinedButton(onClick = {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + context.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }) { Text(stringResource(R.string.ps_exact_open)) }
    }
}

@Composable
private fun PrayerNotificationRow(prayer: Prayer, config: PrayerNotificationConfig, vm: PrayerSettingsViewModel) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LocalHayatiColors.current.border)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.ps_notif_prayer_enabled, stringResource(prayer.labelRes())),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(checked = config.enabled, onCheckedChange = { vm.setPrayerNotification(prayer, it) })
    }
    if (config.enabled) {
        Text(stringResource(R.string.ps_notif_reminder), style = MaterialTheme.typography.bodySmall, color = LocalHayatiColors.current.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrayerNotificationConfig.REMINDER_OPTIONS.forEach { minutes ->
                FilterChip(
                    selected = config.reminderMinutes == minutes,
                    onClick = { vm.setReminder(prayer, minutes) },
                    label = {
                        Text(if (minutes == 0) stringResource(R.string.ps_notif_reminder_none) else stringResource(R.string.ps_notif_reminder_minutes, minutes))
                    },
                )
            }
        }
        Text(stringResource(R.string.ps_notif_sound), style = MaterialTheme.typography.bodySmall, color = LocalHayatiColors.current.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationSound.entries.forEach { sound ->
                FilterChip(
                    selected = config.sound == sound,
                    onClick = { vm.setSound(prayer, sound) },
                    label = {
                        Text(stringResource(if (sound == NotificationSound.SILENT) R.string.ps_notif_sound_silent else R.string.ps_notif_sound_default))
                    },
                )
            }
        }
    }
}

private fun appNotificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private fun channelSettingsIntent(context: Context): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, PrayerNotificationPresenter.CHANNEL_SOUND)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
        appNotificationSettingsIntent(context)
    }

// ------------------------------------------------------------------- Display

@Composable
private fun DisplaySection(settings: PrayerSettings, vm: PrayerSettingsViewModel) {
    SettingsCard(stringResource(R.string.ps_display_title)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.ps_display_24h), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = settings.use24Hour, onCheckedChange = vm::setUse24Hour)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.ps_hijri_label), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = { vm.adjustHijri(-1) }) { Text("−") }
            Text(stringResource(R.string.ps_adjust_value, settings.hijriOffsetDays), color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = { vm.adjustHijri(1) }) { Text("+") }
        }
        Note(stringResource(R.string.ps_hijri_note))
    }
}

// -------------------------------------------------------------------- Common

@Composable
private fun SettingsCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = LocalHayatiColors.current.textSecondary,
    )
}
