package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PsuRawDto(
    val name: String? = null,
    val price: Double? = null,
    val type: String? = null,
    val efficiency: String? = null,
    val wattage: Int? = null,
    val color: String? = null,
    @Serializable(with = LenientModularitySerializer::class)
    @SerialName("modular") val modularity: String? = null,
    val outputs: String? = null,
    @SerialName("tier") val tierRating: String? = null,
    val capacitors: String? = null,
    @SerialName("atx_standard") val atxStandard: String? = null,
    @SerialName("pcie_12vhpwr") val pcie12Vhpwr: String? = null,
    @SerialName("eps_connectors") val epsConnectors: String? = null,
    @SerialName("pcie_connectors") val pcieConnectors: String? = null,
    @SerialName("sata_connectors") val sataConnectors: String? = null,
    val topology: String? = null,
    val protections: String? = null,
    @SerialName("fan_size") val fanAndSize: String? = null,
    @SerialName("cybenetics") val cybeneticsCert: String? = null,
    @SerialName("cybenetics_lambda") val cybeneticsLambda: String? = null,
    val imageUrl: String? = null,
)
