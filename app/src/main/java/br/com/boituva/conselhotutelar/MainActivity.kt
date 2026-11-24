package br.com.boituva.conselhotutelar

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val channels = loadChannels(assets)
        setContent {
            HelpApp(
                channels = channels,
                openAction = ::openAction,
                copyPhone = { name, number ->
                    getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText(name, number))
                },
            )
        }
    }

    private fun openAction(action: ExternalAction): Boolean = try {
        startActivity(action.toIntent())
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

internal fun ExternalAction.toIntent(): Intent = when (this) {
    is ExternalAction.Dial -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    is ExternalAction.OpenLink -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
}
