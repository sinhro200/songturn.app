package com.sinhro.songturn.app.ui.objects

import com.sinhro.songturn.rest.validation.Validator

object ValidatorProvider {
    val validator: Validator by lazy { Validator() }
}