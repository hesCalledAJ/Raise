package com.alijafari.raise.feature_exceptions

import android.content.ClipData
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.alijafari.raise.BuildConfig
import com.alijafari.raise.core.ui.theme.Wakee2Theme
import kotlinx.coroutines.launch

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val stackTrace = CustomActivityOnCrash.getStackTraceFromIntent(intent)
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        val shareableText = """
        [CRASH REPORT]
        APP_V: ${BuildConfig.VERSION_NAME}
        API_V: ${Build.VERSION.SDK_INT}
        MODEL: ${Build.MODEL}
        ---
        ${stackTrace?.take(3000)} 
        """.trimIndent()

        setContent {
            val clipboardManager = LocalClipboard.current
            BSODScreen(
                errorDetails = stackTrace ?: "No details available",
                onShare = {
                    lifecycleScope.launch {
                        clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("Raise Crash Data",AnnotatedString(shareableText))))
                    }
                    Toast.makeText(this, "Copied to clipboard!", LENGTH_SHORT).show()
                },
                onRestart = {
                    CustomActivityOnCrash.restartApplication(this, config!!)
                }
            )
        }
    }
}

@Composable
fun BSODScreen(errorDetails: String,onShare : () -> Unit, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0078D7))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = ":(",
            fontSize = 100.sp,
            color = Color.White
        )

        Text(
            text = "Sorry , Raise ran into a problem . please report this details to the developer :",
            fontSize = 20.sp,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
        val scrollState = rememberScrollState()
        // The technical "Stop Code" area
        Text(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            text = errorDetails,
            fontSize = 12.sp,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                modifier = Modifier.padding(end = 3.dp),
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0078D7)
                )
            ) {
                Text("Restart")
            }
            OutlinedButton(
                onClick = onShare,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White))
            ) {
                Icon(
                    Icons.Rounded.ContentCopy,null
                )
            }

            Text(
                modifier = Modifier.weight(1f),
                text = "MANU-${Build.MANUFACTURER}\nMODEL-${Build.MODEL}\nRAISE_V-${BuildConfig.VERSION_NAME}\nAPI_V-${Build.VERSION.SDK_INT}",
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Wakee2Theme {
        BSODScreen(
            "testing this piece of shiiiii",{}
        ) { }
    }
}