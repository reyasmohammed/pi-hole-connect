package com.tien.piholeconnect.ui.screen.log

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tien.piholeconnect.model.PiHoleLog
import com.tien.piholeconnect.model.RefreshableViewModel
import com.tien.piholeconnect.repository.IPiHoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(private val piHoleRepository: IPiHoleRepository) :
    RefreshableViewModel() {
    var logs: Iterable<PiHoleLog> by mutableStateOf(listOf())
        private set

    override suspend fun queueRefresh() {
        logs = piHoleRepository.getLogs(200).data
    }
}