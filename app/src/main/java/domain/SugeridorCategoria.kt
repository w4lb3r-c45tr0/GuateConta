package com.example.guateconta.domain


object SugeridorCategoria {
    fun obtenerCodigoContable(descripcion: String): String {
        val texto = descripcion.lowercase()
        return when {
            texto.contains("hamburguesa") || texto.contains("pollo") || texto.contains("pizza") || texto.contains("comida") -> "5.1.01"
            texto.contains("gasolina") || texto.contains("uber") || texto.contains("bus") || texto.contains("pasaje") -> "5.1.02"
            texto.contains("luz") || texto.contains("agua") || texto.contains("internet") || texto.contains("telefono") -> "5.1.03"
            texto.contains("cine") || texto.contains("juego") || texto.contains("fiesta") -> "5.1.04"
            else -> "5.1.01"
        }
    }
}