package com.socialvibe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.ui.theme.InkMuted
import com.socialvibe.app.ui.theme.InkStrong

// Built on BasicTextField rather than Material3's TextField/OutlinedTextField
// on purpose: TextFieldDefaults' color-override API has shifted across
// Material3 versions and this project's exact version isn't knowable from
// here, so a hand-styled BasicTextField (a much smaller, long-stable
// surface) is the safer bet for code that can't be compiled to check.
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    backgroundColor: Color = Color.White.copy(alpha = 0.92f),
    textColor: Color = InkStrong,
    placeholderColor: Color = InkMuted
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = textColor),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions.Default
        },
        cursorBrush = SolidColor(textColor),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, color = placeholderColor, fontSize = 14.sp)
                }
                innerTextField()
            }
        }
    )
}
