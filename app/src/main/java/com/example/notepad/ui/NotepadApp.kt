package com.example.notepad.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NotepadApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "noteList") {
        composable("noteList") {
            NoteListScreen(
                onNoteClick = { noteId ->
                    navController.navigate("editNote/$noteId")
                },
                onAddClick = {
                    navController.navigate("editNote/new")
                }
            )
        }
        composable("editNote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
            EditNoteScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}