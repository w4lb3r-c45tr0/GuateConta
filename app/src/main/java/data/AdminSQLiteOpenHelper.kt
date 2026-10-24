package com.example.guateconta.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminSQLiteOpenHelper(context: Context) :
    SQLiteOpenHelper(context, "GuateContaDB", null, 1) {

    data class Transaccion(
        val id: Int,
        val descripcion: String,
        val monto: Double,
        val baseImponible: Double,
        val impuesto: Double,
        val tipo: String,
        val categoria: String,
        val fechaIso: String,
        val fechaLegible: String
    )

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de usuario
        db.execSQL("CREATE TABLE usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT)")

        // Tabla de transacciones
        db.execSQL(
            """
            CREATE TABLE transacciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT,
                monto REAL,
                base_imponible REAL,
                impuesto REAL,
                tipo TEXT,
                categoria TEXT,
                fecha_iso TEXT,
                fecha_legible TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuario")
        db.execSQL("DROP TABLE IF EXISTS transacciones")
        onCreate(db)
    }

    // --- GESTIÓN DE USUARIO ---

    fun guardarNombreUsuario(nombre: String) {
        val db = writableDatabase
        // Primero borramos cualquier registro previo para evitar nombres duplicados acumulados
        db.delete("usuario", null, null)
        val values = ContentValues().apply {
            put("nombre", nombre)
        }
        db.insert("usuario", null, values)
        db.close()
    }

    fun obtenerNombreUsuario(): String? {
        val db = readableDatabase
        var nombre: String? = null
        val cursor = db.rawQuery("SELECT nombre FROM usuario LIMIT 1", null)
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return nombre
    }

    fun cerrarSesionUsuario() {
        val db = writableDatabase
        db.delete("usuario", null, null)
        db.delete("transacciones", null, null) // Vacía las transacciones para iniciar desde cero
        db.close()
    }

    fun eliminarCuentaYDatos() {
        val db = writableDatabase
        db.delete("usuario", null, null)
        db.delete("transacciones", null, null)
        db.close()
    }

    // --- GESTIÓN DE TRANSACCIONES ---

    fun guardarTransaccion(
        descripcion: String,
        monto: Double,
        baseImponible: Double,
        impuesto: Double,
        tipo: String,
        categoria: String
    ) {
        val db = writableDatabase
        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfLegible = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val ahora = Date()

        val values = ContentValues().apply {
            put("descripcion", descripcion)
            put("monto", monto)
            put("base_imponible", baseImponible)
            put("impuesto", impuesto)
            put("tipo", tipo)
            put("categoria", categoria)
            put("fecha_iso", sdfIso.format(ahora))
            put("fecha_legible", sdfLegible.format(ahora))
        }

        db.insert("transacciones", null, values)
        db.close()
    }

    fun obtenerTodasLasTransacciones(): List<Transaccion> {
        val lista = mutableListOf<Transaccion>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, descripcion, monto, base_imponible, impuesto, tipo, categoria, fecha_iso, fecha_legible FROM transacciones ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = Transaccion(
                    id = cursor.getInt(0),
                    descripcion = cursor.getString(1),
                    monto = cursor.getDouble(2),
                    baseImponible = cursor.getDouble(3),
                    impuesto = cursor.getDouble(4),
                    tipo = cursor.getString(5),
                    categoria = cursor.getString(6),
                    fechaIso = cursor.getString(7),
                    fechaLegible = cursor.getString(8)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerBalanceTotal(): Double {
        val db = readableDatabase
        var balance = 0.0
        val cursor = db.rawQuery("SELECT tipo, monto FROM transacciones", null)

        if (cursor.moveToFirst()) {
            do {
                val tipo = cursor.getString(0)
                val monto = cursor.getDouble(1)
                if (tipo == "INGRESO") balance += monto else balance -= monto
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return balance
    }

    fun eliminarTransaccion(id: Int) {
        val db = writableDatabase
        db.delete("transacciones", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun actualizarTransaccion(
        id: Int,
        descripcion: String,
        monto: Double,
        baseImponible: Double,
        impuesto: Double,
        tipo: String,
        categoria: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("descripcion", descripcion)
            put("monto", monto)
            put("base_imponible", baseImponible)
            put("impuesto", impuesto)
            put("tipo", tipo)
            put("categoria", categoria)
        }
        db.update("transacciones", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun vaciarTodasLasTransacciones() {
        val db = writableDatabase
        db.delete("transacciones", null, null)
        db.close()
    }
}