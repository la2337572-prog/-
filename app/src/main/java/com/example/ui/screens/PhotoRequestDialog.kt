package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandOrange
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoRequestDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String?, (Long) -> Unit) -> Unit,
    whatsappNumber: String
) {
    if (!isOpen) return

    val context = LocalContext.current
    var partName by remember { mutableStateOf("") }
    var bikeModel by remember { mutableStateOf("") }
    var bikeYear by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var submittedId by remember { mutableStateOf<Long?>(null) }

    // Photo picker using Google Play compliant zero-permission PickVisualMedia
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    if (submittedId != null) {
        AlertDialog(
            onDismissRequest = {
                submittedId = null
                onDismiss()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "تم استلام طلب القطعة بنجاح!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "تم تسجيل طلبك برقم #${submittedId}. سيقوم مسؤولو قطع الغيار بالبحث عنها وإشعارك بالتوفر والسعر.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanPhone = whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
                        val text = """
                            السلام عليكم، أرسلت طلب قطعة خاصة من التطبيق:
                            - اسم القطعة: $partName
                            - نوع الدراجة: $bikeModel
                            - الموديل/السنة: $bikeYear
                            - رقم الطلب: #${submittedId}
                            أرجو الإفادة بتوفرها وسعرها.
                        """.trimIndent()
                        val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=$encoded"))
                        context.startActivity(intent)
                        submittedId = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال التفاصيل لواتساب")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    submittedId = null
                    onDismiss()
                }) {
                    Text("إغلاق")
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BrandOrange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "طلب صورة قطعة غير متوفرة",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "لم تجد القطعة في الكتالوج؟ ارفع صورتها وسنوفرها لك بأسرع وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Photo selection box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("box_pick_photo"),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "الصورة المختارة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "انقر لاختيار صورة القطعة أو الاستمارة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = { Text("اسم القطعة (مثال: كويل كهرباء، رديتر)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bikeModel,
                        onValueChange = { bikeModel = it },
                        label = { Text("موديل الدراجة") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bikeYear,
                        onValueChange = { bikeYear = it },
                        label = { Text("السنة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الجوال للتواصل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية (أصلي / تجاري / رقم الهيكل)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = AccentRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (partName.isBlank() || bikeModel.isBlank() || phone.isBlank()) {
                        errorMessage = "يرجى تعبئة اسم القطعة ونوع الدراجة ورقم التواصل"
                    } else {
                        onSubmit(
                            partName,
                            bikeModel,
                            bikeYear,
                            phone,
                            notes,
                            photoUri?.toString()
                        ) { id ->
                            submittedId = id
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("إرسال الطلب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
