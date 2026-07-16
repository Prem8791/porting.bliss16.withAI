package com.android.prodx.contract

data class Extension(
    val identifier: String,
    val payload: Map<String, Any?>
)

class ContractEnvelope private constructor(
    val objectType: String,
    val contractVersion: ContractVersion,
    val objectId: String,
    val createdAt: Long,
    val issuer: String,
    val schemaRef: String,
    val registryGeneration: Int,
    val contentHash: ContentHash?,
    val payload: Map<String, Any?>,
    val extensions: Map<String, Extension>
) {
    companion object {
        private const val MAX_STRING_LENGTH = 200
        private const val MAX_PAYLOAD_SIZE_BYTES = 65536

        fun create(
            objectType: String,
            contractVersion: ContractVersion,
            objectId: String,
            createdAt: Long,
            issuer: String,
            schemaRef: String,
            registryGeneration: Int = 0,
            payload: Map<String, Any?> = emptyMap(),
            extensions: Map<String, Extension> = emptyMap()
        ): Result<ContractEnvelope> {
            val errors = mutableListOf<String>()

            if (objectType.isBlank() || objectType.length > MAX_STRING_LENGTH) {
                errors.add("objectType: invalid length")
            }
            if (objectId.isBlank() || objectId.length > MAX_STRING_LENGTH) {
                errors.add("objectId: invalid length")
            }
            if (issuer.isBlank() || issuer.length > MAX_STRING_LENGTH) {
                errors.add("issuer: invalid length")
            }
            if (schemaRef.isBlank() || schemaRef.length > MAX_STRING_LENGTH) {
                errors.add("schemaRef: invalid length")
            }
            if (createdAt <= 0) {
                errors.add("createdAt: must be positive")
            }
            if (registryGeneration < 0) {
                errors.add("registryGeneration: must be non-negative")
            }

            for ((key, _) in extensions) {
                if (key.isBlank() || key.length > MAX_STRING_LENGTH) {
                    errors.add("extension key '$key': invalid length")
                }
            }

            if (errors.isNotEmpty()) {
                return Result.failure(IllegalArgumentException(errors.joinToString("; ")))
            }

            val envelope = ContractEnvelope(
                objectType = objectType,
                contractVersion = contractVersion,
                objectId = objectId,
                createdAt = createdAt,
                issuer = issuer,
                schemaRef = schemaRef,
                registryGeneration = registryGeneration,
                contentHash = null,
                payload = payload,
                extensions = extensions
            )

            val contentHash = ContentHash.compute(envelope.computeContentBytes())
            return Result.success(envelope.copyWithHash(contentHash))
        }
    }

    private fun computeContentBytes(): ByteArray =
        CanonicalCborCodec.encode(toMap().filterKeys { it !in listOf("content_hash", "signature") })

    fun toMap(): Map<String, Any?> = buildMap {
        put("object_type", objectType)
        put("contract_version", contractVersion.toString())
        put("object_id", objectId)
        put("created_at", createdAt)
        put("issuer", issuer)
        put("schema_ref", schemaRef)
        put("registry_generation", registryGeneration)
        contentHash?.let { put("content_hash", it.value) }
        if (payload.isNotEmpty()) put("payload", payload)
        if (extensions.isNotEmpty()) {
            put("extensions", extensions.mapValues { (_, ext) -> ext.payload })
        }
    }

    private fun copyWithHash(hash: ContentHash): ContractEnvelope =
        ContractEnvelope(
            objectType, contractVersion, objectId, createdAt, issuer,
            schemaRef, registryGeneration, hash, payload, extensions
        )

    fun verifyContentHash(): Boolean =
        contentHash?.let { expected ->
            ContentHash.verify(computeContentBytes(), expected)
        } ?: false

    override fun toString(): String =
        "ContractEnvelope(type=$objectType, id=$objectId, version=$contractVersion)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContractEnvelope) return false
        return objectType == other.objectType &&
            contractVersion == other.contractVersion &&
            objectId == other.objectId &&
            createdAt == other.createdAt &&
            issuer == other.issuer &&
            schemaRef == other.schemaRef &&
            registryGeneration == other.registryGeneration &&
            contentHash == other.contentHash
    }

    override fun hashCode(): Int = 31 * objectId.hashCode() + contractVersion.hashCode()
}
