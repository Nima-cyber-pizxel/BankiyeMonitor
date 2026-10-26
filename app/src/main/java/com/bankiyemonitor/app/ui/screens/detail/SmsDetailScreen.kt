package com.bankiyemonitor.app.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bankiyemonitor.app.ui.components.formatEtb
import com.bankiyemonitor.app.ui.components.formatTransactionType
import com.bankiyemonitor.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetailScreen(
    smsId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SmsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(smsId) {
        viewModel.loadSms(smsId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val sms = uiState.sms
        if (sms == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss") }
            val dateTime = remember(sms.timestamp) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(sms.timestamp), ZoneId.systemDefault())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Verification Status Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (sms.verificationStatus) {
                                "VERIFIED" -> Verified.copy(alpha = 0.1f)
                                "SPOOFED" -> Spoofed.copy(alpha = 0.1f)
                                else -> Unverified.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (sms.verificationStatus) {
                                    "VERIFIED" -> Icons.Default.VerifiedUser
                                    "SPOOFED" -> Icons.Default.GppBad
                                    else -> Icons.Default.GppMaybe
                                },
                                contentDescription = null,
                                tint = when (sms.verificationStatus) {
                                    "VERIFIED" -> Verified
                                    "SPOOFED" -> Spoofed
                                    else -> Unverified
                                },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    when (sms.verificationStatus) {
                                        "VERIFIED" -> "Verified Sender"
                                        "SPOOFED" -> "POTENTIAL SPOOF DETECTED"
                                        else -> "Unverified Sender"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (sms.verificationStatus) {
                                        "VERIFIED" -> Verified
                                        "SPOOFED" -> Spoofed
                                        else -> Unverified
                                    }
                                )
                                Text(
                                    "From: ${sms.senderAddress}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Transaction Info Card
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Transaction Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow("Type", formatTransactionType(sms.transactionType))
                            DetailRow("Bank", sms.bankName)
                            DetailRow("Time", dateTime.format(dateFormatter))
                            
                            if (sms.amount != null) {
                                DetailRow("Amount", formatEtb(sms.amount))
                            }
                            if (sms.balance != null) {
                                DetailRow("Balance", formatEtb(sms.balance))
                            }
                            sms.counterparty?.let { DetailRow("Counterparty", it) }
                            sms.accountNumber?.let { DetailRow("Account", "****$it") }
                            sms.referenceNumber?.let { DetailRow("Reference", it) }
                            sms.deviceInfo?.let { DetailRow("Device", it) }
                        }
                    }
                }

                // Risk Assessment Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (sms.riskLevel) {
                                "CRITICAL" -> RiskCritical.copy(alpha = 0.1f)
                                "HIGH" -> RiskHigh.copy(alpha = 0.1f)
                                "MEDIUM" -> RiskMedium.copy(alpha = 0.1f)
                                else -> RiskLow.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Risk Assessment",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (sms.riskLevel) {
                                        "CRITICAL" -> RiskCritical
                                        "HIGH" -> RiskHigh
                                        "MEDIUM" -> RiskMedium
                                        else -> RiskLow
                                    }
                                ) {
                                    Text(
                                        sms.riskLevel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Urgency Score", "${sms.urgencyScore}/100")
                            DetailRow("Contains Links", if (sms.containsLink) "Yes" else "No")
                            DetailRow("OTP Detected", if (sms.otpDetected) "Yes" else "No")
                            
                            if (sms.riskReasons.isNotEmpty() && sms.riskReasons != "[]") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Risk Reasons:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val reasons = sms.riskReasons
                                    .removeSurrounding("[", "]")
                                    .split(",")
                                    .map { it.trim().removeSurrounding("\"") }
                                    .filter { it.isNotBlank() }
                                reasons.forEach { reason ->
                                    Text(
                                        "• $reason",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RiskCritical
                                    )
                                }
                            }
                        }
                    }
                }

                // Raw Message
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Original Message",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    sms.rawMessage,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
