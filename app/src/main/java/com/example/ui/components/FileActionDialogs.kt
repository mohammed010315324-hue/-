package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RafiqiFile
import com.example.domain.model.SortOption
import com.example.domain.model.ViewMode

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء مجلد جديد", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("أدخل اسم المجلد الذي ترغب في إنشائه:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        isError = it.isBlank()
                    },
                    label = { Text("اسم المجلد") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_folder_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim())
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("create_folder_confirm")
            ) {
                Text("إنشاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun RenameDialog(
    file: RafiqiFile,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(file.name) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة تسمية", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("تعديل اسم العنصر:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        isError = it.isBlank()
                    },
                    label = { Text("الاسم الجديد") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && newName != file.name) {
                        onConfirm(newName.trim())
                    } else if (newName == file.name) {
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("rename_confirm")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteConfirmDialog(
    itemName: String,
    isMultiple: Boolean = false,
    count: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تأكيد الحذف", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            val msg = if (isMultiple) {
                "هل أنت متأكد من رغبتك في حذف $count عناصر محددة نهائياً من هاتفك؟ لا يمكن التراجع عن هذا الإجراء."
            } else {
                "هل أنت متأكد من رغبتك في حذف «$itemName» نهائياً من هاتفك؟"
            }
            Text(msg, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("حذف نهائياً")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun FileDetailsDialog(
    file: RafiqiFile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تفاصيل العنصر", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "الاسم", value = file.name)
                DetailRow(label = "المسار", value = file.path)
                DetailRow(label = "النوع", value = if (file.isDirectory) "مجلد" else file.mimeType)
                if (!file.isDirectory) {
                    DetailRow(label = "الحجم", value = "${formatBytes(file.size)} (${file.size} بايت)")
                } else {
                    DetailRow(label = "عدد العناصر", value = "${file.itemCount} عنصر")
                }
                DetailRow(label = "تاريخ التعديل", value = formatDate(file.lastModified))
                DetailRow(label = "الحالة", value = if (file.exists) "متاح على الهاتف" else "غير موجود / تم نقله")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ZipDialog(
    suggestedName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var zipName by remember { mutableStateOf(suggestedName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ضغط إلى ZIP", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("أدخل اسم الملف المضغوط الجديد:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = zipName,
                    onValueChange = { zipName = it },
                    label = { Text("اسم الملف المضغوط") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (zipName.isNotBlank()) onConfirm(zipName.trim()) }
            ) {
                Text("ضغط")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
