package com.benmohammad.modelviewintent.stats

import com.benmohammad.modelviewintent.mvibase.MviAction

sealed class StatisticsAction: MviAction {

    object LoadStatisticsAction: StatisticsAction()
}