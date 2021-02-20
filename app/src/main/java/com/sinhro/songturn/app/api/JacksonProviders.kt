package com.sinhro.songturn.app.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.converter.jackson.JacksonConverterFactory

object JacksonObjectMapperProvider {
    val objectMapper: ObjectMapper by lazy {
        val om = ObjectMapper()
//        om.registerModule(JavaTimeModule())
        om.registerModule(KotlinModule())
        om.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)

        return@lazy om
    }
}

object JacksonConvertingFactoryProvider {
    val convertingFactory =
        JacksonConverterFactory.create(JacksonObjectMapperProvider.objectMapper)
}