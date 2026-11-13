package com.example.pickmeup.ui.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.target.PickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 픽업 불러오기 화면 */
@HiltViewModel
class PickupTargetSelectViewModel @Inject constructor(
    private val repository: PickupTargetRepository,
) : ViewModel() {

    /** 화면에 보여줄 최근 대상자 (최대 [MAX_VISIBLE]명, 나머지는 '전체 프로필 보기'에서) */
    val recentTargets: StateFlow<List<PickupTarget>> = repository.targets
        .map { it.take(MAX_VISIBLE) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            repository.targets.value.take(MAX_VISIBLE),
        )

    init {
        // 화면에 들어올 때마다 최신 목록 불러오기
        // TODO: 서버 연동 시 로딩·실패 안내 UI (Figma 미정)
        viewModelScope.launch { repository.refresh() }
    }

    companion object {
        /** Figma: 프로필 카드 3개 + 직접 입력 카드 */
        const val MAX_VISIBLE = 3
    }
}
