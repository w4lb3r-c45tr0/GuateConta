package com.example.guateconta.domain


import kotlin.math.round

data class DesgloseFiscal(
    val montoTotal: Double,
    val baseImponible: Double,
    val impuestoRetenido: Double
)

object CalculadoraFiscal {
    fun calcularRegimenGeneral(montoTotal: Double): DesgloseFiscal {
        val base = montoTotal / 1.12
        val iva = montoTotal - base
        return DesgloseFiscal(redondear(montoTotal), redondear(base), redondear(iva))
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }
}