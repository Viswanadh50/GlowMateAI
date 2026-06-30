package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SkincareAppContent
import com.example.ui.SkincareViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: SkincareViewModel = viewModel()
      MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
        SkincareAppContent(viewModel)
      }
    }
  }
}
