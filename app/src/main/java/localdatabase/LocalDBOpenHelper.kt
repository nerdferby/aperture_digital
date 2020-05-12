package localdatabase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalDBOpenHelper(context: Context,
                        factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME,
        factory, DATABASE_VERSION) {

//    override fun onOpen(db: SQLiteDatabase) {
//        super.onOpen(db)
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
//        onCreate(db)
//    }


    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE = ("CREATE TABLE " +
                TABLE_NAME + "(" +
                COLUMN_ProductID + " INTEGER PRIMARY KEY," +
                COLUMN_Gtin + " TEXT," +
                COLUMN_Description + " TEXT," +
                COLUMN_Ingredients + " TEXT," +
                COLUMN_Lifestyle + " TEXT" + ")")
        db.execSQL(CREATE_PRODUCTS_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }
    fun addProduct(name: Product) {
        val values = ContentValues()
        values.put(COLUMN_ProductID, name.ProductID)
        values.put(COLUMN_Gtin, name.Gtin)
        values.put(COLUMN_Description, name.Description)
        values.put(COLUMN_Ingredients, name.Ingredients)
        values.put(COLUMN_Lifestyle, name.Lifestyle)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun getAllProducts(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "projectVDb"
        const val TABLE_NAME = "Product"
        const val COLUMN_ProductID = "ProductID"
        const val COLUMN_Gtin = "Gtin"
        const val COLUMN_Description = "Description"
        const val COLUMN_Ingredients = "Ingredients"
        const val COLUMN_Lifestyle = "Lifestyle"
    }
}