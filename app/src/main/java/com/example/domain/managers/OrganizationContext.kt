package com.example.domain.managers

import com.example.data.model.OrganizationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enterprise Multi-Tenant Organization Context.
 * Holds active company license profile and notifies ViewModels upon switching.
 */
object OrganizationContext {

    private val defaultOrg = OrganizationEntity(
        orgCode = "MAIN_HUB",
        legalName = "GrainOS Enterprise Agri Hub",
        tradeName = "GrainOS Trading Co",
        apmcLicenseNo = "APMC/MH/2026/088",
        gstin = "27AABCB1234F1Z5",
        pan = "AABCB1234F",
        address = "Dhule APMC Market Yard, Maharashtra"
    )

    private val _currentOrganization = MutableStateFlow(defaultOrg)
    val currentOrganization: StateFlow<OrganizationEntity> = _currentOrganization.asStateFlow()

    fun getCurrentOrgCode(): String = _currentOrganization.value.orgCode

    fun switchOrganization(newOrg: OrganizationEntity) {
        _currentOrganization.value = newOrg
    }

    fun switchOrganization(orgCode: String) {
        val current = _currentOrganization.value
        _currentOrganization.value = current.copy(orgCode = orgCode)
    }
}
