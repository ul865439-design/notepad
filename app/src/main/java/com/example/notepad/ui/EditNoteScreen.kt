package com.example.notepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.notepad.data.Note
import com.example.notepad.data.NoteRepository
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { NoteRepository(context) }
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    val isNewNote = noteId == "new"
    
    // 加载已有笔记
    LaunchedEffect(noteId) {
        if (!isNewNote) {
            repository.notes.collect { notes ->
                notes.find { it.id == noteId }?.let { note ->
                    title = note.title
                    content = note.content
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "新建笔记" else "编辑笔记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (!isNewNote) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "删除")
                        }
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                val note = Note(
                                    id = if (isNewNote) UUID.randomUUID().toString() else noteId,
                                    title = title,
                                    content = content,
                                    timestamp = System.currentTimeMillis()
                                )
                                repository.saveNote(note)
                                snackbarMessage = "笔记已保存"
                                onBack()
                            }
                        },
                        enabled = title.isNotEmpty() || content.isNotEmpty()
                    ) {
                        Text("保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标题") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("写点什么...") },
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条笔记吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteNote(noteId)
                            showDeleteDialog = false
                            snackbarMessage = "笔记已删除"
                            onBack()
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // Snackbar 提示
    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            // 可以在这里显示 Snackbar
            snackbarMessage = null
        }
    }
}