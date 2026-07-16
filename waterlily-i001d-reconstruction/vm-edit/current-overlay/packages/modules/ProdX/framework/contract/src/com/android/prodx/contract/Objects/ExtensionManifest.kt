package com.android.prodx.contract.objects

import com.android.prodx.contract.ContractVersion

data class ExtensionManifest(
    val extensionId: String,
    val name: String,
    val version: ContractVersion,
    val description: String? = null,
    val author: String? = null,
    val requiredCapabilities: Set<String> = emptySet(),
    val optionalCapabilities: Set<String> = emptySet(),
    val schemaHashes: Set<String> = emptySet(),
    val dependencies: Set<String> = emptySet(),
    val contractVersion: ContractVersion = ContractVersion.LATEST
) {
    fun toEnvelopeMap(): Map<String, Any?> = buildMap {
        put("extension_id", extensionId)
        put("name", name)
        put("version", version.toString())
        description?.let { put("description", it) }
        author?.let { put("author", it) }
        if (requiredCapabilities.isNotEmpty()) put("required_capabilities", requiredCapabilities.toList())
        if (optionalCapabilities.isNotEmpty()) put("optional_capabilities", optionalCapabilities.toList())
        if (schemaHashes.isNotEmpty()) put("schema_hashes", schemaHashes.toList())
        if (dependencies.isNotEmpty()) put("dependencies", dependencies.toList())
        put("contract_version", contractVersion.toString())
    }
}
