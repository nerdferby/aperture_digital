package lib

class VeganDetermination {
    companion object {
        const val IS_VEGAN = 0
        const val NOT_VEGAN = 1
        const val UNKNOWN = 2

        private val nonVeganIngredients = arrayOf(
            "Beef",
            "Lamb",
            "Pork",
            "Horse",
            "Duck",
            "Chicken",
            "Turkey",
            "Goose",
            "Quail",
            "Fish",
            "Anchovy",
            "Anchovies",
            "Shrimp",
            "Squid",
            "Scallop",
            "Scallops",
            "Calamari",
            "Mussels",
            "Milk",
            "Milk fat",
            "Milk solids",
            "Milk powder",
            "Yoghurt",
            "Cheese",
            "Butter",
            "Cream",
            "Ice cream",
            "Egg",
            "Egg white",
            "Albumen",
            "Honey",
            "Bee pollen",
            "Royal jelly",
            "E120",
            "E322",
            "E422",
            "E471",
            "E542",
            "E631",
            "E901",
            "E904",
            "Cochineal",
            "Carmine",
            "Gelatin",
            "Isinglass",
            "Castoreum",
            "Shellac",
            "Whey",
            "Casein",
            "Lactose",
            "Animal fat",
            "Bone",
            "Bone char",
            "Beeswax",
            "Shellac"
        )

        private val potentiallyVeganIngredients = arrayOf(
            "Vitamin D3", //unless from lichen
            "Omega-3" //unless from algae
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

        private fun isMaybeNotVegan(ingredients: Array<String>): Boolean {
            for(ingredient in ingredients){
                if(ingredient in potentiallyVeganIngredients){
                    return true
                }
            }

            return false
        }
    }
}