package se.w3footprint.korlog.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.w3footprint.korlog.R
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.presentation.common.theme.Green500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onDone: () -> Unit,
    viewModel: SessionSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val session = uiState.session ?: run {
        // Session not found — just go back
        LaunchedEffect(Unit) { onDone() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Green500,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.session_summary_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.session_summary_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        SummaryCard(session = session)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.session_summary_done),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SummaryCard(session: DrivingSession) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val drivingMillis = session.drivingDurationMillis
    val durationHours = (drivingMillis / 3_600_000).toInt()
    val durationMinutes = ((drivingMillis % 3_600_000) / 60_000).toInt()
    val hourlyRate = if (drivingMillis > 0)
        session.earningsSek / (drivingMillis / 3_600_000.0) else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Big earnings
            Text(
                text = "%,.0f kr".format(session.earningsSek),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Green500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.stats_earnings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(
                label = stringResource(R.string.session_driving_time),
                value = if (durationHours > 0) "$durationHours h $durationMinutes min"
                else "$durationMinutes min"
            )
            if (session.breakDurationMillis > 0) {
                val bH = (session.breakDurationMillis / 3_600_000).toInt()
                val bM = ((session.breakDurationMillis % 3_600_000) / 60_000).toInt()
                Spacer(modifier = Modifier.height(10.dp))
                SummaryRow(
                    label = stringResource(R.string.session_break_duration),
                    value = if (bH > 0) "$bH h $bM min" else "$bM min"
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                label = stringResource(R.string.session_detail_hourly_rate),
                value = if (hourlyRate > 0) "%,.0f kr/h".format(hourlyRate) else "—"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                label = stringResource(R.string.session_detail_start),
                value = timeFormatter.format(Date(session.startTime))
            )
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                label = stringResource(R.string.session_detail_end),
                value = timeFormatter.format(Date(session.endTime))
            )
            if (session.distanceKm > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                SummaryRow(
                    label = stringResource(R.string.stats_distance),
                    value = "%.0f km".format(session.distanceKm)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                label = stringResource(R.string.session_platform),
                value = session.platform.name.lowercase().replaceFirstChar { it.uppercase() }
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
