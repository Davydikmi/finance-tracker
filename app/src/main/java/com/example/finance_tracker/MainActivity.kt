package com.example.finance_tracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.data.local.entity.TransactionEntity
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.ui.theme.FinancetrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Смоук-тест базы данных: вставляем категорию и транзакцию, читаем обратно.
 * Результат показывается на экране и в Logcat (тег "DB_TEST").
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt сам внедрит DAO из DatabaseModule.
    // @AndroidEntryPoint на классе обязателен, иначе поля останутся null.
    @Inject
    lateinit var transactionDao: TransactionDao

    @Inject
    lateinit var categoryDao: CategoryDao

    private var dbTestResult by mutableStateOf("Тест БД: запуск…")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancetrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Финансовый трекер")
                        Text(dbTestResult)
                    }
                }
            }
        }

        testDatabase()
    }

    private fun testDatabase() {
        // lifecycleScope привязан к Activity и отменяется вместе с ней,
        // в отличие от "голого" CoroutineScope(Dispatchers.IO).
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Вставляем тестовую категорию
                val categoryId = categoryDao.insert(
                    CategoryEntity(
                        name = "Тестовая категория",
                        icon = "restaurant",
                        color = 0xFF4CAF50,
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    )
                )
                Log.d("DB_TEST", "✅ Категория создана с id=$categoryId")

                // 2. Вставляем тестовую транзакцию
                val transactionId = transactionDao.insert(
                    TransactionEntity(
                        type = TransactionType.EXPENSE,
                        amount = 150.50,
                        categoryId = categoryId,
                        date = System.currentTimeMillis(),
                        description = "Тестовая транзакция"
                    )
                )
                Log.d("DB_TEST", "✅ Транзакция создана с id=$transactionId")

                // 3. Читаем обратно
                val transaction = transactionDao.getById(transactionId)
                Log.d("DB_TEST", "✅ Прочитано: ${transaction?.description}, ${transaction?.amount}")

                Log.d("DB_TEST", "🎉 БД работает полностью!")
                dbTestResult = "🎉 БД работает: ${transaction?.description} — ${transaction?.amount}"
            } catch (e: Exception) {
                Log.e("DB_TEST", "❌ Ошибка БД: ${e.message}", e)
                dbTestResult = "❌ Ошибка БД: ${e.message}"
            }
        }
    }
}
