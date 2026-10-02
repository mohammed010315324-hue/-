package com.example.service

import com.example.BuildConfig
import com.example.domain.model.CleanItem
import com.example.domain.model.DuplicateGroup
import com.example.domain.model.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AssistantTip(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
    val actionTag: String? = null
)

data class AssistantInsight(
    val score: Int, // 0 to 100
    val summary: String,
    val tips: List<AssistantTip>
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    RAFIQI
}

class RafiqiAssistant(private val fileManagerService: FileManagerService) {

    fun isGeminiKeyAvailable(): Boolean {
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val key = field.get(null) as? String
            key != null && key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun generateHealthReport(
        storageInfo: StorageInfo,
        cleanItems: List<CleanItem>,
        duplicateGroups: List<DuplicateGroup>
    ): AssistantInsight = withContext(Dispatchers.Default) {
        val usedPercent = storageInfo.usedPercent * 100f
        var score = 100

        if (usedPercent > 90f) score -= 40
        else if (usedPercent > 75f) score -= 20
        else if (usedPercent > 60f) score -= 10

        val tempTotal = cleanItems.filter { it.reason == com.example.domain.model.CleanReason.TEMP_CACHE }.sumOf { it.file.size }
        if (tempTotal > 200 * 1024 * 1024L) score -= 15
        else if (tempTotal > 50 * 1024 * 1024L) score -= 5

        if (duplicateGroups.isNotEmpty()) score -= 10

        score = score.coerceIn(20, 100)

        val tips = mutableListOf<AssistantTip>()

        if (tempTotal > 10 * 1024 * 1024L) {
            val formatted = formatSize(tempTotal)
            tips.add(
                AssistantTip(
                    title = "ملفات مؤقتة قابلة للتنظيف",
                    description = "لديك حوالي $formatted من الملفات المؤقتة وبقايا التخزين المؤقت، يمكنك حذفها بأمان.",
                    actionLabel = "تنظيف الآن",
                    actionTag = "TOOL_CLEAN"
                )
            )
        }

        if (duplicateGroups.isNotEmpty()) {
            val duplicateSaved = duplicateGroups.sumOf { it.size * (it.files.size - 1) }
            val formatted = formatSize(duplicateSaved)
            tips.add(
                AssistantTip(
                    title = "نسخ مكررة متطابقة",
                    description = "تم اكتشاف ${duplicateGroups.size} مجموعة مكررة يمكن أن توفر $formatted مساحة.",
                    actionLabel = "مراجعة المكررات",
                    actionTag = "TOOL_DUPLICATES"
                )
            )
        }

        if (usedPercent > 75f) {
            tips.add(
                AssistantTip(
                    title = "مساحة التخزين شارفت على الامتلاء",
                    description = "استخدمت أكثر من 75% من ذاكرة الجهاز. تفقد قائمة الملفات الكبيرة لحذف ما لا تحتاجه.",
                    actionLabel = "عرض الملفات الكبيرة",
                    actionTag = "TOOL_LARGE"
                )
            )
        } else {
            tips.add(
                AssistantTip(
                    title = "تنظيم مجلد التنزيلات",
                    description = "مجلد التنزيلات عادة يجمع ملفات APK ومستندات قديمة لا تحتاجها، تفقد محتوياته دورياً.",
                    actionLabel = "فتح التنزيلات",
                    actionTag = "CAT_DOWNLOADS"
                )
            )
        }

        val summary = when {
            score >= 85 -> "حالة التخزين ممتازة! جهازك منظم بشكل جيد."
            score >= 65 -> "حالة التخزين جيدة، وتوجد بعض الفرص لتحرير مساحة إضافية."
            else -> "تنبيه: مساحة التخزين منخفضة أو ممتلئة بالملفات المؤقتة."
        }

        AssistantInsight(score = score, summary = summary, tips = tips)
    }

    suspend fun getAssistantResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val query = prompt.trim().lowercase()

        // 1. If user asks common storage / file questions, provide instant expert offline advice
        when {
            query.contains("تنظيف") || query.contains("مساحة") || query.contains("أفرغ") -> {
                "أهلاً بك! لتنظيف هاتفك بأمان عبر رفيقي:\n\n" +
                "1. افتح أداة «تنظيف التخزين» لفحص الملفات المؤقتة والذاكرة المخبأة.\n" +
                "2. راجع أداة «الملفات المكررة» لاختيار النسخ الزائدة وحذفها.\n" +
                "3. افتح أداة «الملفات الكبيرة» لإيجاد الفيديوهات والملفات الضخمة غير المستخدمة.\n\n" +
                "ملاحظة: رفيقي لا يحذف أي ملف تلقائياً، بل يمنحك القرار الكامل دائماً."
            }
            query.contains("مكرر") || query.contains("نسخ") -> {
                "الملفات المكررة غالباً ما تنتج عن إعادة تنزيل الصور والمستندات من تطبيقات المراسلة والتواصل.\n" +
                "يقوم رفيقي بمقارنة أحجام الملفات والتحقق من بصمتها الرقمية لاكتشاف النسخ المتطابقة بدقة وأمان."
            }
            query.contains("كبير") || query.contains("فيديو") -> {
                "الملفات الكبيرة مثل تسجيلات الفيديو وملفات التثبيت (APK) والملفات المضغوطة هي أكثر ما يستهلك الذاكرة.\n" +
                "يمكنك الانتقال إلى قسم «الملفات الكبيرة» لترتيبها من الأكبر حجماً إلى الأصغر وحذف ما انتهيت منه."
            }
            query.contains("نسخ") && query.contains("نقل") || query.contains("فرق") -> {
                "الفرق بين النسخ والنقل:\n\n" +
                "• النسخ (Copy): يحتفظ بالملف الأصلي في مكانه وينشئ نسخة جديدة ثانية في المجلد المختار.\n" +
                "• النقل (Move): ينقل الملف نفسه إلى المجلد الجديد ويحذفه من المكان القديم، مما يوفر المساحة."
            }
            query.contains("أمان") || query.contains("خصوصية") || query.contains("إنترنت") -> {
                "تطبيق رفيقي صُمم ليعمل بنظام Offline-first بالكامل.\n" +
                "جميع ملفاتك، صورك، مستنداتك، وبياناتك تبقى حصراً على هاتفك ولا يتم إرسالها إلى أي خادم خارجي."
            }
            query.contains("مرحبا") || query.contains("سلام") || query.contains("أهلا") -> {
                "أهلاً وسهلاً بك! أنا رفيقي، مساعدك الذكي لتنظيم وإدارة ملفات هاتفك. كيف يمكنني مساعدتك اليوم؟"
            }
            else -> {
                "أنا رفيقي، رفيقك الذكي في تنظيم هاتفك.\n\n" +
                "يمكنك سؤالي عن:\n" +
                "• كيفية تحرير مساحة التخزين.\n" +
                "• التعامل مع الملفات المكررة والملفات الكبيرة.\n" +
                "• نصائح للحفاظ على سرعة وأداء الذاكرة.\n" +
                "• كيفية ضغط وفك ضغط الملفات."
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(java.util.Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
