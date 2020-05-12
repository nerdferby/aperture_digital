package localdatabase

class Product {
    var ProductID: String? = null
    var Gtin: String? = null
    var Description: String? = null
    var Ingredients: String? = null
    var Lifestyle: String? = null

    constructor(ProductID: String, Gtin: String, Description: String,  Ingredients: String, Lifestyle: String) {
        this.ProductID = ProductID
        this.Gtin = Gtin
        this.Description = Description
        this.Ingredients = Ingredients
        this.Lifestyle = Lifestyle
    }

}