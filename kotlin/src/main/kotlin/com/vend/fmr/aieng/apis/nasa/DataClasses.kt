package com.vend.fmr.aieng.apis.nasa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApodResponse(
    val date: String,
    val explanation: String,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("service_version")
    val serviceVersion: String,
    val title: String,
    val url: String,
    @SerialName("hdurl")
    val hdUrl: String? = null,
    val copyright: String? = null
)

@Serializable
data class NearEarthObjectsResponse(
    @SerialName("near_earth_objects")
    val nearEarthObjects: Map<String, List<NearEarthObject>>,
    @SerialName("element_count")
    val elementCount: Int,
    val links: NeoLinks
)

@Serializable
data class NearEarthObject(
    val id: String,
    @SerialName("neo_reference_id")
    val neoReferenceId: String,
    val name: String,
    @SerialName("nasa_jpl_url")
    val nasaJplUrl: String,
    @SerialName("absolute_magnitude_h")
    val absoluteMagnitudeH: Double,
    @SerialName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameter,
    @SerialName("is_potentially_hazardous_asteroid")
    val isPotentiallyHazardousAsteroid: Boolean,
    @SerialName("close_approach_data")
    val closeApproachData: List<CloseApproachData>
)

@Serializable
data class EstimatedDiameter(
    val kilometers: DiameterRange,
    val meters: DiameterRange,
    val miles: DiameterRange,
    val feet: DiameterRange
)

@Serializable
data class DiameterRange(
    @SerialName("estimated_diameter_min")
    val estimatedDiameterMin: Double,
    @SerialName("estimated_diameter_max")
    val estimatedDiameterMax: Double
)

@Serializable
data class CloseApproachData(
    @SerialName("close_approach_date")
    val closeApproachDate: String,
    @SerialName("close_approach_date_full")
    val closeApproachDateFull: String,
    @SerialName("epoch_date_close_approach")
    val epochDateCloseApproach: Long,
    @SerialName("relative_velocity")
    val relativeVelocity: RelativeVelocity,
    @SerialName("miss_distance")
    val missDistance: MissDistance,
    @SerialName("orbiting_body")
    val orbitingBody: String
)

@Serializable
data class RelativeVelocity(
    @SerialName("kilometers_per_second")
    val kilometersPerSecond: String,
    @SerialName("kilometers_per_hour")
    val kilometersPerHour: String,
    @SerialName("miles_per_hour")
    val milesPerHour: String
)

@Serializable
data class MissDistance(
    val astronomical: String,
    val lunar: String,
    val kilometers: String,
    val miles: String
)

@Serializable
data class NeoLinks(
    val next: String? = null,
    val previous: String? = null,
    val self: String
)