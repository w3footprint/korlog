package se.w3footprint.korlog.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.w3footprint.korlog.R
import se.w3footprint.korlog.domain.model.ComplianceStatus
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.WorkStats
import se.w3footprint.korlog.presentation.common.theme.Green500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onStartSession: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { GreetingHeader() }

        item {
            StartSessionButton(
                hasActiveSession = uiState.hasActiveSession,
                onClick = onStartSession
            )
        }

        item { SectionLabel(stringResource(R.string.dashboard_this_week)) }
        item { WeeklyStatsRow(stats = uiState.weeklyStats) }
        item { ComplianceCard(compliance = uiState.compliance) }

        item { SectionLabel(stringResource(R.string.dashboard_this_month)) }
        item { MonthlyStatsCard(stats = uiState.monthlyStats) }

        if (uiState.recentSessions.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.dashboard_recent_sessions)) }
            items(uiState.recentSessions) { session ->
                SessionRow(session = session, onClick = { onSessionClick(session.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun GreetingHeader() {
    Column {
        Text(
            text = stringResource(R.string.dashboard_greeting),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StartSessionButton(hasActiveSession: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (hasActiveSession) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (hasActiveSession) stringResource(R.string.session_active_continue)
            else stringResource(R.string.session_start),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun WeeklyStatsRow(stats: WorkStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.AccessTime,
            label = stringResource(R.string.stats_hours), value = "%.1f h".format(stats.totalHours))
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.AttachMoney,
            label = stringResource(R.string.stats_earnings), value = "%,.0f kr".format(stats.totalEarningsSek))
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.DirectionsCar,
            label = stringResource(R.string.stats_distance), value = "%.0f km".format(stats.totalDistanceKm))
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ComplianceCard(compliance: ComplianceStatus) {
    val warningColor = Color(0xFFF59E0B)
    val progressColor = when {
        compliance.isWeeklyHardLimitExceeded -> MaterialTheme.colorScheme.error
        compliance.isWeeklyAverageLimitExceeded -> warningColor
        else -> Green500
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.compliance_weekly_limit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (compliance.isWeeklyAverageLimitExceeded) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = if (compliance.isWeeklyHardLimitExceeded) MaterialTheme.colorScheme.error else warningColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { compliance.weeklyProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "%.1f h".format(compliance.weeklyHours),
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${compliance.weeklyHardLimitHours.roundToInt()} h max",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (compliance.isWeeklyHardLimitExceeded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.compliance_hard_limit_warning),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            } else if (compliance.isWeeklyAverageLimitExceeded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.compliance_average_warning),
                    style = MaterialTheme.typography.labelSmall, color = warningColor)
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(stats: WorkStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MonthlyStatItem(label = stringResource(R.string.stats_hours), value = "%.0f h".format(stats.totalHours))
            MonthlyStatItem(label = stringResource(R.string.stats_earnings), value = "%,.0f kr".format(stats.totalEarningsSek))
            MonthlyStatItem(label = stringResource(R.string.stats_sessions), value = stats.sessionCount.toString())
        }
    }
}

@Composable
private fun MonthlyStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SessionRow(session: DrivingSession, onClick: () -> Unit) {
    val dateFormatter = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
    val durationHours = (session.durationMillis / 3_600_000).toInt()
    val durationMinutes = ((session.durationMillis % 3_600_000) / 60_000).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = dateFormatter.format(Date(session.date)),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = session.platform.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "%,.0f kr".format(session.earningsSek),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Green500)
                Text(
                    text = if (durationHours > 0) "$durationHours h $durationMinutes min" else "$durationMinutes min",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
