package lib

import java.util.*
import kotlin.collections.ArrayList

class VeganDetermination {
    companion object {
        const val IS_VEGAN = 0
        const val NOT_VEGAN = 1
        const val UNKNOWN = 2

        private val nonVeganIngredients = arrayOf(
            "beef",
            "lamb",
            "pork",
            "horse",
            "duck",
            "chicken",
            "turkey",
            "goose",
            "quail",
            "fish",
            "anchovy",
            "anchovies",
            "shrimp",
            "squid",
            "scallop",
            "scallops",
            "calamari",
            "mussels",
            "milk",
            "milk fat",
            "milk solids",
            "milk powder",
            "yoghurt",
            "cheese",
            "butter",
            "cream",
            "ice cream",
            "egg",
            "egg white",
            "albumen",
            "honey",
            "bee pollen",
            "royal jelly",
            "e120",
            "e322",
            "e422",
            "e471",
            "e542",
            "e631",
            "e901",
            "e904",
            "cochineal",
            "carmine",
            "gelatin",
            "isinglass",
            "castoreum",
            "shellac",
            "whey",
            "casein",
            "lactose",
            "animal fat",
            "bone",
            "bone char",
            "beeswax",
            "shellac"
        )

        private val potentiallyVeganIngredients = arrayOf(
            "vitamin D3", //unless from lichen
            "omega-3" //unless from algae
        )

        private val potentiallyVeganTypes = mapOf(
            Pair("vitamin D3", arrayOf(
                "cholecalciferol"
            )),
            Pair("omega-3", arrayOf(
                "algea",
                "nut",
                "seed",
                "flax",
                "chia",
                "spirulina",
                "chlorella"
            ))
        )

        /*
        Test if a product is vegan
        -
        Expecting ingredients as a string array
        --
        Description: This function tests to see if any ingredients are definitely not vegan, if so it will return VeganDetermination.NOT_VEGAN
            If no non-vegan ingredients are found then it tests to see if there are any potentially vegan ingredients such as D3 or Omega 3
            if the ingredients list contains the vegan friendly version then VeganDetermination.IS_VEGAN will be returned
            otherwise VeganDetermination.UNKNOWN will be returned
         */
        fun isProductVegan(ingredients: Array<String>): Int{
            val formattedIngredients = formatIngredients(ingredients)
            val definitelyNotVegan = isDefinitelyNotVegan(formattedIngredients)
            val maybeNotVegan = isMaybeNotVegan(formattedIngredients)

            if(definitelyNotVegan == this.NOT_VEGAN) return this.NOT_VEGAN
            if(maybeNotVegan == this.UNKNOWN) return this.UNKNOWN

            return this.IS_VEGAN

        }

        private fun formatIngredients(ingredients: Array<String>): Array<String> {
            val formattedIngredients = ArrayList<String>()

            for(ingredient in ingredients) {
                formattedIngredients.add(ingredient.toLowerCase(Locale.ROOT))
            }

            return formattedIngredients.toTypedArray()
        }

        private fun isDefinitelyNotVegan(ingredients: Array<String>): Int {
            for(ingredient in ingredients){
                if(ingredient in nonVeganIngredients){
                    return this.NOT_VEGAN
                }
            }

            return this.IS_VEGAN
        }

        /*
            Finds potentially vegan ingredients and checks them against the types that are vegan for that ingredient
         */
        private fun isMaybeNotVegan(ingredients: Array<String>): Int {
            val maybeIngredients = ArrayList<String>()
            for(ingredient in ingredients){
                if(ingredient in potentiallyVeganIngredients){
                    maybeIngredients.add(ingredient)
                }
            }

            for(ingredient in maybeIngredients){
                if(ingredient in potentiallyVeganTypes){
                    if(isVeganType(ingredient, ingredients) == this.UNKNOWN) return this.UNKNOWN
                }
            }

            return this.IS_VEGAN
        }

        private fun isVeganType(validType: String, ingredients: Array<String>): Int {
            if(validType in potentiallyVeganTypes){
                val validTypes = potentiallyVeganTypes[validType]

                if (validTypes != null) {
                    for(type in validTypes){
                        if(type !in ingredients) return this.UNKNOWN
                    }
                }
            }
            return this.IS_VEGAN
        }
    }
}