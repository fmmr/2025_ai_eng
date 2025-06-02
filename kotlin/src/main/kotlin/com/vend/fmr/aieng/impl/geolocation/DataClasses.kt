package com.vend.fmr.aieng.impl.geolocation

import kotlinx.serialization.*

@Serializable
data class GeolocationResponse(
    val ip: String,
    val network: String? = null,
    val version: String,
    val city: String,
    val region: String,
    @SerialName("region_code") val regionCode: String,
    val country: String,
    @SerialName("country_name") val countryName: String,
    @SerialName("country_code") val countryCode: String,
    @SerialName("country_code_iso3") val countryCodeIso3: String,
    @SerialName("country_capital") val countryCapital: String,
    @SerialName("country_tld") val countryTld: String,
    @SerialName("continent_code") val continentCode: String,
    @SerialName("in_eu") val inEu: Boolean,
    val postal: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("utc_offset") val utcOffset: String,
    @SerialName("country_calling_code") val countryCallingCode: String,
    val currency: String,
    @SerialName("currency_name") val currencyName: String,
    val languages: String,
    @SerialName("country_area") val countryArea: Double,
    @SerialName("country_population") val countryPopulation: Long,
    val asn: String,
    val org: String
)