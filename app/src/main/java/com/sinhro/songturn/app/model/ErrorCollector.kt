package com.sinhro.songturn.app.model

import com.sinhro.songturn.app.view_models.ErrorViewModel
import com.sinhro.songturn.rest.core.CommonError

object ErrorCollectorProvider {
    val collectorForErrorViewModel: ((CommonError) -> Unit) by lazy {
        fun(ce: CommonError) {
            ErrorViewModel.getGlobalInstance()?.error(ce)
        }
    }
}