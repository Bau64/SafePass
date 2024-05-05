package pfc.safepass.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pfc.safepass.app.Pass_Item

class DataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "passwords.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "passwords"

        private const val COLUMN_ID = "id"
        private const val COLUMN_NICKNAME = "nickname"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_ICON = "icon"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_RECYCLED = "recycled"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NICKNAME TEXT NOT NULL, $COLUMN_USERNAME TEXT, $COLUMN_ICON BLOB, $COLUMN_PASSWORD TEXT NOT NULL, $COLUMN_NOTES TEXT, $COLUMN_DATE TEXT NOT NULL, $COLUMN_RECYCLED INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Agrega una contraseña a la base de contraseñas
     * @param Pass_Item
     */
    fun insertPassword(passItem: Pass_Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NICKNAME, passItem.nickname)
            put(COLUMN_USERNAME, passItem.user)
            put(COLUMN_ICON, passItem.icon)
            put(COLUMN_PASSWORD, passItem.password)
            put(COLUMN_NOTES, passItem.notes)
            put(COLUMN_DATE, passItem.date)
            put(COLUMN_RECYCLED, false)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    /**
     * Devuelve todas las entradas en la base de contraseñas
     * @return mutableListOf<Pass_Item>
     */
    fun getAllPassword(recycled: Boolean): List<Pass_Item> {
        val passList = mutableListOf<Pass_Item>()
        val db = readableDatabase
        val query: String = if (recycled)
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_RECYCLED = TRUE"
        else
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_RECYCLED = FALSE"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val icon = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ICON))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))

            val passwordItem = Pass_Item(id, nickname, username, password, notes, icon, date)
            passList.add(passwordItem)
        }

        cursor.close()
        db.close()
        return passList
    }

    /**
     * Obtiene una contraseña por su ID en la base de datos
     * @param id ID de la contraseña
     * @return PassItem
     */
    fun getPasswordbyID(id: Int): Pass_Item? {
        var passItem: Pass_Item? = null
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $id", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val icon = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ICON))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))

            passItem = Pass_Item(id, nickname, username, password, notes, icon, date)
        }

        cursor.close()
        db.close()
        return passItem
    }

    /**
     * Devuelve la cantidad de contraseñas existentes en la base de datos
     * @return Int
     */
    fun getPasswordCount(): Int{
        val count: Int
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_RECYCLED = false", null)

        cursor.moveToFirst()
        count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    /**
     * Actualiza una contraseña de la base de datos
     * @param passItem Contraseña a ctualizar
     */
    fun updatePassword(passItem: Pass_Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NICKNAME, passItem.nickname)
            put(COLUMN_USERNAME, passItem.user)
            put(COLUMN_ICON, passItem.icon)
            put(COLUMN_PASSWORD, passItem.password)
            put(COLUMN_NOTES, passItem.notes)
            put(COLUMN_DATE, passItem.date)
        }
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(passItem.id.toString())
        db.update(TABLE_NAME, values, where, whereArgs)
        db.close()
    }

    fun recyclePassword(passItem: Pass_Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECYCLED, true)
        }
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(passItem.id.toString())
        db.update(TABLE_NAME, values, where, whereArgs)
        db.close()
    }

    /**
     * Elimina una contraseña de la base de datos
     * @param id ID de la contraseña
     */
    fun deletePassword(id: Int) {
        val db = writableDatabase
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete(TABLE_NAME, where, whereArgs)
        db.close()
    }

    fun deleteAllPasswords() {
        val db = writableDatabase
        val where = "$COLUMN_RECYCLED = TRUE"
        db.delete(TABLE_NAME, where, null)
        db.close()
    }
}