package com.distasilucas.cryptobalancetracker.model.response.goal

data class PageGoalResponse(
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val goals: List<GoalResponse>
) {

    constructor(page: Int, totalPages: Int, goals: List<GoalResponse>) : this(
        page + 1,
        totalPages,
        totalPages - 1 > page,
        goals
    )
}