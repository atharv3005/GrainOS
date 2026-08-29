cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/viewmodel/GrainWmsViewModel.kt

    fun addStorageFacilities(facilities: List<Pair<String, Double>>) {
        viewModelScope.launch {
            if (facilities.isNotEmpty()) {
                val mainCrop = _activeCrop.value
                val existingGodowns = repository.getAllGodowns().firstOrNull() ?: emptyList()
                val offset = existingGodowns.size
                
                val godownEntities = facilities.mapIndexed { idx, fac ->
                    val idStr = "GODOWN_${System.currentTimeMillis()}_${idx}"
                    GodownEntity(
                        godownId = idStr,
                        displayName = fac.first.ifBlank { "Storage Facility ${offset + idx + 1}" },
                        capacityMt = fac.second.coerceAtLeast(50.0),
                        currentStockMt = 0.0,
                        activeCrop = mainCrop.name,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                repository.insertGodowns(godownEntities)
            }
        }
    }
INNER_EOF
