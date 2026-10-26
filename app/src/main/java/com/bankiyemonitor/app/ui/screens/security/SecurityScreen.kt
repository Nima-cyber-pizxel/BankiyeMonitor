package com.bankiyemonitor.app.ui.screens.security

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import com.bankiyemonitor.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateToSmsDetail: (Long) -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Security Center", fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.unresolvedCount} unresolved events",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.unresolvedCount > 0) RiskCritical else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Icon(
                        if (uiState.unresolvedCount > 0) Icons.Default.GppBad else Icons.Default.GppGood,
                        contentDescription = null,
                        tint = if (uiState.unresolvedCount > 0) RiskCritical else Verified,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.GppGood,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Verified
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All Clear!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "No security events detected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.events) { event ->
                    SecurityEventCard(
                        event = event,
                        onResolve = { viewModel.resolveEvent(event.id) },
                        onViewSms = { onNavigateToSmsDetail(event.smsId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityEventCard(
    event: SecurityEventEntity,
    onResolve: () -> Unit,
    onViewSms: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm") }
    val dateTime = remember(event.timestamp) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.timestamp),
            ZoneId.systemDefault()
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (event.riskLevel) {
                "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                "HIGH" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                "MEDIUM" -> TertiaryContainerLight
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (event.riskLevel) {
                            "CRITICAL" -> RiskCritical
                            "HIGH" -> RiskHigh
                            "MEDIUM" -> RiskMedium
                            else -> RiskLow
                        }
                    ) {
                        Text(
                            event.riskLevel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                event.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        event.bankName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        dateTime.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    TextButton(onClick = onViewSms) {
                        Text("View SMS")
                    }
                    if (!event.isResolved) {
                        Button(
                            onClick = onResolve,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Verified
                            )
                        ) {
                            Text("Resolve")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Verified.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "✓ Resolved",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = Verified,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
