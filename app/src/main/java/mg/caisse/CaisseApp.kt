package mg.caisse

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mg.caisse.data.local.AppDatabase
import mg.caisse.data.repository.CaisseRepository
import mg.caisse.data.repository.SettingsRepository

class CaisseApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: CaisseRepository by lazy { CaisseRepository(database) }
    val settings: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.ensureSeed() }
    }
}
