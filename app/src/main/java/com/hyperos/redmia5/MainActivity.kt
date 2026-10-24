package com.hyperos.redmia5

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var appsGrid: GridLayout
    private lateinit var allApps: List<ResolveInfo>
    private lateinit var search: EditText
    private val prefs by lazy { getSharedPreferences("hyperos_a5", MODE_PRIVATE) }
    private var query = ""
    private var currentTheme = "HyperOS 3 · Claro"
    private var bgColor = 0xFFF3F5FA.toInt()
    private var primaryColor = 0xFF356AE6.toInt()
    private var textColor = 0xFF182033.toInt()
    private var secondaryColor = 0xFF697386.toInt()
    private var wallpaperUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentTheme = prefs.getString("theme", currentTheme) ?: currentTheme
        wallpaperUri = prefs.getString("wallpaper", null)?.let { Uri.parse(it) }
        applyPalette(currentTheme)
        window.statusBarColor = primaryColor
        window.navigationBarColor = if (currentTheme.contains("Oscuro")) 0xFF10131A.toInt() else 0xFFE9EDF5.toInt()
        window.decorView.systemUiVisibility = if (currentTheme.contains("Oscuro")) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        allApps = loadApps()
        buildHome()
    }

    private fun loadApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun text(value: String, size: Float, color: Int = textColor, bold: Boolean = false): TextView = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private fun buildHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(12))
            background = rounded(bgColor, 0)
        }
        wallpaperUri?.let { uri -> try {
            val stream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream); stream?.close()
            if (bitmap != null) root.background = BitmapDrawable(resources, bitmap)
        } catch (_: Exception) { wallpaperUri = null } }

        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val dateBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val date = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date())
        dateBox.addView(text(date.replaceFirstChar { it.uppercase() }, 14f))
        dateBox.addView(text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), 48f, textColor, true))
        dateBox.addView(text("Tu día, de un vistazo", 13f, secondaryColor))
        top.addView(dateBox)
        val themeButton = TextView(this).apply {
            text = "✦\nTemas"; gravity = Gravity.CENTER; textSize = 12f; setTextColor(textColor)
            background = rounded(0x33FFFFFF, 20); setPadding(dp(13), dp(8), dp(13), dp(8))
            setOnClickListener { showThemeStore() }
        }
        top.addView(themeButton, LinearLayout.LayoutParams(dp(68), dp(62)))
        root.addView(top)

        search = EditText(this).apply {
            hint = "Buscar aplicaciones"; textSize = 15f; setSingleLine(true)
            setTextColor(textColor); setHintTextColor(secondaryColor)
            setPadding(dp(18), 0, dp(16), 0); background = rounded(0x33FFFFFF, 28)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(18); bottomMargin = dp(12) })
        val scroll = ScrollView(this).apply { clipToPadding = false }
        appsGrid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        scroll.addView(appsGrid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        search.addTextChangedListener(SimpleTextWatcher { value -> query = value; renderApps(value) })

        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val themeLabel = text("${currentTheme}  ·  Personalizar", 12f, secondaryColor)
        themeLabel.gravity = Gravity.CENTER
        themeLabel.setOnClickListener { showThemeStore() }
        bottom.addView(themeLabel, LinearLayout.LayoutParams(-1, dp(42)))
        root.addView(bottom)
        setContentView(root)
        renderApps(query)
        root.setOnLongClickListener { showThemeStore(); true }
    }

    private fun applyPalette(name: String) {
        when (name) {
            "HyperOS 2 · Claro" -> { bgColor = 0xFFF6F7FB.toInt(); primaryColor = 0xFF3677E8.toInt(); textColor = 0xFF172033.toInt(); secondaryColor = 0xFF68758A.toInt() }
            "HyperOS 2 · Oscuro" -> { bgColor = 0xFF171A20.toInt(); primaryColor = 0xFF4D82F2.toInt(); textColor = Color.WHITE; secondaryColor = 0xFFB7C0D0.toInt() }
            "HyperOS 3 · Oscuro" -> { bgColor = 0xFF10131A.toInt(); primaryColor = 0xFF789CFF.toInt(); textColor = 0xFFF4F6FC.toInt(); secondaryColor = 0xFFADB7C9.toInt() }
            else -> { bgColor = 0xFFF3F5FA.toInt(); primaryColor = 0xFF356AE6.toInt(); textColor = 0xFF182033.toInt(); secondaryColor = 0xFF697386.toInt() }
        }
    }

    private fun showThemeStore() {
        val options = arrayOf(
            "Aplicar · HyperOS 2 Claro", "Aplicar · HyperOS 2 Oscuro",
            "Aplicar · HyperOS 3 Claro", "Aplicar · HyperOS 3 Oscuro",
            "Importar fondo de pantalla", "Cambiar icono de una aplicación", "Quitar fondo importado"
        )
        AlertDialog.Builder(this).setTitle("Tienda de temas · HyperOS A5")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> applyTheme("HyperOS 2 · Claro")
                    1 -> applyTheme("HyperOS 2 · Oscuro")
                    2 -> applyTheme("HyperOS 3 · Claro")
                    3 -> applyTheme("HyperOS 3 · Oscuro")
                    4 -> pickWallpaper()
                    5 -> chooseAppForIcon()
                    6 -> { wallpaperUri = null; prefs.edit().remove("wallpaper").apply(); buildHome() }
                }
            }.setNegativeButton("Cerrar", null).show()
    }

    private fun applyTheme(name: String) {
        currentTheme = name; prefs.edit().putString("theme", name).apply(); applyPalette(name)
        window.statusBarColor = primaryColor
        window.navigationBarColor = if (name.contains("Oscuro")) 0xFF10131A.toInt() else 0xFFE9EDF5.toInt()
        window.decorView.systemUiVisibility = if (name.contains("Oscuro")) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        buildHome()
        Toast.makeText(this, "Tema aplicado: $name", Toast.LENGTH_SHORT).show()
    }

    private fun pickWallpaper() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "image/*" }
        try { startActivityForResult(intent, REQUEST_WALLPAPER) } catch (_: Exception) { Toast.makeText(this, "No hay selector de archivos disponible", Toast.LENGTH_SHORT).show() }
    }

    private fun chooseAppForIcon() {
        if (allApps.isEmpty()) return
        val labels = allApps.map { it.loadLabel(packageManager).toString() }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Elige la aplicación")
            .setItems(labels) { _, index -> selectedPackageForIcon = allApps[index].activityInfo.packageName; pickAppIcon() }
            .setNegativeButton("Cancelar", null).show()
    }

    private var selectedPackageForIcon: String? = null
    private fun pickAppIcon() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "image/*" }
        try { startActivityForResult(intent, REQUEST_ICON) } catch (_: Exception) { Toast.makeText(this, "No hay selector de archivos disponible", Toast.LENGTH_SHORT).show() }
    }

    @Deprecated("Deprecated in Android API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
        when (requestCode) {
            REQUEST_WALLPAPER -> {
                wallpaperUri = uri; prefs.edit().putString("wallpaper", uri.toString()).apply(); buildHome()
                Toast.makeText(this, "Fondo importado", Toast.LENGTH_SHORT).show()
            }
            REQUEST_ICON -> {
                val pkg = selectedPackageForIcon ?: return
                prefs.edit().putString("icon_$pkg", uri.toString()).apply(); renderApps(query)
                Toast.makeText(this, "Icono guardado para esta aplicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderApps(filter: String) {
        appsGrid.removeAllViews()
        val matches = allApps.filter { it.loadLabel(packageManager).toString().contains(filter.trim(), true) }
        matches.forEach { info ->
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(2), dp(7), dp(2), dp(7)); isClickable = true; isFocusable = true }
            val icon = ImageView(this).apply {
                val custom = prefs.getString("icon_${info.activityInfo.packageName}", null)
                if (custom != null) try {
                    val stream = contentResolver.openInputStream(Uri.parse(custom)); val bitmap = BitmapFactory.decodeStream(stream); stream?.close()
                    if (bitmap != null) setImageBitmap(bitmap) else setImageDrawable(info.loadIcon(packageManager))
                } catch (_: Exception) { setImageDrawable(info.loadIcon(packageManager)) }
                else setImageDrawable(info.loadIcon(packageManager))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            item.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
            val label = text(info.loadLabel(packageManager).toString(), 11f, textColor); label.gravity = Gravity.CENTER; label.maxLines = 2
            item.addView(label, LinearLayout.LayoutParams(-1, dp(34)))
            item.setOnClickListener { launchApp(info) }
            item.setOnLongClickListener {
                AlertDialog.Builder(this).setTitle(info.loadLabel(packageManager).toString())
                    .setItems(arrayOf("Abrir", "Información de la aplicación", "Cambiar icono personalizado")) { _, which ->
                        when (which) {
                            0 -> launchApp(info)
                            1 -> { val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS); intent.data = Uri.parse("package:${info.activityInfo.packageName}"); startActivity(intent) }
                            2 -> { selectedPackageForIcon = info.activityInfo.packageName; pickAppIcon() }
                        }
                    }.show(); true
            }
            appsGrid.addView(item, GridLayout.LayoutParams().apply { width = 0; height = dp(94); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) })
        }
        if (matches.isEmpty()) appsGrid.addView(text("No se encontraron aplicaciones", 14f))
    }

    private fun launchApp(info: ResolveInfo) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(info.activityInfo.packageName, info.activityInfo.name)
        try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this, "No se pudo abrir la aplicación", Toast.LENGTH_SHORT).show() }
    }

    companion object { private const val REQUEST_WALLPAPER = 1201; private const val REQUEST_ICON = 1202 }
}
