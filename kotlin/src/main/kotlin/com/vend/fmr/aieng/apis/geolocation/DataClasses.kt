package com.vend.fmr.aieng.apis.geolocation

import kotlinx.serialization.*

@Serializable
data class GeolocationError(
    val error: Boolean,
    val reason: String,
    val message: String
)

@Serializable
data class GeolocationResponse(
    val ip: String = "unknown",
    val network: String? = null,
    val version: String? = null,
    val city: String? = null,
    val region: String? = null,
    @SerialName("region_code") val regionCode: String? = null,
    val country: String? = null,
    @SerialName("country_name") val countryName: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("country_code_iso3") val countryCodeIso3: String? = null,
    @SerialName("country_capital") val countryCapital: String? = null,
    @SerialName("country_tld") val countryTld: String? = null,
    @SerialName("continent_code") val continentCode: String? = null,
    @SerialName("in_eu") val inEu: Boolean? = null,
    val postal: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    @SerialName("utc_offset") val utcOffset: String? = null,
    @SerialName("country_calling_code") val countryCallingCode: String? = null,
    val currency: String? = null,
    @SerialName("currency_name") val currencyName: String? = null,
    val languages: String? = null,
    @SerialName("country_area") val countryArea: Double? = null,
    @SerialName("country_population") val countryPopulation: Long? = null,
    val asn: String? = null,
    val org: String? = null
)