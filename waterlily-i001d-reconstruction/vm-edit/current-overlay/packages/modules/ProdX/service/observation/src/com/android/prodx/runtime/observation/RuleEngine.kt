package com.android.prodx.runtime.observation

data class RuleDefinition(
    val id: String,
    val name: String,
    val description: String,
    val condition: RuleCondition,
    val severity: IncidentSeverity,
    val recommendedAction: String = ""
)

sealed class RuleCondition {
    data class And(val conditions: List<RuleCondition>) : RuleCondition()
    data class Or(val conditions: List<RuleCondition>) : RuleCondition()
    data class Not(val inner: RuleCondition) : RuleCondition()
    data class SequenceDetected(
        val eventTypes: List<String>,
        val withinWindowMs: Long
    ) : RuleCondition()
    data class ThresholdExceeded(
        val metric: String,
        val maxCount: Int,
        val windowMs: Long
    ) : RuleCondition()
    data class PatternMatch(
        val field: String,
        val pattern: String
    ) : RuleCondition()
}

data class RuleMatch(
    val ruleId: String,
    val matchedAt: Long,
    val evidence: Map<String, Any?>
)

class RuleEngine {
    private val rules = mutableListOf<RuleDefinition>()

    fun registerRule(rule: RuleDefinition) {
        rules.add(rule)
    }

    fun unregisterRule(ruleId: String): Boolean {
        return rules.removeAll { it.id == ruleId }
    }

    fun getRules(): List<RuleDefinition> = rules.toList()

    fun evaluate(context: RuleEvaluationContext): List<IncidentRecord> {
        val incidents = mutableListOf<IncidentRecord>()
        for (rule in rules) {
            val result = evaluateCondition(rule.condition, context)
            if (result.matched) {
                incidents.add(
                    IncidentRecord(
                        id = "inc_${rule.id}_${context.sequence}",
                        timestamp = System.currentTimeMillis(),
                        severity = rule.severity,
                        source = context.sourceId,
                        description = rule.description,
                        confidence = result.confidence,
                        provenanceChain = context.provenanceChain + rule.id,
                        recommendedAction = rule.recommendedAction
                    )
                )
            }
        }
        return incidents
    }

    private data class ConditionResult(val matched: Boolean, val confidence: Double)

    private fun evaluateCondition(condition: RuleCondition, context: RuleEvaluationContext): ConditionResult {
        return when (condition) {
            is RuleCondition.And -> {
                val results = condition.conditions.map { evaluateCondition(it, context) }
                if (results.all { it.matched }) {
                    ConditionResult(true, results.minOf { it.confidence })
                } else {
                    ConditionResult(false, 0.0)
                }
            }

            is RuleCondition.Or -> {
                val results = condition.conditions.map { evaluateCondition(it, context) }
                val anyMatch = results.any { it.matched }
                if (anyMatch) {
                    ConditionResult(true, results.maxOf { it.confidence })
                } else {
                    ConditionResult(false, 0.0)
                }
            }

            is RuleCondition.Not -> {
                val inner = evaluateCondition(condition.inner, context)
                ConditionResult(!inner.matched, 1.0 - inner.confidence)
            }

            is RuleCondition.SequenceDetected -> {
                val events = context.recentEvents.filter {
                    it.eventType in condition.eventTypes
                }.sortedByDescending { it.timestamp }
                val matched = events.size >= condition.eventTypes.size &&
                    events.zipWithNext { a, b -> a.timestamp - b.timestamp <= condition.withinWindowMs }
                        .all { it }
                ConditionResult(matched, if (matched) 0.85 else 0.0)
            }

            is RuleCondition.ThresholdExceeded -> {
                val count = context.counters[condition.metric] ?: 0
                val exceeded = count > condition.maxCount
                val ratio = count.toDouble() / condition.maxCount.toDouble()
                ConditionResult(exceeded, if (exceeded) (ratio.coerceAtMost(1.0)) else 0.0)
            }

            is RuleCondition.PatternMatch -> {
                val value = context.fields[condition.field]
                val matched = value?.toString()?.contains(condition.pattern.toRegex()) == true
                ConditionResult(matched, if (matched) 0.9 else 0.0)
            }
        }
    }
}

data class RuleEvaluationContext(
    val sourceId: String,
    val sequence: Long,
    val recentEvents: List<EventSummary>,
    val counters: Map<String, Int>,
    val fields: Map<String, Any?>,
    val provenanceChain: List<String> = emptyList()
)

data class EventSummary(
    val eventId: String,
    val eventType: String,
    val timestamp: Long,
    val source: String
)
