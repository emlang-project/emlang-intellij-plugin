package com.emlang.intellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "EmlangSettings",
    storages = [Storage("emlang.xml")]
)
class EmlangSettings : PersistentStateComponent<EmlangSettings.State> {

    private var myState = State()

    class State {
        var binaryPath: String = ""
        var configPath: String = ""
        var autoRefresh: Boolean = true
        var refreshDelayMs: Int = 500
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        @JvmStatic
        fun getInstance(): EmlangSettings {
            return ApplicationManager.getApplication().getService(EmlangSettings::class.java)
        }
    }
}
