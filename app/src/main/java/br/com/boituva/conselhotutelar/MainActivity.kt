package br.com.boituva.conselhotutelar

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.boituva.conselhotutelar.data.OfflineCatalogRepository
import br.com.boituva.conselhotutelar.platform.AndroidActions
import br.com.boituva.conselhotutelar.presentation.CouncilViewModel
import br.com.boituva.conselhotutelar.presentation.ui.CouncilRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.rgb(23, 59, 50)),
        )

        val factory = viewModelFactory {
            initializer {
                CouncilViewModel(OfflineCatalogRepository(applicationContext.assets), createSavedStateHandle())
            }
        }
        val viewModel = ViewModelProvider(this, factory)[CouncilViewModel::class.java]
        val actions = AndroidActions(this)
        setContent {
            CouncilRoute(viewModel, openAction = actions::open, copyPhone = actions::copyPhone)
        }
    }
}
