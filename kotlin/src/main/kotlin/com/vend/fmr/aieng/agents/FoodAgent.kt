package com.vend.fmr.aieng.agents

/**
 * Food Agent - Recommends restaurants and local cuisine
 */
class FoodAgent : TripPlanningAgent {
    override val agentName = "Food Agent"
    
    override suspend fun process(destination: String): AgentResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Simulate API call delay
            kotlinx.coroutines.delay(600)
            
            val restaurants = getRestaurantsForDestination(destination)
            
            val insights = generateFoodInsights(restaurants, destination)
            val recommendations = generateFoodRecommendations(restaurants)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = restaurants,
                insights = insights,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = false,
                error = e.message,
                insights = listOf("⚠️ Unable to load restaurant recommendations"),
                recommendations = listOf("Explore local food options independently")
            )
        }
    }
    
    private fun getRestaurantsForDestination(destination: String): List<Restaurant> {
        // Mock data - will be replaced with real API later
        return when (destination.lowercase()) {
            "stockholm", "sweden" -> listOf(
                Restaurant(
                    name = "Oaxen Krog",
                    cuisine = "Nordic",
                    description = "Michelin-starred restaurant focusing on local Swedish ingredients",
                    priceRange = "€€€€",
                    specialty = "Innovative Nordic tasting menu"
                ),
                Restaurant(
                    name = "Tradition",
                    cuisine = "Swedish",
                    description = "Classic Swedish dishes in a cozy, traditional setting",
                    priceRange = "€€€",
                    specialty = "Köttbullar (Swedish meatballs) and seafood"
                ),
                Restaurant(
                    name = "Hermitage",
                    cuisine = "Vegetarian",
                    description = "Charming vegetarian restaurant in Gamla Stan",
                    priceRange = "€€",
                    specialty = "Creative vegetarian dishes with Swedish influences"
                ),
                Restaurant(
                    name = "K25 Food Truck",
                    cuisine = "Street Food",
                    description = "Popular food truck serving gourmet burgers",
                    priceRange = "€",
                    specialty = "Swedish-style gourmet burgers"
                )
            )
            "oslo", "norway" -> listOf(
                Restaurant(
                    name = "Maaemo",
                    cuisine = "Nordic",
                    description = "Three Michelin-starred restaurant showcasing Norwegian nature",
                    priceRange = "€€€€",
                    specialty = "Organic Norwegian ingredients, seasonal menu"
                ),
                Restaurant(
                    name = "Fiskeriet Youngstorget",
                    cuisine = "Seafood",
                    description = "Fresh fish and seafood in a casual market setting",
                    priceRange = "€€",
                    specialty = "Fish soup and fresh catch of the day"
                ),
                Restaurant(
                    name = "Døgnvill Burger",
                    cuisine = "American/Norwegian",
                    description = "Oslo's beloved burger joint with local twist",
                    priceRange = "€€",
                    specialty = "Gourmet burgers with Norwegian ingredients"
                ),
                Restaurant(
                    name = "Mathallen Food Hall",
                    cuisine = "International",
                    description = "Food hall with diverse vendors and local specialties",
                    priceRange = "€-€€",
                    specialty = "Variety of Norwegian and international foods"
                )
            )
            "copenhagen", "denmark" -> listOf(
                Restaurant(
                    name = "Noma",
                    cuisine = "New Nordic",
                    description = "World-renowned restaurant that redefined Nordic cuisine",
                    priceRange = "€€€€",
                    specialty = "Foraged ingredients and innovative Nordic techniques"
                ),
                Restaurant(
                    name = "Smørrebrød",
                    cuisine = "Danish",
                    description = "Traditional Danish open-faced sandwiches",
                    priceRange = "€€",
                    specialty = "Classic and modern smørrebrød varieties"
                ),
                Restaurant(
                    name = "Torvehallerne",
                    cuisine = "Market Food",
                    description = "Vibrant food market with local and international vendors",
                    priceRange = "€-€€",
                    specialty = "Fresh produce, pastries, and street food"
                ),
                Restaurant(
                    name = "Restaurant Schønnemann",
                    cuisine = "Danish",
                    description = "Historic restaurant serving traditional Danish lunch",
                    priceRange = "€€€",
                    specialty = "Traditional Danish lunch dishes and aquavit"
                )
            )
            else -> listOf(
                Restaurant(
                    name = "Local Bistro",
                    cuisine = "Local",
                    description = "Popular local restaurant serving regional specialties",
                    priceRange = "€€",
                    specialty = "Traditional local dishes"
                ),
                Restaurant(
                    name = "Market Square Café",
                    cuisine = "Café",
                    description = "Cozy café in the city center",
                    priceRange = "€",
                    specialty = "Coffee, pastries, and light meals"
                ),
                Restaurant(
                    name = "Heritage Restaurant",
                    cuisine = "Traditional",
                    description = "Family-run restaurant with generations of recipes",
                    priceRange = "€€",
                    specialty = "Authentic regional cuisine"
                )
            )
        }
    }
    
    private fun generateFoodInsights(restaurants: List<Restaurant>, destination: String): List<String> {
        return buildList {
            val cuisineTypes = restaurants.map { it.cuisine }.distinct()
            val priceRanges = restaurants.map { it.priceRange }.distinct().sorted()
            
            add("🍽️ ${restaurants.size} restaurants found in $destination")
            add("🌎 Cuisine variety: ${cuisineTypes.joinToString(", ")}")
            add("💰 Price ranges: ${priceRanges.joinToString(" to ")}")
            
            val hasHighEnd = restaurants.any { it.priceRange.contains("€€€") }
            val hasBudget = restaurants.any { it.priceRange == "€" }
            
            if (hasHighEnd && hasBudget) {
                add("🔄 Good mix of budget and fine dining options")
            }
        }
    }
    
    private fun generateFoodRecommendations(restaurants: List<Restaurant>): List<String> {
        return buildList {
            val localCuisine = restaurants.filter { 
                it.cuisine.contains("Swedish", ignoreCase = true) || 
                it.cuisine.contains("Nordic", ignoreCase = true) ||
                it.cuisine.contains("Danish", ignoreCase = true) ||
                it.cuisine.contains("Norwegian", ignoreCase = true) ||
                it.cuisine.contains("Local", ignoreCase = true)
            }
            
            if (localCuisine.isNotEmpty()) {
                add("Try local specialties: ${localCuisine.take(2).joinToString(", ") { it.name }}")
            }
            
            val budgetOptions = restaurants.filter { it.priceRange == "€" || it.priceRange == "€-€€" }
            if (budgetOptions.isNotEmpty()) {
                add("Budget-friendly: ${budgetOptions.first().name}")
            }
            
            add("Make reservations for high-end restaurants in advance")
            add("Ask locals for hidden gem recommendations")
        }
    }
}