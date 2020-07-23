package com.benmohammad.modelviewintent.stats

import com.benmohammad.modelviewintent.mvibase.MviIntent

sealed class StatisticsIntent: MviIntent {

    object InitialIntent: StatisticsIntent()
}