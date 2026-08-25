package com.aima.koraki.data.model

import com.google.gson.annotations.SerializedName

/**
 * A virtual sprite companion loaded from `assets/companions/companions_list.json`.
 */
data class VirtualCompanion(
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("requiredLevel") val requiredLevel: Int = 0,
    @SerializedName("spriteAsset") val spriteAsset: String,
    val unlockStatus: Boolean = false,
    @SerializedName("birthYear") val birthYear: Int? = null,
    @SerializedName("favFoods") val favoriteFoods: List<String>? = null,
    val affectionLevel: Int = 0,
)

data class CompanionListResponse(
    @SerializedName("companions") val companions: List<VirtualCompanion>,
)
