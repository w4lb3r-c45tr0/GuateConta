package com.example.guateconta.domain

import java.text.Normalizer

data class ResultadoVoz(
    val descripcionLimpia: String,
    val monto: String,
    val esGasto: Boolean,
    val categoriaSugerida: String
)

object ProcesadorVozContable {

    private fun normalizarTexto(texto: String): String {
        val textoSinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return textoSinTildes.lowercase()
    }

    fun procesarTextoDictado(textoOriginal: String): ResultadoVoz {
        val textoNorm = normalizarTexto(textoOriginal)

        // 1. EXTRAER EL MONTO
        val numeros = Regex("\\d+(\\.\\d+)?").findAll(textoOriginal).map { it.value }.toList()
        val monto = if (numeros.isNotEmpty()) numeros.last() else ""

        // 2. DETERMINAR TIPO (GASTO VS INGRESO)
        val palabrasGasto = listOf(
            "compre", "gastat", "gaste", "pague", "compra", "pago", "consumi", "gasto", "inverti"
        )
        val palabrasIngreso = listOf(
            "gane", "cobre", "recibi", "ingreso", "venta", "sueldo", "salario", "honorario", "cobro", "deposito", "remesa"
        )

        val esGasto = when {
            palabrasIngreso.any { textoNorm.contains(it) } -> false
            palabrasGasto.any { textoNorm.contains(it) } -> true
            else -> true
        }

        // 3. DICCIONARIO EXPANDIDO DE CATEGORÍAS
        val alimentos = listOf(
            "pizza", "comida", "almuerzo", "cena", "desayuno", "pan", "restaurante", "hamburguesa",
            "sandwich", "sandwinch", "refresco", "cafe", "bebida", "tacos", "tortas", "pastel",
            "snack", "chucheria", "supermercado", "despensa", "fruta", "carne", "pollo", "campero", "mcdonalds"
        )

        val serviciosPublicos = listOf(
            "luz", "agua", "servicio", "telefono", "celular", "internet", "claro", "tigo", "cable",
            "energia", "eegsa", "empagua", "recarga", "wifi"
        )

        val combustiblesTransportes = listOf(
            "gasolina", "diesel", "combustible", "uber", "pasaje", "taxi", "mantenimiento auto",
            "mecanico", "aceite", "llanta", "parqueo", "peaje", "transmetro"
        )

        val arrendamiento = listOf(
            "alquiler", "renta", "local", "apartamento", "oficina", "bodega"
        )

        val mercaderiaInventario = listOf(
            "mercaderia", "inventario", "insumos", "materia prima", "stock", "productos"
        )

        val equiposTecnologia = listOf(
            "computadora", "laptop", "telefono nuevo", "teclado", "mouse", "monitor", "impresora", "software"
        )

        val sueldosIngreso = listOf(
            "sueldo", "salario", "honorarios", "planilla", "quincena", "pago de planilla"
        )

        val ventasIngreso = listOf(
            "venta", "factura emitida", "cobro", "servicio prestado", "cliente", "comision"
        )

        val categoriaSugerida = when {
            alimentos.any { textoNorm.contains(it) } -> "5.1.01 Alimentos y Alimentación"
            serviciosPublicos.any { textoNorm.contains(it) } -> "5.1.02 Servicios Públicos (Luz/Agua/Tel)"
            combustiblesTransportes.any { textoNorm.contains(it) } -> "5.1.04 Combustibles y Transportes"
            arrendamiento.any { textoNorm.contains(it) } -> "5.1.03 Arrendamiento e Inmuebles"
            mercaderiaInventario.any { textoNorm.contains(it) } -> "5.1.05 Compra de Mercadería / Inventario"
            equiposTecnologia.any { textoNorm.contains(it) } -> "5.1.07 Equipos y Tecnología"
            sueldosIngreso.any { textoNorm.contains(it) } -> "4.1.02 Sueldos y Honorarios"
            ventasIngreso.any { textoNorm.contains(it) } -> "4.1.01 Ventas y Servicios Generales"
            else -> if (esGasto) "5.1.01 Alimentos y Alimentación" else "4.1.01 Ventas y Servicios Generales"
        }

        // 4. LIMPIEZA PROFUNDA DE PALABRAS PARÁSITAS Y CONECTORES
        var descripcionLimpia = textoOriginal
            .replace(Regex("(?i)\\b(compré|compre|gasté|gaste|pagué|pague|gané|gane|cobré|cobre|un|una|unos|unas|de|del|en|por|para|quetzales|quetzal|Q)\\b"), "")
            .replace(Regex("\\d+"), "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        if (descripcionLimpia.isBlank()) {
            descripcionLimpia = if (esGasto) "Gasto vario" else "Ingreso vario"
        }

        return ResultadoVoz(
            descripcionLimpia = descripcionLimpia,
            monto = monto,
            esGasto = esGasto,
            categoriaSugerida = categoriaSugerida
        )
    }
}