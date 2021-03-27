package com.tien.piholeconnect.ui.screen.filterRules

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.tien.piholeconnect.model.*
import com.tien.piholeconnect.repository.IPiHoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

@HiltViewModel
class FilterRulesViewModel @Inject constructor(private val piHoleRepository: IPiHoleRepository) :
    RefreshableViewModel() {
    enum class Tab {
        WHITE,
        BLACK
    }

    var rules: Iterable<PiHoleFilterRule> by mutableStateOf(listOf())
        private set
    var selectedTab by mutableStateOf(Tab.WHITE)

    var addRuleInputValue by mutableStateOf("")
    var addRuleIsWildcardChecked by mutableStateOf(false)

    override suspend fun queueRefresh() {
        rules =
            RuleType.values().map { viewModelScope.async { piHoleRepository.getFilterRules(it) } }
                .awaitAll()
                .flatMap { it.data }
    }

    suspend fun addRule() {
        val ruleType = when (selectedTab) {
            Tab.WHITE -> if (addRuleIsWildcardChecked) RuleType.REGEX_WHITE else RuleType.WHITE
            Tab.BLACK -> if (addRuleIsWildcardChecked) RuleType.REGEX_BLACK else RuleType.BLACK
        }

        val trimmedDomain = addRuleInputValue.trim()
        val parsedDomain =
            if (addRuleIsWildcardChecked) "$WILDCARD_REGEX_PREFIX$trimmedDomain$WILDCARD_REGEX_SUFFIX" else trimmedDomain

        piHoleRepository.addFilterRules(parsedDomain, ruleType = ruleType)
        resetAddRuleDialogInputs()
        refresh()
    }

    fun resetAddRuleDialogInputs() {
        addRuleInputValue = ""
        addRuleIsWildcardChecked = false
    }
}