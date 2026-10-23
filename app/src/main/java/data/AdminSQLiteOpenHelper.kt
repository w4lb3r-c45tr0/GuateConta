package com.example.guateconta.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminSQLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    data class Transaccion(
        val id: Int,
        val descripcion: String,
        val monto: Double,
        val baseImponible: Double,
        val impuesto: Double,
        val tipo: String,
        val categoria: String,
        val fechaLegible: String,
        val fechaIso: String
    )

    companion object {
        private const val DATABASE_NAME = "GuateConta.db"
        private const val DATABASE_VERSION = 4

        private const val TABLA_PERFIL = "perfil_usuario"
        private const val TABLA_TRANSACCIONES = "transacciones"

        private const val KEY_ID = "id"
        private const val KEY_DESCRIPCION = "descripcion"
        private const val KEY_MONTO = "monto"
        private const val KEY_BASE = "base_imponible"
        private const val KEY_IMPUESTO = "impuesto"
        private const val KEY_TIPO = "tipo"
        private const val KEY_CATEGORIA = "categoria"
        private const val KEY_FECHA = "fecha"
        private const val KEY_FECHA_ISO = "fecha_iso"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createPerfilTable = "CREATE TABLE $TABLA_PERFIL (id INTEGER PRIMARY KEY, nombre TEXT)"
        val createTransaccionesTable = ("CREATE TABLE $TABLA_TRANSACCIONES ("
                + "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$KEY_DESCRIPCION TEXT, "
                + "$KEY_MONTO REAL, "
                + "$KEY_BASE REAL, "
                + "$KEY_IMPUESTO REAL, "
                + "$KEY_TIPO TEXT, "
                + "$KEY_CATEGORIA TEXT, "
                + "$KEY_FECHA TEXT, "
                + "$KEY_FECHA_ISO TEXT)")

        db?.execSQL(createPerfilTable)
        db?.execSQL(createTransaccionesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_PERFIL")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_TRANSACCIONES")
        onCreate(db)
    }

    fun guardarNombreUsuario(nombre: String) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("id", 1)
            put("nombre", nombre)
        }
        db.insertWithOnConflict(TABLA_PERFIL, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun obtenerNombreUsuario(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nombre FROM $TABLA_PERFIL WHERE id = 1", null)
        var nombre: String? = null
        if (cursor.moveToFirst()) nombre = cursor.getString(0)
        cursor.close()
        db.close()
        return nombre
    }

    fun guardarTransaccion(descripcion: String, monto: Double, base: Double, impuesto: Double, tipo: String, categoria: String): Long {
        val db = writableDatabase
        val hoy = Date()
        val fechaLegible = SimpleDateFormat("dd MMM", Locale.getDefault()).format(hoy)
        val fechaIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(hoy)

        val valores = ContentValues().apply {
            put(KEY_DESCRIPCION, descripcion)
            put(KEY_MONTO, monto)
            put(KEY_BASE, base)
            put(KEY_IMPUESTO, impuesto)
            put(KEY_TIPO, tipo)
            put(KEY_CATEGORIA, categoria)
            put(KEY_FECHA, fechaLegible)
            put(KEY_FECHA_ISO, fechaIso)
        }
        val idResult = db.insert(TABLA_TRANSACCIONES, null, valores)
        db.close()
        return idResult
    }

    fun obtenerTodasLasTransacciones(): List<Transaccion> {
        val lista = mutableListOf<Transaccion>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_ID, $KEY_DESCRIPCION, $KEY_MONTO, $KEY_BASE, $KEY_IMPUESTO, $KEY_TIPO, $KEY_CATEGORIA, $KEY_FECHA, $KEY_FECHA_ISO FROM $TABLA_TRANSACCIONES ORDER BY $KEY_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Transaccion(
                        id = cursor.getInt(0),
                        descripcion = cursor.getString(1),
                        monto = cursor.getDouble(2),
                        baseImponible = cursor.getDouble(3),
                        impuesto = cursor.getDouble(4),
                        tipo = cursor.getString(5),
                        categoria = cursor.getString(6),
                        fechaLegible = cursor.getString(7) ?: "Hoy",
                        fechaIso = cursor.getString(8) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerBalanceTotal(): Double {
        var total = 0.0
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_MONTO, $KEY_TIPO FROM $TABLA_TRANSACCIONES", null)
        if (cursor.moveToFirst()) {
            do {
                val monto = cursor.getDouble(0)
                val tipo = cursor.getString(1)
                if (tipo == "INGRESO") total += monto else total -= monto
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return total
    }

    fun eliminarTransaccion(id: Int): Int {
        val db = writableDatabase
        val filasAfectadas = db.delete(TABLA_TRANSACCIONES, "$KEY_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas
    }

    fun actualizarTransaccion(id: Int, descripcion: String, monto: Double, base: Double, impuesto: Double, tipo: String, categoria: String): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(KEY_DESCRIPCION, descripcion)
            put(KEY_MONTO, monto)
            put(KEY_BASE, base)
            put(KEY_IMPUESTO, impuesto)
            put(KEY_TIPO, tipo)
            put(KEY_CATEGORIA, categoria)
        }
        val filasAfectadas = db.update(TABLA_TRANSACCIONES, valores, "$KEY_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas
    }

    fun vaciarTodasLasTransacciones() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLA_TRANSACCIONES")
        db.execSQL("DELETE FROM sqlite_sequence WHERE name='$TABLA_TRANSACCIONES'")
        db.close()
    }
    // Borra el nombre registrado manteniendo los registros contables si se desea
    fun cerrarSesionUsuario() {
        val db = writableDatabase
        db.execSQL("DELETE FROM usuario")
        db.close()
    }

    // Borra absolutamente todo: usuario y transacciones
    fun eliminarCuentaYDatos() {
        val db = writableDatabase
        db.execSQL("DELETE FROM usuario")
        db.execSQL("DELETE FROM transacciones")
        db.close()
    }
}