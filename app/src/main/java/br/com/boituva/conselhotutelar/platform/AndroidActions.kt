package br.com.boituva.conselhotutelar.platform

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import br.com.boituva.conselhotutelar.presentation.ExternalAction

internal class AndroidActions(private val activity: Context) {
    fun open(action: ExternalAction): Boolean = try {
        activity.startActivity(action.toIntent())
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    fun copyPhone(name: String, number: String) {
        activity.getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(ClipData.newPlainText(name, number))
    }
}

internal fun ExternalAction.toIntent(): Intent = when (this) {
    is ExternalAction.Dial -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    is ExternalAction.OpenLink -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
    is ExternalAction.OpenMap -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}"))
    is ExternalAction.ShareLink -> Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url),
        null,
    )
}
