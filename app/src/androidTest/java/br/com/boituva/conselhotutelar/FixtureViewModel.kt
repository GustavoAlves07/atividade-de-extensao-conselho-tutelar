package br.com.boituva.conselhotutelar

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.platform.app.InstrumentationRegistry
import br.com.boituva.conselhotutelar.data.OfflineCatalogRepository
import br.com.boituva.conselhotutelar.presentation.CouncilViewModel

internal fun ComponentActivity.fixtureViewModel(): CouncilViewModel {
    val assets = InstrumentationRegistry.getInstrumentation().context.assets
    val factory = viewModelFactory {
        initializer { CouncilViewModel(OfflineCatalogRepository(assets), createSavedStateHandle()) }
    }
    return ViewModelProvider(this, factory)[CouncilViewModel::class.java]
}
