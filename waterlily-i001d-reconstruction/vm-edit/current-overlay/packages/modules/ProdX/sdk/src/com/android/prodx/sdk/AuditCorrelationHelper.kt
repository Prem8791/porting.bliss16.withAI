package com.android.prodx.sdk

import java.util.UUID

data class CorrelationChain(
    val rootId: String,
    val parentId: String?,
    val childIds: MutableList<String> = mutableListOf(),
    val depth: Int = 0,
) {
    fun deriveChild(): CorrelationChain = CorrelationChain(
        rootId = rootId,
        parentId = currentId,
        childIds = mutableListOf(),
        depth = depth + 1,
    )

    val currentId: String get() = childIds.lastOrNull() ?: rootId

    fun addChild(childId: String) {
        childIds.add(childId)
    }
}

data class AuditContext(
    val correlationId: String,
    val correlationChain: CorrelationChain? = null,
    val operationType: String = "",
    val resourceId: String = "",
    val callerIdentity: String = "",
    val additionalMetadata: Map<String, String> = emptyMap(),
)

class AuditCorrelationHelper {
    private var chain: CorrelationChain? = null

    fun generateCorrelationId(): String = UUID.randomUUID().toString()

    fun startCorrelationChain(): CorrelationChain {
        val rootId = generateCorrelationId()
        val newChain = CorrelationChain(rootId = rootId, parentId = null, depth = 0)
        chain = newChain
        return newChain
    }

    fun deriveFromParent(parentChain: CorrelationChain): CorrelationChain {
        val childId = generateCorrelationId()
        parentChain.addChild(childId)
        return parentChain.deriveChild()
    }

    fun getCurrentChain(): CorrelationChain? = chain

    fun buildAuditContext(
        operationType: String,
        resourceId: String,
        callerIdentity: String,
        additionalMetadata: Map<String, String> = emptyMap(),
    ): AuditContext {
        val corrId = chain?.currentId ?: generateCorrelationId()
        return AuditContext(
            correlationId = corrId,
            correlationChain = chain,
            operationType = operationType,
            resourceId = resourceId,
            callerIdentity = callerIdentity,
            additionalMetadata = additionalMetadata,
        )
    }

    fun createContextWithChain(
        operationType: String,
        resourceId: String,
        callerIdentity: String,
        chain: CorrelationChain,
    ): AuditContext = AuditContext(
        correlationId = chain.currentId,
        correlationChain = chain,
        operationType = operationType,
        resourceId = resourceId,
        callerIdentity = callerIdentity,
    )

    fun reset() {
        chain = null
    }
}
