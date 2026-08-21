package com.arrazyfathan.kbbi.feature.settings.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP

data class LegalDocumentSection(
    val titleRes: Int,
    val bodyRes: Int,
)

@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    LegalDocumentScreen(
        titleRes = R.string.privacy_policy_title,
        effectiveDateRes = R.string.legal_effective_date,
        sections = privacyPolicySections,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun TermsConditionsScreen(onNavigateBack: () -> Unit) {
    LegalDocumentScreen(
        titleRes = R.string.terms_condition_title,
        effectiveDateRes = R.string.legal_effective_date,
        sections = termsConditionsSections,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentScreen(
    titleRes: Int,
    effectiveDateRes: Int,
    sections: List<LegalDocumentSection>,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.background(Brush.verticalGradient(listOf(BlueSecondary, BluePrimary))),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                            tint = Color.White,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(titleRes),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = MetropolisFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(effectiveDateRes),
                style = MaterialTheme.typography.labelMedium,
                color = TextP,
            )
            sections.forEach { section ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextH1,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(section.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextP,
                        lineHeight = 25.sp,
                    )
                }
            }
        }
    }
}

private val privacyPolicySections = listOf(
    LegalDocumentSection(R.string.privacy_intro_title, R.string.privacy_intro_body),
    LegalDocumentSection(R.string.privacy_data_title, R.string.privacy_data_body),
    LegalDocumentSection(R.string.privacy_use_title, R.string.privacy_use_body),
    LegalDocumentSection(R.string.privacy_storage_title, R.string.privacy_storage_body),
    LegalDocumentSection(R.string.privacy_permissions_title, R.string.privacy_permissions_body),
    LegalDocumentSection(R.string.privacy_services_title, R.string.privacy_services_body),
    LegalDocumentSection(R.string.privacy_choices_title, R.string.privacy_choices_body),
    LegalDocumentSection(R.string.privacy_children_title, R.string.privacy_children_body),
    LegalDocumentSection(R.string.privacy_changes_title, R.string.privacy_changes_body),
    LegalDocumentSection(R.string.privacy_contact_title, R.string.privacy_contact_body),
)

private val termsConditionsSections = listOf(
    LegalDocumentSection(R.string.terms_acceptance_title, R.string.terms_acceptance_body),
    LegalDocumentSection(R.string.terms_service_title, R.string.terms_service_body),
    LegalDocumentSection(R.string.terms_content_title, R.string.terms_content_body),
    LegalDocumentSection(R.string.terms_user_responsibilities_title, R.string.terms_user_responsibilities_body),
    LegalDocumentSection(R.string.terms_third_party_title, R.string.terms_third_party_body),
    LegalDocumentSection(R.string.terms_ownership_title, R.string.terms_ownership_body),
    LegalDocumentSection(R.string.terms_disclaimer_title, R.string.terms_disclaimer_body),
    LegalDocumentSection(R.string.terms_changes_title, R.string.terms_changes_body),
    LegalDocumentSection(R.string.terms_contact_title, R.string.terms_contact_body),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PrivacyPolicyScreenPreview() {
    KBBITheme { PrivacyPolicyScreen(onNavigateBack = {}) }
}
