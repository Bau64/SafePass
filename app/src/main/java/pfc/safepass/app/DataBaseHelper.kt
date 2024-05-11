package pfc.safepass.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pfc.safepass.app.recycler.PassItem

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
     * Adds a single password to the database
     * @param passItem Password Object
     */
    fun insertPassword(passItem: PassItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NICKNAME, passItem.nickname)
            put(COLUMN_USERNAME, passItem.user)
            put(COLUMN_ICON, passItem.icon)
            put(COLUMN_PASSWORD, passItem.password)
            put(COLUMN_NOTES, passItem.notes)
            put(COLUMN_DATE, passItem.date)
            put(COLUMN_RECYCLED, 1)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    /**
     * Returns all passwords from the database
     * @return List of passwords in the database
     */
    fun getAllPassword(recycled: Boolean): List<PassItem> {
        val passList = mutableListOf<PassItem>()
        val db = readableDatabase
        val query: String = if (recycled)
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_RECYCLED = 0"
        else
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_RECYCLED = 1"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val icon = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ICON))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))

            val passwordItem = PassItem(id, nickname, username, password, notes, icon, date)
            passList.add(passwordItem)
        }

        cursor.close()
        db.close()
        return passList
    }

    /**
     * Returns a password by its ID from the database
     * @param id Password ID
     * @return PassItem Password object
     */
    fun getPasswordbyID(id: Int): PassItem? {
        var passItem: PassItem? = null
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

            passItem = PassItem(id, nickname, username, password, notes, icon, date)
        }

        cursor.close()
        db.close()
        return passItem
    }

    /**
     * Updates a single password from the database
     * @param passItem Password object to update
     */
    fun updatePassword(passItem: PassItem) {
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

    /**
     * Moves a single password to the recycle bin
     * @param id Password ID
     */
    fun recyclePassword(id: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECYCLED, 0)
        }
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.update(TABLE_NAME, values, where, whereArgs)
        db.close()
    }

    /**
     * Restores a single password from the recycle bin
     * @param id Password ID
     */
    fun restorePassword(id: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECYCLED, 1)
        }
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.update(TABLE_NAME, values, where, whereArgs)
        db.close()
    }

    /**
     * Deletes a single password from the database
     * @param id Password ID
     */
    fun deletePassword(id: Int) {
        val db = writableDatabase
        val where = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete(TABLE_NAME, where, whereArgs)
        db.close()
    }

    /**
     * Empty recycle bin
     */
    fun deleteAllPasswords() {
        val db = writableDatabase
        val where = "$COLUMN_RECYCLED = 0"
        db.delete(TABLE_NAME, where, null)
        db.close()
    }
}