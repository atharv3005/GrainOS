package com.example.domain.usecase

import com.example.data.model.DocumentType
import com.example.data.repository.DocumentSequenceRepository

/**
 * Domain Use Case for thread-safe sequential document numbering by Financial Year and Facility.
 */
class GenerateDocumentNumberUseCase(
    private val sequenceRepository: DocumentSequenceRepository
) {
    suspend operator fun invoke(
        financialYear: String = "26-27",
        facilityId: String = "MAIN",
        documentType: DocumentType
    ): String {
        return sequenceRepository.getNextNumber(financialYear, facilityId, documentType.name)
    }
}
