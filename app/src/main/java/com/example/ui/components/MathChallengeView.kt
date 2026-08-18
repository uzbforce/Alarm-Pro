package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun MathChallengeView(
    onSolved: () -> Unit
) {
    var num1 by remember { mutableStateOf(Random.nextInt(12, 45)) }
    var num2 by remember { mutableStateOf(Random.nextInt(15, 38)) }
    var isAddition by remember { mutableStateOf(Random.nextBoolean()) }
    val correctAnswer = remember(num1, num2, isAddition) {
        if (isAddition) num1 + num2 else num1 * num2 / 3
    }

    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Wake Up Brain Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val opSymbol = if (isAddition) "+" else "×"
            Text(
                text = "$num1 $opSymbol $num2 = ?",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = {
                    userInput = it
                    isError = false
                },
                isError = isError,
                label = { Text("Your Answer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isError) {
                Text(
                    text = "Incorrect! Try again.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (userInput.trim().toIntOrNull() == correctAnswer) {
                        onSolved()
                    } else {
                        isError = true
                        userInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Answer", fontWeight = FontWeight.Bold)
            }
        }
    }
}
