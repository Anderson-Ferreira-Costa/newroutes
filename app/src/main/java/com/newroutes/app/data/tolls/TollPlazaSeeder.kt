package com.newroutes.app.data.tolls

import android.content.Context
import android.util.Log
import com.newroutes.app.domain.model.TollCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Popula a tabela toll_plazas com dados do CSV da ANTT na primeira
 * execução do app. Executado uma única vez via flag em SharedPreferences.
 */
@Singleton
class TollPlazaSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tollPlazaDao: TollPlazaDao
) {
    companion object {
        private const val PREF_NAME = "newroutes_seed"
        private const val KEY_SEEDED = "toll_plazas_seeded_v1"
        // Incrementar KEY_SEEDED (ex: v2, v3) para forçar re-seed
        // quando o CSV for atualizado
    }

    suspend fun seedIfNeeded() {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        try {
            val entities = parseCsv()
            if (entities.isNotEmpty()) {
                tollPlazaDao.upsertAll(entities)
                prefs.edit().putBoolean(KEY_SEEDED, true).apply()
                Log.i("TollPlazaSeeder", "Seed concluído: ${entities.size} praças inseridas")
            }
        } catch (e: Exception) {
            Log.e("TollPlazaSeeder", "Erro no seed de pedágios: ${e.message}", e)
            // Não marcar como seeded — tentar novamente no próximo launch
        }
    }

    private fun parseCsv(): List<TollPlazaEntity> {
        val entities = mutableListOf<TollPlazaEntity>()

        context.assets.open("pracas_pedagio.csv").bufferedReader(Charsets.UTF_8).use { reader ->
            val lines = reader.readLines()
            if (lines.isEmpty()) return emptyList()

            // Pular header (primeira linha)
            lines.drop(1).forEach { line ->
                try {
                    val cols = parseCsvLine(line)
                    // Formato: concessionaria,praca_de_pedagio,rodovia,uf,
                    //          municipio,latitude,longitude,preco_categoria_1,fonte_preco
                    if (cols.size < 8) return@forEach

                    val latitude = cols[5].trim().toDoubleOrNull() ?: return@forEach
                    val longitude = cols[6].trim().toDoubleOrNull() ?: return@forEach
                    val cost = cols[7].trim().toDoubleOrNull() ?: 0.0

                    // Validação de coordenadas — Brasil: lat -5 a -34, lon -35 a -74
                    if (latitude !in -34.0..-5.0 || longitude !in -74.0..-34.0) return@forEach

                    val entity = TollPlazaEntity(
                        id = UUID.randomUUID().toString(),
                        name = "${cols[1].trim()} — ${cols[0].trim()}",
                        highway = cols[2].trim(),
                        latitude = latitude,
                        longitude = longitude,
                        cost = cost,
                        category = TollCategory.CAR.name
                    )
                    entities.add(entity)
                } catch (e: Exception) {
                    Log.w("TollPlazaSeeder", "Linha ignorada: $line — ${e.message}")
                }
            }
        }

        return entities
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
