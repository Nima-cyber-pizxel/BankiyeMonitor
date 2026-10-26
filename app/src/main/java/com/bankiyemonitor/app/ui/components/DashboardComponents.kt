package com.bankiyemonitor.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.ui.theme.*
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinancialSummaryCard(
    totalSent: Double,
    totalReceived: Double,
    transactionCount: Int,
    onViewSummary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "This Week's Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(onClick = onViewSummary) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "View full summary",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Received
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = CreditGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Received",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        formatEtb(totalReceived),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CreditGreen
                    )
                }

                // Total Sent
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = DebitRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Sent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        formatEtb(totalSent),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DebitRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "$transactionCount total transactions processed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SecurityOverviewCard(
    loginAttempts: Int,
    failedLogins: Int,
    passwordResets: Int,
    deviceChanges: Int,
    spoofedMessages: Int,
    highRiskEvents: Int,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highRiskEvents > 0 || spoofedMessages > 0)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (highRiskEvents > 0 || spoofedMessages > 0)
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (highRiskEvents > 0) RiskCritical else Verified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Security Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Security metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecurityMetric(
                    icon = Icons.Outlined.Login,
                    label = "Logins",
                    value = loginAttempts.toString(),
                    modifier = Modifier.weight(1f)
                )
                SecurityMetric(
                    icon = Icons.Outlined.ErrorOutline,
                    label = "Failed",
                    value = failedLogins.toString(),
                    isAlert = failedLogins > 0,
                    modifier = Modifier.weight(1f)
                )
                SecurityMetric(
                    icon = Icons.Outlined.LockReset,
                    label = "Resets",
                    value = passwordResets.toString(),
                    isAlert = passwordResets > 0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecurityMetric(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "Devices",
                    value = deviceChanges.toString(),
                    isAlert = deviceChanges > 0,
                    modifier = Modifier.weight(1f)
                )
                SecurityMetric(
                    icon = Icons.Outlined.Warning,
                    label = "Spoofed",
                    value = spoofedMessages.toString(),
                    isAlert = spoofedMessages > 0,
                    modifier = Modifier.weight(1f)
                )
                SecurityMetric(
                    icon = Icons.Outlined.GppBad,
                    label = "High Risk",
                    value = highRiskEvents.toString(),
                    isAlert = highRiskEvents > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SecurityMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isAlert) RiskCritical else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) RiskCritical else MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlertBanner(
    unresolvedEvents: Int,
    spoofedMessages: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = RiskCritical
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Security Alert",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Security Attention Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
                val message = buildString {
                    if (unresolvedEvents > 0) append("$unresolvedEvents unresolved events")
                    if (spoofedMessages > 0) {
                        if (isNotEmpty()) append(" • ")
                        append("$spoofedMessages spoofed messages")
                    }
                }
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun TransactionListItem(
    sms: ParsedSmsEntity,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, HH:mm") }
    val dateTime = remember(sms.timestamp) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(sms.timestamp),
            ZoneId.systemDefault()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (sms.riskLevel) {
                "CRITICAL" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                "HIGH" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            when (sms.riskLevel) {
                "CRITICAL", "HIGH" -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction type icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (sms.transactionType) {
                    "CREDIT" -> CreditGreen.copy(alpha = 0.1f)
                    "DEBIT" -> DebitRed.copy(alpha = 0.1f)
                    "LOGIN", "LOGIN_FAILED" -> RiskMedium.copy(alpha = 0.1f)
                    "SPOOFED" -> RiskCritical.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (sms.transactionType) {
                            "CREDIT" -> Icons.Default.ArrowDownward
                            "DEBIT" -> Icons.Default.ArrowUpward
                            "LOGIN" -> Icons.Default.Login
                            "LOGIN_FAILED" -> Icons.Default.ErrorOutline
                            "PASSWORD_RESET" -> Icons.Default.LockReset
                            "DEVICE_CHANGE" -> Icons.Default.PhoneAndroid
                            "OTP" -> Icons.Default.Pin
                            else -> Icons.Default.Email
                        },
                        contentDescription = null,
                        tint = when (sms.transactionType) {
                            "CREDIT" -> CreditGreen
                            "DEBIT" -> DebitRed
                            "LOGIN_FAILED" -> RiskCritical
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTransactionType(sms.transactionType),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (sms.amount != null) {
                        Text(
                            text = "${if (sms.transactionType == "CREDIT") "+" else "-"}${formatEtb(sms.amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sms.transactionType == "CREDIT") CreditGreen else DebitRed
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sms.bankName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dateTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Verification badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeColor, badgeText, badgeIcon) = when (sms.verificationStatus) {
                        "VERIFIED" -> Triple(Verified, "Verified", Icons.Default.VerifiedUser)
                        "SPOOFED" -> Triple(Spoofed, "Spoofed!", Icons.Default.GppBad)
                        else -> Triple(Unverified, "Unverified", Icons.Default.GppMaybe)
                    }
                    Icon(
                        badgeIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

fun formatEtb(amount: Double): String {
    return String.format(Locale.US, "ETB %,.2f", amount)
}

fun formatTransactionType(type: String): String {
    return when (type) {
        "CREDIT" -> "Money Received"
        "DEBIT" -> "Money Sent"
        "LOGIN" -> "Account Login"
        "LOGIN_FAILED" -> "Failed Login"
        "PASSWORD_RESET" -> "Password Reset"
        "DEVICE_CHANGE" -> "New Device"
        "OTP" -> "OTP Request"
        "BALANCE_INQUIRY" -> "Balance Check"
        "ACCOUNT_LOCKED" -> "Account Locked"
        "BILL_PAYMENT" -> "Bill Payment"
        "AIRTIME" -> "Airtime Purchase"
        else -> "Other Activity"
    }
}
