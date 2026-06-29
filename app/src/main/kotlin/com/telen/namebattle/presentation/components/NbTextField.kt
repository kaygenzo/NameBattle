package com.telen.namebattle.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NbTheme

/** Prototype `.inp`: bg2 fill, border2, radius 10. */
@Composable
fun NbTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val c = NbTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { p -> { Text(p) } },
        singleLine = true,
        isError = isError,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions.Default
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = c.bg2,
            unfocusedContainerColor = c.bg2,
            errorContainerColor = c.bg2,
            focusedBorderColor = c.accent,
            unfocusedBorderColor = c.border2,
            focusedTextColor = c.textHi,
            unfocusedTextColor = c.textHi,
            cursorColor = c.accent,
            focusedLabelColor = c.accent,
            unfocusedLabelColor = c.textMid,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
