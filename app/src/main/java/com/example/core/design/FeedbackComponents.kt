package com.example.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.linguistics.model.CorrectionIssue
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * 14. EmptyState — État vide pédagogique et sobre.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = semantic.textSecondary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = EmptyState(icon, title, message, actionLabel, onAction, modifier)

/**
 * 15. LoadingState — Indicateur de chargement non bloquant et discret.
 */
@Composable
fun LoadingState(
    message: String = "Analyse en cours…",
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = semantic.textSecondary
            )
        }
    }
}

@Composable
fun LoadingStateView(
    message: String = "Analyse locale en cours…",
    modifier: Modifier = Modifier
) = LoadingState(message, modifier)

/**
 * 16. ErrorState — Message d'erreur clair avec action de récupération optionnelle.
 */
@Composable
fun ErrorState(
    title: String,
    message: String,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
        color = semantic.dangerContainer,
        border = BorderStroke(1.dp, semantic.danger.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(FrancaisProDimens.Space16)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = semantic.danger,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = semantic.onDangerContainer
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = semantic.onDangerContainer
            )
            if (retryLabel != null && onRetry != null) {
                Spacer(Modifier.height(12.dp))
                SecondaryButton(text = retryLabel, onClick = onRetry, contentColor = semantic.danger)
            }
        }
    }
}

/**
 * 17. SuccessFeedback — Bandeau de réussite calme et valorisant.
 */
@Composable
fun SuccessFeedback(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
        color = semantic.successContainer,
        border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(FrancaisProDimens.Space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = semantic.success,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = semantic.onSuccessContainer
                )
                if (!message.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onSuccessContainer
                    )
                }
            }
        }
    }
}

/**
 * 18. ErrorDetailBottomSheet — Panneau pédagogique ouvert au clic sur une faute surlignée.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDetailBottomSheet(
    issue: CorrectionIssue,
    onAccept: () -> Unit,
    onDismissIssue: () -> Unit,
    onClose: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val (catColor, catBg) = issueSemanticPalette(issue.category, semantic)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = FrancaisProDimens.RadiusSurface, topEnd = FrancaisProDimens.RadiusSurface),
        containerColor = semantic.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = FrancaisProDimens.Space20, vertical = FrancaisProDimens.Space12)
                .testTag("error_detail_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(label = issue.category.labelFr, color = catColor, containerColor = catBg)
                Text(
                    text = "Confiance ${(issue.confidence * 100).toInt()} %",
                    style = FrancaisProTextStyles.MonoMetric,
                    color = semantic.textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                color = semantic.surfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = issue.originalFragment,
                        style = MaterialTheme.typography.titleSmall,
                        color = semantic.danger,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text("→", style = MaterialTheme.typography.titleMedium, color = semantic.textSecondary)
                    Text(
                        text = issue.suggestion,
                        style = MaterialTheme.typography.titleMedium,
                        color = semantic.success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Explication", style = MaterialTheme.typography.labelLarge, color = semantic.textSecondary)
                Text(issue.explanation, style = MaterialTheme.typography.bodyLarge)
            }

            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Exemple correct", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(issue.example, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (issue.suggestion != issue.originalFragment) {
                    PrimaryButton(
                        text = "Accepter",
                        onClick = {
                            onAccept()
                            onClose()
                        },
                        icon = Icons.Filled.Check,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sheet_accept_btn")
                    )
                }
                SecondaryButton(
                    text = "Ignorer",
                    onClick = {
                        onDismissIssue()
                        onClose()
                    },
                    icon = Icons.Filled.Close,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_ignore_btn")
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * 19. ConfirmationDialog — Dialogue de confirmation avant toute action destructive.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String = "Annuler",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSurface),
        containerColor = semantic.elevatedSurface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = semantic.textSecondary) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_dialog_confirm_btn")
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDestructive) semantic.danger else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("confirm_dialog_cancel_btn")
            ) {
                Text(
                    text = cancelLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = semantic.textSecondary
                )
            }
        }
    )
}
