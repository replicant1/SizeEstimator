package com.example.sizeestimator.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MeasureButtonPanel(
    sizeText : String?,
    onButtonClick : () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            shape = RoundedCornerShape(10.dp),
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color.Blue),
            modifier = Modifier
                .fillMaxWidth(1F)
                .fillMaxHeight(0.6F)
        ){
            Text(text = "Measure", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.weight(1F))
        Text(
            text = if (sizeText == null) "" else "Size: $sizeText",
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth(1F))
        Spacer(modifier = Modifier.weight(1F))
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ButtonPanelPreview() {
MeasureButtonPanel("90 x 90 mm") {  }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ButtonPanelNullSizeTextPreview() {
MeasureButtonPanel(null) { }
}