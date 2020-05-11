package lib

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
         */
        fun isProductVegan(ingredients: Array<String>): Int{
            val definitelyNotVegan = isDefinitelyNotVegan(ingredients)

            if(definitelyNotVegan) return this.NOT_VEGAN

            val maybeNotVegan = isMaybeNotVegan(ingredients)

            if(maybeNotVegan) return this.UNKNOWN

            return this.IS_VEGAN

        }

        private fun isDefinitelyNotVegan(ingredients: Array<String>): Boolean{
            for(ingredient in ingredients){
                if(ingredient in nonVeganIngredients){
                    return true
                }
            }

            return false
        }

        /*
            Finds potentially vegan ingredients and checks them against the types that are vegan for that ingredient
         */
        private fun isMaybeNotVegan(ingredients: Array<String>): Boolean {
            val maybeIngredients = ArrayList<String>()
            for(ingredient in ingredients){
                if(ingredient in potentiallyVeganIngredients){
                    maybeIngredients.add(ingredient)
                }
            }

            for(ingredient in maybeIngredients){
                if(ingredient in potentiallyVeganTypes){
                    TODO("Complete logic, test the ingredient exists in the potentiallyVeganTypes")
                }
            }

            return false
        }
    }
}