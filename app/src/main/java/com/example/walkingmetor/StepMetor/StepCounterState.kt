package com.example.walkingmetor.StepMetor

import java.time.LocalDate

data class StepCounterState(
    val date: LocalDate,
    val steps: Int
)