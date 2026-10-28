package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminUnlockDialog(
  onDismiss: () -> Unit,
  onVerifyPin: (String) -> Boolean
) {
  var pinInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = SurfaceDark,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("admin_unlock_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Admin Lock",
              tint = LiveRed,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Broadcaster / Admin Access",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = TextPrimary
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "साधारण दर्शकहरूका लागि लिंक थप्ने वा ब्रोडकास्ट गर्ने सुविधा बन्द गरिएको छ। केवल एडमिनले मात्र पिन राखेर खोल्न सक्नुहुन्छ।",
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Enter Admin PIN (Default: 1234)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = BroadcastCyan
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
          value = pinInput,
          onValueChange = {
            pinInput = it
            errorMessage = null
          },
          placeholder = { Text("Enter 4-digit PIN", color = TextMuted) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BroadcastCyan)
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (onVerifyPin(pinInput)) {
                onDismiss()
              } else {
                errorMessage = "Incorrect PIN code. Please try again."
              }
            }
          ),
          visualTransformation = PasswordVisualTransformation(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceVariantDark,
            unfocusedContainerColor = SurfaceVariantDark,
            focusedBorderColor = BroadcastCyan,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_pin_input")
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = LiveRed,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = TextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (onVerifyPin(pinInput)) {
                onDismiss()
              } else {
                errorMessage = "Incorrect PIN code. Default is 1234."
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("submit_admin_pin_button")
          ) {
            Text("Unlock Studio 🔓", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
