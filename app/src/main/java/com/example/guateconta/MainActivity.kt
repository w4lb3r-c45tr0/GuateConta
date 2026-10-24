package com.example.guateconta

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guateconta.data.AdminSQLiteOpenHelper
import com.example.guateconta.domain.CalculadoraFiscal
import com.example.guateconta.domain.GeneradorPdf
import com.example.guateconta.domain.GestorSaludo
import com.example.guateconta.domain.ProcesadorVozContable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sistemaEsOscuro = isSystemInDarkTheme()
            var esModoOscuro by remember { mutableStateOf(sistemaEsOscuro) }

            val esquemaColores = if (esModoOscuro) darkColorScheme() else lightColorScheme()
            val colorFondo = if (esModoOscuro) Color(0xFF121212) else Color(0xFFF8F9FA)

            MaterialTheme(colorScheme = esquemaColores) {
                Surface(modifier = Modifier.fillMaxSize(), color = colorFondo) {
                    AppGuateConta(
                        esModoOscuro = esModoOscuro,
                        onCambiarModoOscuro = { esModoOscuro = it }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGuateConta(esModoOscuro: Boolean, onCambiarModoOscuro: (Boolean) -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { AdminSQLiteOpenHelper(context) }
    var nombreUsuario by remember { mutableStateOf(dbHelper.obtenerNombreUsuario()) }

    if (nombreUsuario.isNullOrBlank()) {
        PantallaBienvenida(onNombreGuardado = { nuevoNombre ->
            dbHelper.guardarNombreUsuario(nuevoNombre)
            nombreUsuario = nuevoNombre
        })
    } else {
        PantallaConPestañas(
            nombreUsuario = nombreUsuario!!,
            dbHelper = dbHelper,
            esModoOscuro = esModoOscuro,
            onCambiarModoOscuro = onCambiarModoOscuro,
            onCerrarSesion = {
                nombreUsuario = null // Resetear el estado redirige a PantallaBienvenida al instante
            }
        )
    }
}

@Composable
fun PantallaBienvenida(onNombreGuardado: (String) -> Unit) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.don_quetzalito_portada),
            contentDescription = "Logo GuateConta",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("Asistente Contable y Fiscal de Guatemala", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(25.dp))
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Nombre del Contribuyente o Empresa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (textInput.isNotBlank()) onNombreGuardado(textInput.trim())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Ingresar", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConPestañas(
    nombreUsuario: String,
    dbHelper: AdminSQLiteOpenHelper,
    esModoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit // <-- Añadido
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    icon = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    icon = { Text("Auditoría", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 2,
                    onClick = { pestañaSeleccionada = 2 },
                    icon = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (pestañaSeleccionada) {
                0 -> PantallaInicio(nombreUsuario = nombreUsuario, dbHelper = dbHelper)
                1 -> PantallaHistorialAvanzado(nombreUsuario = nombreUsuario, dbHelper = dbHelper)
                2 -> PantallaAjustes(
                    nombreUsuario = nombreUsuario,
                    dbHelper = dbHelper,
                    esModoOscuro = esModoOscuro,
                    onCambiarModoOscuro = onCambiarModoOscuro,
                    onCerrarSesion = onCerrarSesion // <-- Pasar aquí
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(nombreUsuario: String, dbHelper: AdminSQLiteOpenHelper) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var balanceActual by remember { mutableStateOf(dbHelper.obtenerBalanceTotal()) }
    var listaHistorial by remember { mutableStateOf(dbHelper.obtenerTodasLasTransacciones()) }

    var conceptoText by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var esGasto by remember { mutableStateOf(true) }

    val cuentasGastos = listOf(
        "5.1.01 Alimentos y Alimentación",
        "5.1.02 Servicios Públicos (Luz/Agua/Tel)",
        "5.1.03 Arrendamiento e Inmuebles",
        "5.1.04 Combustibles y Transportes",
        "5.1.05 Compra de Mercadería / Inventario",
        "5.1.06 Mantenimiento y Reparaciones",
        "5.1.07 Equipos y Tecnología"
    )

    val cuentasIngresos = listOf(
        "4.1.01 Ventas y Servicios Generales",
        "4.1.02 Sueldos y Honorarios",
        "4.1.03 Comisiones Mercantiles",
        "4.1.04 Rendimientos Financieros",
        "4.1.05 Otros Ingresos Extraordinarios"
    )

    var categoriaSeleccionada by remember { mutableStateOf(cuentasGastos[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(esGasto) {
        if (esGasto && !cuentasGastos.contains(categoriaSeleccionada)) {
            categoriaSeleccionada = cuentasGastos[0]
        } else if (!esGasto && !cuentasIngresos.contains(categoriaSeleccionada)) {
            categoriaSeleccionada = cuentasIngresos[0]
        }
    }

    val saludo = remember { GestorSaludo.obtenerSaludo(nombreUsuario) }

    val totalIngresos = listaHistorial.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
    val totalGastos = listaHistorial.filter { it.tipo == "GASTO" }.sumOf { it.monto }

    val imagenMascota = if (balanceActual < 0) R.drawable.don_quetzalito_alerta else R.drawable.don_quetzalito

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val textoDictado = matches[0]
                val analisis = ProcesadorVozContable.procesarTextoDictado(textoDictado)

                conceptoText = analisis.descripcionLimpia
                montoText = analisis.monto
                esGasto = analisis.esGasto
                categoriaSeleccionada = analisis.categoriaSugerida

                Toast.makeText(context, "Procesado: ${analisis.descripcionLimpia} (Q.${analisis.monto})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun lanzarReconocimientoVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-GT")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ejemplo: Compré un sándwich de 35 quetzales")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Servicio de voz no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(45.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = saludo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Resumen de Ejercicio Fiscal", fontSize = 12.sp, color = Color.Gray)
                }
                Image(
                    painter = painterResource(id = imagenMascota),
                    contentDescription = "Don Quetzalito",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("SALDO", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Q. ${String.format("%.2f", balanceActual)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("INGRESOS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("+Q.${String.format("%.2f", totalIngresos)}", color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GASTOS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("-Q.${String.format("%.2f", totalGastos)}", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Evolución del saldo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        val width = size.width
                        val height = size.height

                        val transaccionesCronologicas = listaHistorial.reversed()
                        if (transaccionesCronologicas.isEmpty()) {
                            drawLine(Color(0xFF2563EB), start = androidx.compose.ui.geometry.Offset(0f, height / 2), end = androidx.compose.ui.geometry.Offset(width, height / 2), strokeWidth = 3.dp.toPx())
                        } else {
                            val saldosPuntos = mutableListOf<Double>()
                            var acumulado = 0.0
                            saldosPuntos.add(0.0)
                            transaccionesCronologicas.forEach {
                                if (it.tipo == "INGRESO") acumulado += it.monto else acumulado -= it.monto
                                saldosPuntos.add(acumulado)
                            }

                            val maxSaldo = (saldosPuntos.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                            val minSaldo = (saldosPuntos.minOrNull() ?: 0.0).coerceAtMost(0.0)
                            val rango = if (maxSaldo == minSaldo) 1.0 else maxSaldo - minSaldo

                            val path = Path()
                            val pasoX = width / (saldosPuntos.size - 1).coerceAtLeast(1)

                            saldosPuntos.forEachIndexed { i, saldo ->
                                val x = i * pasoX
                                val yNormalized = ((saldo - minSaldo) / rango).toFloat()
                                val y = height - (yNormalized * (height * 0.8f) + height * 0.1f)

                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(path = path, color = Color(0xFF2563EB), style = Stroke(width = 3.dp.toPx()))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = conceptoText,
                            onValueChange = { conceptoText = it },
                            label = { Text("Descripción del Movimiento") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { lanzarReconocimientoVoz() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Voz", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = montoText,
                        onValueChange = { montoText = it },
                        label = { Text("Monto Total (Q)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(selected = esGasto, onClick = { esGasto = true }, label = { Text("GASTO") })
                        FilterChip(selected = !esGasto, onClick = { esGasto = false }, label = { Text("INGRESO") })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cuenta Contable") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            val listaActual = if (esGasto) cuentasGastos else cuentasIngresos
                            listaActual.forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion, fontSize = 12.sp) }, onClick = {
                                    categoriaSeleccionada = opcion
                                    dropdownExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val monto = montoText.toDoubleOrNull()
                            if (conceptoText.isNotBlank() && monto != null && monto > 0) {
                                val desglose = CalculadoraFiscal.calcularRegimenGeneral(monto)
                                dbHelper.guardarTransaccion(
                                    conceptoText,
                                    desglose.montoTotal,
                                    desglose.baseImponible,
                                    desglose.impuestoRetenido,
                                    if (esGasto) "GASTO" else "INGRESO",
                                    categoriaSeleccionada
                                )

                                balanceActual = dbHelper.obtenerBalanceTotal()
                                listaHistorial = dbHelper.obtenerTodasLasTransacciones()
                                conceptoText = ""
                                montoText = ""
                                keyboardController?.hide()
                                Toast.makeText(context, "Asiento Registrado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Registrar Transacción", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialAvanzado(nombreUsuario: String, dbHelper: AdminSQLiteOpenHelper) {
    val context = LocalContext.current

    var balanceActual by remember { mutableStateOf(dbHelper.obtenerBalanceTotal()) }
    var listaHistorial by remember { mutableStateOf(dbHelper.obtenerTodasLasTransacciones()) }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroTiempo by remember { mutableStateOf("TODO") }

    var transaccionAEliminar by remember { mutableStateOf<AdminSQLiteOpenHelper.Transaccion?>(null) }
    var transaccionAEditar by remember { mutableStateOf<AdminSQLiteOpenHelper.Transaccion?>(null) }
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    val historialFiltrado = remember(listaHistorial, textoBusqueda, filtroTiempo) {
        val hoySdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoyIso = hoySdf.format(Date())

        listaHistorial.filter { item ->
            val coincideTexto = item.descripcion.contains(textoBusqueda, ignoreCase = true) ||
                    item.categoria.contains(textoBusqueda, ignoreCase = true) ||
                    item.monto.toString().contains(textoBusqueda)

            val coincideTiempo = when (filtroTiempo) {
                "HOY" -> item.fechaIso == fechaHoyIso
                "7DIAS" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -7)
                    val fechaLimite = hoySdf.format(cal.time)
                    item.fechaIso >= fechaLimite
                }
                "30DIAS" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    val fechaLimite = hoySdf.format(cal.time)
                    item.fechaIso >= fechaLimite
                }
                else -> true
            }

            coincideTexto && coincideTiempo
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(45.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Historial & Auditoría", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row {
                    Button(
                        onClick = { GeneradorPdf.generarYAbrirReporte(context, nombreUsuario, historialFiltrado, balanceActual) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("PDF", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { mostrarConfirmacionCierre = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por Agua, Luz, 5.1.02...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FilterChip(selected = filtroTiempo == "TODO", onClick = { filtroTiempo = "TODO" }, label = { Text("Todo", fontSize = 11.sp) })
                FilterChip(selected = filtroTiempo == "HOY", onClick = { filtroTiempo = "HOY" }, label = { Text("Hoy", fontSize = 11.sp) })
                FilterChip(selected = filtroTiempo == "7DIAS", onClick = { filtroTiempo = "7DIAS" }, label = { Text("7 Días", fontSize = 11.sp) })
                FilterChip(selected = filtroTiempo == "30DIAS", onClick = { filtroTiempo = "30DIAS" }, label = { Text("30 Días", fontSize = 11.sp) })
            }

            Spacer(modifier = Modifier.height(15.dp))
        }

        if (historialFiltrado.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text("No se encontraron registros para esta búsqueda.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            items(historialFiltrado) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { transaccionAEditar = item },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = item.descripcion, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${item.fechaLegible} • ${item.categoria}", fontSize = 10.sp, color = Color.Gray)
                            Text(text = "Base: Q.${String.format("%.2f", item.baseImponible)} | IVA: Q.${String.format("%.2f", item.impuesto)}", fontSize = 10.sp, color = Color(0xFF2563EB))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (item.tipo == "GASTO") "-" else "+"}Q.${String.format("%.2f", item.monto)}",
                                color = if (item.tipo == "GASTO") Color(0xFFDC2626) else Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.clickable { transaccionAEditar = item }.padding(4.dp)
                            )
                            Text(
                                text = "Del",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.clickable { transaccionAEliminar = item }.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // DIÁLOGOS
    transaccionAEditar?.let { item ->
        var editDesc by remember { mutableStateOf(item.descripcion) }
        var editMonto by remember { mutableStateOf(item.monto.toString()) }
        var editCategoria by remember { mutableStateOf(item.categoria) }
        var editDropdownExpanded by remember { mutableStateOf(false) }

        val cuentasGastos = listOf("5.1.01 Alimentos y Alimentación", "5.1.02 Servicios Públicos (Luz/Agua/Tel)", "5.1.03 Arrendamiento e Inmuebles", "5.1.04 Combustibles y Transportes", "5.1.05 Compra de Mercadería / Inventario", "5.1.06 Mantenimiento y Reparaciones", "5.1.07 Equipos y Tecnología")
        val cuentasIngresos = listOf("4.1.01 Ventas y Servicios Generales", "4.1.02 Sueldos y Honorarios", "4.1.03 Comisiones Mercantiles", "4.1.04 Rendimientos Financieros", "4.1.05 Otros Ingresos Extraordinarios")
        val opcionesDisponibles = if (item.tipo == "GASTO") cuentasGastos else cuentasIngresos

        AlertDialog(
            onDismissRequest = { transaccionAEditar = null },
            title = { Text("Editar Registro") },
            text = {
                Column {
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editMonto, onValueChange = { editMonto = it }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = editDropdownExpanded, onExpandedChange = { editDropdownExpanded = !editDropdownExpanded }) {
                        OutlinedTextField(value = editCategoria, onValueChange = {}, readOnly = true, label = { Text("Cuenta Contable") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editDropdownExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = editDropdownExpanded, onDismissRequest = { editDropdownExpanded = false }) {
                            opcionesDisponibles.forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion, fontSize = 12.sp) }, onClick = {
                                    editCategoria = opcion
                                    editDropdownExpanded = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val montoDouble = editMonto.toDoubleOrNull()
                    if (editDesc.isNotBlank() && montoDouble != null && montoDouble > 0) {
                        val desglose = CalculadoraFiscal.calcularRegimenGeneral(montoDouble)
                        dbHelper.actualizarTransaccion(item.id, editDesc, desglose.montoTotal, desglose.baseImponible, desglose.impuestoRetenido, item.tipo, editCategoria)
                        balanceActual = dbHelper.obtenerBalanceTotal()
                        listaHistorial = dbHelper.obtenerTodasLasTransacciones()
                        transaccionAEditar = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { transaccionAEditar = null }) { Text("Cancelar") } }
        )
    }

    transaccionAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { transaccionAEliminar = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Deseas eliminar '${item.descripcion}' por Q.${item.monto}?") },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.eliminarTransaccion(item.id)
                    balanceActual = dbHelper.obtenerBalanceTotal()
                    listaHistorial = dbHelper.obtenerTodasLasTransacciones()
                    transaccionAEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { transaccionAEliminar = null }) { Text("Cancelar") } }
        )
    }

    if (mostrarConfirmacionCierre) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCierre = false },
            title = { Text("Reiniciar Ejercicio") },
            text = { Text("¿Deseas borrar todos los registros contables del ejercicio?") },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.vaciarTodasLasTransacciones()
                    balanceActual = dbHelper.obtenerBalanceTotal()
                    listaHistorial = dbHelper.obtenerTodasLasTransacciones()
                    mostrarConfirmacionCierre = false
                }) { Text("Reiniciar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacionCierre = false }) { Text("Cancelar") } }
        )
    }
}

// --- PESTAÑA DE AJUSTES Y CONFIGURACIÓN ---
@Composable
fun PantallaAjustes(
    nombreUsuario: String,
    dbHelper: AdminSQLiteOpenHelper,
    esModoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit // <-- Añadido
) {
    val context = LocalContext.current
    var mostrarConfirmarCerrarSesion by remember { mutableStateOf(false) }
    var mostrarConfirmarEliminarCuenta by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.don_quetzalito),
            contentDescription = "Perfil Don Quetzalito",
            modifier = Modifier.size(85.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = nombreUsuario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "Contribuyente Registrado", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(25.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Modo Oscuro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Cambiar tema de la interfaz", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = esModoOscuro,
                    onCheckedChange = { onCambiarModoOscuro(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Información de GuateConta", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Versión: 1.0.0 (Producción Android)", fontSize = 11.sp)
                Text("Régimen Fiscal: General (12% IVA Guatemala)", fontSize = 11.sp)
                Text("Almacenamiento: Base de datos SQLite Local", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = { mostrarConfirmarCerrarSesion = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { mostrarConfirmarEliminarCuenta = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Eliminar Cuenta y Borrar Datos", fontWeight = FontWeight.Bold)
        }

        // DIÁLOGO CERRAR SESIÓN
        if (mostrarConfirmarCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarCerrarSesion = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Deseas salir de la cuenta de '$nombreUsuario'?") },
                confirmButton = {
                    TextButton(onClick = {
                        dbHelper.cerrarSesionUsuario()
                        mostrarConfirmarCerrarSesion = false
                        onCerrarSesion()
                    }) {
                        Text("Aceptar", color = Color(0xFF2563EB))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarCerrarSesion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

// DIÁLOGO ELIMINAR CUENTA
        if (mostrarConfirmarEliminarCuenta) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarEliminarCuenta = false },
                title = { Text("¿Eliminar Cuenta Permanentemente?") },
                text = { Text("Esta acción borrará la cuenta de '$nombreUsuario' y TODOS los asientos contables registrados.") },
                confirmButton = {
                    TextButton(onClick = {
                        dbHelper.eliminarCuentaYDatos()
                        Toast.makeText(context, "Cuenta y datos eliminados", Toast.LENGTH_SHORT).show()
                        mostrarConfirmarEliminarCuenta = false
                        onCerrarSesion()
                    }) {
                        Text("Eliminar Todo", color = Color(0xFFDC2626))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarEliminarCuenta = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}