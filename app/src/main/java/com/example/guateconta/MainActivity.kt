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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

// --- PALETA DE COLORES PROFESIONAL ---
val AzulMarino = Color(0xFF0F172A)
val AzulPrimario = Color(0xFF1E40AF)
val AzulSuave = Color(0xFF3B82F6)
val VerdeExito = Color(0xFF10B981)
val RojoPeligro = Color(0xFFEF4444)
val FondoClaro = Color(0xFFF1F5F9)
val FondoOscuro = Color(0xFF090D16)
val TarjetaOscura = Color(0xFF1E293B)
val GrisTexto = Color(0xFF64748B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sistemaEsOscuro = isSystemInDarkTheme()
            var esModoOscuro by remember { mutableStateOf(sistemaEsOscuro) }

            val esquemaColores = if (esModoOscuro) {
                darkColorScheme(primary = AzulSuave, background = FondoOscuro, surface = TarjetaOscura)
            } else {
                lightColorScheme(primary = AzulPrimario, background = FondoClaro, surface = Color.White)
            }

            MaterialTheme(colorScheme = esquemaColores) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (esModoOscuro) FondoOscuro else FondoClaro
                ) {
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
            onCerrarSesion = { nombreUsuario = null }
        )
    }
}

@Composable
fun PantallaBienvenida(onNombreGuardado: (String) -> Unit) {
    var textInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AzulMarino, Color(0xFF1E293B))
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.don_quetzalito_portada),
                    contentDescription = "Logo GuateConta",
                    modifier = Modifier
                        .size(230.dp)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("GuateConta", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Gestión Fiscal y Contable de Guatemala", fontSize = 13.sp, color = Color(0xFF94A3B8))

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Nombre del Contribuyente") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulSuave,
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedLabelColor = AzulSuave,
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { if (textInput.isNotBlank()) onNombreGuardado(textInput.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulSuave)
            ) {
                Text("Comenzar Ejercicio Fiscal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
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
    onCerrarSesion: () -> Unit
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                containerColor = if (esModoOscuro) TarjetaOscura else Color.White
            ) {
                NavigationBarItem(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    icon = { Text("📊", fontSize = 18.sp) },
                    label = { Text("Inicio", fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    icon = { Text("📋", fontSize = 18.sp) },
                    label = { Text("Historial", fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = pestañaSeleccionada == 2,
                    onClick = { pestañaSeleccionada = 2 },
                    icon = { Text("⚙️", fontSize = 18.sp) },
                    label = { Text("Ajustes", fontWeight = FontWeight.SemiBold) }
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
                    onCerrarSesion = onCerrarSesion
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
    var mostrarModalCalendario by remember { mutableStateOf(false) }

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
        categoriaSeleccionada = if (esGasto) cuentasGastos[0] else cuentasIngresos[0]
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
                val analisis = ProcesadorVozContable.procesarTextoDictado(matches[0])
                conceptoText = analisis.descripcionLimpia
                montoText = analisis.monto
                esGasto = analisis.esGasto
                categoriaSeleccionada = analisis.categoriaSugerida
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = saludo, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Régimen General del IVA (12%)", fontSize = 12.sp, color = GrisTexto)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = imagenMascota),
                    contentDescription = "Mascota",
                    modifier = Modifier
                        .size(95.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Principal de Balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AzulMarino, AzulPrimario)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BALANCE NETO FISCAL", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                            // BOTÓN CALENDARIO
                            IconButton(
                                onClick = { mostrarModalCalendario = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text("📅", fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Q. ${String.format("%.2f", balanceActual)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Ingresos", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text("+Q.${String.format("%.2f", totalIngresos)}", color = VerdeExito, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text("Gastos", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text("-Q.${String.format("%.2f", totalGastos)}", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfica de Tendencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Evolución del Ejercicio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(modifier = Modifier.fillMaxWidth().height(70.dp)) {
                        val width = size.width
                        val height = size.height
                        val transaccionesCronologicas = listaHistorial.reversed()

                        if (transaccionesCronologicas.isNotEmpty()) {
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
                            drawPath(path = path, color = AzulSuave, style = Stroke(width = 3.dp.toPx()))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Formulario de Asiento Contable
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Nuevo Asiento Contable", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = conceptoText,
                            onValueChange = { conceptoText = it },
                            label = { Text("Concepto / Factura") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-GT")
                                }
                                try { speechLauncher.launch(intent) } catch (e: Exception) {}
                            },
                            modifier = Modifier.background(AzulPrimario, CircleShape)
                        ) {
                            Text("🎙️", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = montoText,
                        onValueChange = { montoText = it },
                        label = { Text("Monto Total con IVA (Q)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { esGasto = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (esGasto) RojoPeligro else Color.LightGray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("GASTO", fontWeight = FontWeight.Bold, color = if (esGasto) Color.White else Color.Black)
                        }
                        Button(
                            onClick = { esGasto = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!esGasto) VerdeExito else Color.LightGray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("INGRESO", fontWeight = FontWeight.Bold, color = if (!esGasto) Color.White else Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cuenta Nomenclatura SAT") },
                            shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

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
                                Toast.makeText(context, "Asiento registrado correctamente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                    ) {
                        Text("Registrar Transacción", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- MODAL DE CALENDARIO FINANCIERO ---
    if (mostrarModalCalendario) {
        DialogoCalendarioFinanciero(
            listaTransacciones = listaHistorial,
            onDismiss = { mostrarModalCalendario = false }
        )
    }
}

// --- COMPONENTE CALENDARIO DE MOVIMIENTOS ---
@Composable
fun DialogoCalendarioFinanciero(
    listaTransacciones: List<AdminSQLiteOpenHelper.Transaccion>,
    onDismiss: () -> Unit
) {
    var fechaSeleccionadaIso by remember { mutableStateOf<String?>(null) }
    val cal = remember { Calendar.getInstance() }
    val mesActual = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.uppercase() ?: "MES"
    val anioActual = cal.get(Calendar.YEAR)
    val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🗓️ Calendario de Movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("$mesActual $anioActual", fontSize = 13.sp, color = GrisTexto, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                // Matriz de Días del Mes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items((1..diasEnMes).toList()) { dia ->
                        val diaFormateado = String.format("%02d", dia)
                        val mesFormateado = String.format("%02d", cal.get(Calendar.MONTH) + 1)
                        val fechaIso = "$anioActual-$mesFormateado-$diaFormateado"

                        val movsDia = listaTransacciones.filter { it.fechaIso == fechaIso }
                        val tieneGastos = movsDia.any { it.tipo == "GASTO" }
                        val tieneIngresos = movsDia.any { it.tipo == "INGRESO" }

                        val estaSeleccionado = fechaSeleccionadaIso == fechaIso

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (estaSeleccionado) AzulSuave
                                    else if (movsDia.isNotEmpty()) Color.LightGray.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (estaSeleccionado) AzulPrimario else Color.LightGray.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { fechaSeleccionadaIso = fechaIso },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dia.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (movsDia.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                    color = if (estaSeleccionado) Color.White else Color.Unspecified
                                )
                                if (movsDia.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (tieneIngresos) {
                                            Box(modifier = Modifier.size(4.dp).background(VerdeExito, CircleShape))
                                        }
                                        if (tieneGastos) {
                                            Box(modifier = Modifier.size(4.dp).background(RojoPeligro, CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detalle del día seleccionado
                fechaSeleccionadaIso?.let { fecha ->
                    val detalleDia = listaTransacciones.filter { it.fechaIso == fecha }
                    Text("Movimientos del $fecha:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulPrimario)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (detalleDia.isEmpty()) {
                        Text("No hay movimientos en esta fecha.", fontSize = 11.sp, color = GrisTexto)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            detalleDia.forEach { t ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${t.descripcion}", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Text(
                                        "${if (t.tipo == "GASTO") "-" else "+"}Q.${String.format("%.2f", t.monto)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (t.tipo == "GASTO") RojoPeligro else VerdeExito
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = AzulPrimario, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialAvanzado(nombreUsuario: String, dbHelper: AdminSQLiteOpenHelper) {
    val context = LocalContext.current
    var balanceActual by remember { mutableStateOf(dbHelper.obtenerBalanceTotal()) }
    var listaHistorial by remember { mutableStateOf(dbHelper.obtenerTodasLasTransacciones()) }
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroTiempo by remember { mutableStateOf("TODO") }

    val historialFiltrado = remember(listaHistorial, textoBusqueda, filtroTiempo) {
        val hoySdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoyIso = hoySdf.format(Date())

        listaHistorial.filter { item ->
            val coincideTexto = item.descripcion.contains(textoBusqueda, ignoreCase = true) ||
                    item.categoria.contains(textoBusqueda, ignoreCase = true)

            val coincideTiempo = when (filtroTiempo) {
                "HOY" -> item.fechaIso == fechaHoyIso
                else -> true
            }
            coincideTexto && coincideTiempo
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Libro Diario & Auditoría", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { GeneradorPdf.generarYAbrirReporte(context, nombreUsuario, historialFiltrado, balanceActual) },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Exportar PDF", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por concepto o cuenta...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(historialFiltrado) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.descripcion, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${item.fechaLegible} • ${item.categoria}", fontSize = 11.sp, color = GrisTexto)
                        Text(
                            text = "Base: Q.${String.format("%.2f", item.baseImponible)} | IVA (12%): Q.${String.format("%.2f", item.impuesto)}",
                            fontSize = 11.sp,
                            color = AzulSuave
                        )
                    }

                    Text(
                        text = "${if (item.tipo == "GASTO") "-" else "+"}Q.${String.format("%.2f", item.monto)}",
                        color = if (item.tipo == "GASTO") RojoPeligro else VerdeExito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaAjustes(
    nombreUsuario: String,
    dbHelper: AdminSQLiteOpenHelper,
    esModoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    var mostrarConfirmarCerrarSesion by remember { mutableStateOf(false) }
    var mostrarConfirmarEliminarCuenta by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.don_quetzalito),
            contentDescription = "Perfil Don Quetzalito",
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = nombreUsuario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "Contribuyente Registrado", fontSize = 12.sp, color = GrisTexto)

        Spacer(modifier = Modifier.height(28.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Modo Oscuro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Ajustar tema visual", fontSize = 11.sp, color = GrisTexto)
                }
                Switch(checked = esModoOscuro, onCheckedChange = { onCambiarModoOscuro(it) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GuateConta v1.1.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AzulSuave)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Régimen Fiscal: General (12% IVA)", fontSize = 11.sp, color = GrisTexto)
                Text("Base de Datos: SQLite Local en Dispositivo", fontSize = 11.sp, color = GrisTexto)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { mostrarConfirmarCerrarSesion = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { mostrarConfirmarEliminarCuenta = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoPeligro),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Eliminar Cuenta y Borrar Datos", fontWeight = FontWeight.Bold)
        }

        if (mostrarConfirmarCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarCerrarSesion = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Deseas salir de la cuenta?") },
                confirmButton = {
                    TextButton(onClick = {
                        dbHelper.cerrarSesionUsuario()
                        mostrarConfirmarCerrarSesion = false
                        onCerrarSesion()
                    }) { Text("Aceptar", color = AzulPrimario) }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarCerrarSesion = false }) { Text("Cancelar") }
                }
            )
        }

        if (mostrarConfirmarEliminarCuenta) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarEliminarCuenta = false },
                title = { Text("¿Eliminar Cuenta?") },
                text = { Text("Se borrarán todos los datos y asientos registrados.") },
                confirmButton = {
                    TextButton(onClick = {
                        dbHelper.eliminarCuentaYDatos()
                        mostrarConfirmarEliminarCuenta = false
                        onCerrarSesion()
                    }) { Text("Eliminar Todo", color = RojoPeligro) }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarEliminarCuenta = false }) { Text("Cancelar") }
                }
            )
        }
    }
}