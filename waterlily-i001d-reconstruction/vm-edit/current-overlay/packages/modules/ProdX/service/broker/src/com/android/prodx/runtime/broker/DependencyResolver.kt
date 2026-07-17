package com.android.prodx.runtime.broker

import android.util.Log

data class DependencyNode(
    val providerId: String,
    val version: String = "",
    val resolved: Boolean = false,
    val dependencies: Set<String> = emptySet()
)

data class DependencyGraph(
    val nodes: Map<String, DependencyNode>,
    val resolutionOrder: List<String>,
    val unresolved: List<String>
)

class DependencyResolver {

    fun resolve(
        manifest: Map<String, List<String>>,
        availableProviders: Set<String>
    ): DependencyGraph {
        val nodes = mutableMapOf<String, DependencyNode>()
        val visited = mutableSetOf<String>()
        val stack = ArrayDeque<String>()
        val resolutionOrder = mutableListOf<String>()
        val unresolved = mutableListOf<String>()

        for ((providerId, deps) in manifest) {
            nodes[providerId] = DependencyNode(
                providerId = providerId,
                dependencies = deps.toSet()
            )
        }

        for (providerId in nodes.keys) {
            if (providerId !in visited) {
                val result = topologicalSort(providerId, nodes, visited, mutableSetOf(), availableProviders)
                if (result != null) {
                    resolutionOrder.addAll(result)
                } else {
                    unresolved.add(providerId)
                }
            }
        }

        for (providerId in nodes.keys) {
            val deps = nodes[providerId]?.dependencies ?: emptySet()
            for (dep in deps) {
                if (dep !in availableProviders && dep !in nodes) {
                    if (dep !in unresolved) unresolved.add(dep)
                }
            }
        }

        return DependencyGraph(
            nodes = nodes,
            resolutionOrder = resolutionOrder,
            unresolved = unresolved.distinct()
        )
    }

    fun resolveFromCapabilityManifest(
        capabilityId: String,
        manifest: Map<String, Any?>
    ): List<String> {
        val deps = mutableListOf<String>()

        val rawDeps = manifest["dependencies"]
        if (rawDeps is List<*>) {
            for (dep in rawDeps) {
                if (dep is String) deps.add(dep)
            }
        }

        val providerRef = manifest["provider"]
        if (providerRef is String && providerRef.isNotBlank()) {
            deps.add(providerRef)
        }

        Log.d(TAG, "Resolved $capabilityId -> ${deps.size} dependencies")
        return deps
    }

    private fun topologicalSort(
        nodeId: String,
        nodes: Map<String, DependencyNode>,
        visited: MutableSet<String>,
        stack: MutableSet<String>,
        availableProviders: Set<String>
    ): List<String>? {
        if (nodeId in stack) return null
        if (nodeId in visited) return emptyList()

        stack.add(nodeId)
        visited.add(nodeId)

        val order = mutableListOf<String>()
        val node = nodes[nodeId]

        if (node != null) {
            for (dep in node.dependencies) {
                if (dep in nodes) {
                    val subOrder = topologicalSort(dep, nodes, visited, stack, availableProviders)
                    if (subOrder == null) return null
                    order.addAll(subOrder)
                } else if (dep !in availableProviders) {
                    Log.w(TAG, "Unresolvable dependency: $dep for provider $nodeId")
                }
            }
        }

        stack.remove(nodeId)
        order.add(nodeId)
        return order
    }

    companion object {
        private const val TAG = "DependencyResolver"
    }
}
