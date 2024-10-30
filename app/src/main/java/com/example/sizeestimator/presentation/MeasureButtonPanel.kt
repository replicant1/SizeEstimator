package com.example.sizeestimator.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Composable
fun MeasureButtonPanel(
    sizeText: String?,
    progressMonitorVisible: Boolean?,
    onButtonClick: () -> Unit,
    errorFlow : Flow<String>
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        errorFlow.collect { msg ->
            Toast.makeText(
                context,
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    Column(modifier = Modifier.background(Color.Green).padding(16.dp)) {
        Button(
            enabled = (progressMonitorVisible == false),
            shape = RoundedCornerShape(10.dp),
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6F)
        ) {
            Text(text = "Measure", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.weight(1F))
        if (progressMonitorVisible == true) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        } else {
            Text(
                text = if (sizeText == null) "" else "Size: $sizeText",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(1F)
            )
        }
        Spacer(modifier = Modifier.weight(1F))
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ButtonPanelWithProgressPreview() {
    MeasureButtonPanel(
        "90 x 90 mm",
        progressMonitorVisible = true,
        {},
        flow { })
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ButtonPanelNoProgressPreview() {
    MeasureButtonPanel(
        "90 x 90 mm",
        progressMonitorVisible = false,
        {},
        flow {})
}