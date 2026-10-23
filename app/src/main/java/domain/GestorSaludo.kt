package com.example.guateconta.domain


import java.util.Calendar

object GestorSaludo {
    fun obtenerSaludo(nombreUsuario: String): String {
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val prefijo = when (horaActual) {
            in 5..11 -> "¡Buenos días"
            in 12..18 -> "¡Buenas tardes"
            else -> "¡Buenas noches"
        }
        return "$prefijo, $nombreUsuario!"
    }
}