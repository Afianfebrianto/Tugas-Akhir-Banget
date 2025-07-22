package com.afian.tugasakhir.Component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private fun getMonthNameDialog(monthNumber: Int): String {
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    return if (monthNumber in 1..12) months[monthNumber - 1] else "Invalid Month"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    yearRange: List<Int>,
    onDismissRequest: () -> Unit,
    onConfirm: (selectedMonth: Int, selectedYear: Int) -> Unit
) {
    var tempSelectedMonth by remember { mutableIntStateOf(initialMonth) }
    var tempSelectedYear by remember { mutableIntStateOf(initialYear) }

    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val months = remember { (1..12).map { Pair(it, getMonthNameDialog(it)) } }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Pilih Periode Laporan",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = monthMenuExpanded,
                        onExpandedChange = { monthMenuExpanded = !monthMenuExpanded },
                        modifier = Modifier.weight(1f) // Ambil sisa ruang
                    ) {
                        OutlinedTextField(
                            value = getMonthNameDialog(tempSelectedMonth),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bulan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false }
                        ) {
                            months.forEach { (monthNumber, monthName) ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        tempSelectedMonth = monthNumber
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = yearMenuExpanded,
                        onExpandedChange = { yearMenuExpanded = !yearMenuExpanded },
                        modifier = Modifier.weight(0.8f) // Lebar lebih kecil
                    ) {
                        OutlinedTextField(
                            value = tempSelectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tahun") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false }
                        ) {
                            yearRange.forEach { yearValue ->
                                DropdownMenuItem(
                                    text = { Text(yearValue.toString()) },
                                    onClick = {
                                        tempSelectedYear = yearValue
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(tempSelectedMonth, tempSelectedYear)
                    }) {
                        Text("Pilih")
                    }
                }
            }
        }
    }
}