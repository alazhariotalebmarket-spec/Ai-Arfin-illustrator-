package com.example.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.res.ResourcesCompat
import com.example.R

data class StudioFontOption(
    val id: String,
    val displayName: String,
    val category: String, // "Bangla", "English", "Calligraphy", "Display", "Mono"
    @FontRes val fontRes: Int,
    val defaultBold: Boolean = false,
    val defaultItalic: Boolean = false,
    val letterSpacingEm: Float = 0f,
    val sampleText: String = "আরফিন ইলাস্ট্রেটর"
)

object FontCatalog {
    val fonts: List<StudioFontOption> = listOf(
        // Requested Bengali & Arabic/Modern Fonts (all previewed with "আরফিন ইলাস্ট্রেটর")
        StudioFontOption("hind_siliguri", "Hind Siliguri", "Bangla", R.font.hind_siliguri, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_serif_bengali", "Noto Serif Bengali", "Bangla", R.font.noto_serif_bengali, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_sans_bengali", "Noto Sans Bengali", "Bangla", R.font.noto_sans_bengali, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("baloo_da_2", "Baloo Da 2", "Bangla", R.font.baloo_da_2, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("tiro_bangla", "Tiro Bangla", "Bangla", R.font.tiro_bangla, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("amiri", "Amiri (Arabic & Islamic)", "Calligraphy", R.font.amiri, sampleText = "আরফিন ইলাস্ট্রেটর • بسم الله"),
        StudioFontOption("solaiman_lipi", "SolaimanLipi", "Bangla", R.font.noto_sans_bengali, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("sutonny_mj", "SutonnyMJ", "Bangla", R.font.tiro_bangla, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("siyam_rupali", "Siyam Rupali", "Bangla", R.font.hind_siliguri, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("bensen", "BenSen", "Bangla", R.font.noto_serif_bengali, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("charukola", "Charukola", "Bangla", R.font.anek_bangla, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("li_ador_noir", "Li Ador Noir", "Bangla", R.font.baloo_da_2, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("li_alinur", "Li Alinur", "Bangla", R.font.galada, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("mina", "Mina", "Bangla", R.font.mina, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("atma", "Atma", "Bangla", R.font.atma, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("galada", "Galada", "Calligraphy", R.font.galada, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("anek_bangla", "Anek Bangla", "Bangla", R.font.anek_bangla, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("jost", "Jost", "English", R.font.jost, sampleText = "আরফিন ইলাস্ট্রেটর • Jost"),

        // Additional Bengali Styled Variants
        StudioFontOption("hind_siliguri_bold", "Hind Siliguri Bold", "Bangla", R.font.hind_siliguri, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("hind_siliguri_wide", "Hind Siliguri Wide", "Bangla", R.font.hind_siliguri, letterSpacingEm = 0.08f, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_serif_bn_bold", "Noto Serif Bengali Bold", "Bangla", R.font.noto_serif_bengali, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_serif_bn_italic", "Noto Serif Bengali Italic", "Bangla", R.font.noto_serif_bengali, defaultItalic = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_sans_bn_bold", "Noto Sans Bengali Bold", "Bangla", R.font.noto_sans_bengali, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("noto_sans_bn_wide", "Noto Sans Bengali Wide", "Bangla", R.font.noto_sans_bengali, letterSpacingEm = 0.1f, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("baloo_da_bold", "Baloo Da 2 Bold", "Bangla", R.font.baloo_da_2, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("atma_bold", "Atma Display Bold", "Bangla", R.font.atma, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("galada_display", "Galada Calligraphy", "Calligraphy", R.font.galada, defaultItalic = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("mina_bold", "Mina Headline", "Bangla", R.font.mina, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("tiro_bangla_italic", "Tiro Bangla Italic", "Bangla", R.font.tiro_bangla, defaultItalic = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("anek_bangla_bold", "Anek Bangla Heavy", "Bangla", R.font.anek_bangla, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("anek_bangla_wide", "Anek Bangla Wide", "Bangla", R.font.anek_bangla, letterSpacingEm = 0.12f, sampleText = "আরফিন ইলাস্ট্রেটর"),

        // English Modern Sans & Display Fonts
        StudioFontOption("poppins", "Poppins", "English", R.font.poppins, sampleText = "আরফিন ইলাস্ট্রেটর • Poppins"),
        StudioFontOption("roboto", "Roboto", "English", R.font.roboto, sampleText = "আরফিন ইলাস্ট্রেটর • Roboto"),
        StudioFontOption("jost_bold", "Jost Bold", "English", R.font.jost, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর • Jost"),
        StudioFontOption("amiri_bold", "Amiri Bold", "Calligraphy", R.font.amiri, defaultBold = true, sampleText = "আরফিন ইলাস্ট্রেটর • محمد"),
        StudioFontOption("montserrat", "Montserrat", "English", R.font.montserrat, sampleText = "আরফিন ইলাস্ট্রেটর • Brand"),
        StudioFontOption("montserrat_bold", "Montserrat Bold", "English", R.font.montserrat, defaultBold = true, sampleText = "CREATIVE STUDIO"),
        StudioFontOption("montserrat_wide", "Montserrat Tracked", "English", R.font.montserrat, defaultBold = true, letterSpacingEm = 0.15f, sampleText = "LUXURY BRAND"),
        StudioFontOption("poppins_bold", "Poppins Bold", "English", R.font.poppins, defaultBold = true, sampleText = "UI & GRAPHICS"),
        StudioFontOption("poppins_italic", "Poppins Italic", "English", R.font.poppins, defaultItalic = true, sampleText = "Sleek Tagline"),
        StudioFontOption("roboto_bold", "Roboto Bold", "English", R.font.roboto, defaultBold = true, sampleText = "Clean Headline"),
        StudioFontOption("inter", "Inter", "English", R.font.inter, sampleText = "Crisp Interface"),
        StudioFontOption("inter_bold", "Inter Bold", "English", R.font.inter, defaultBold = true, sampleText = "Product Feature"),
        StudioFontOption("nunito", "Nunito", "English", R.font.nunito, sampleText = "Friendly Rounded"),
        StudioFontOption("raleway", "Raleway", "English", R.font.raleway, sampleText = "Elegant Fashion"),
        StudioFontOption("space_grotesk", "Space Grotesk", "Display", R.font.space_grotesk, sampleText = "TECH VECTOR"),
        StudioFontOption("space_grotesk_bold", "Space Grotesk Bold", "Display", R.font.space_grotesk, defaultBold = true, sampleText = "FUTURE LAB"),

        // Serif & Editorial Fonts
        StudioFontOption("playfair", "Playfair Display", "Display", R.font.playfair_display, sampleText = "Classic Luxury"),
        StudioFontOption("playfair_bold", "Playfair Bold", "Display", R.font.playfair_display, defaultBold = true, sampleText = "Royal Edition"),
        StudioFontOption("merriweather", "Merriweather", "English", R.font.merriweather, sampleText = "Editorial Serif"),
        StudioFontOption("merriweather_bold", "Merriweather Bold", "English", R.font.merriweather, defaultBold = true, sampleText = "Journal Title"),

        // Bold Posters & Thumbnails
        StudioFontOption("oswald", "Oswald", "Display", R.font.oswald, sampleText = "THUMBNAIL IMPACT"),
        StudioFontOption("oswald_bold", "Oswald Heavy", "Display", R.font.oswald, defaultBold = true, sampleText = "BREAKING 2026"),
        StudioFontOption("bebas_neue", "Bebas Neue", "Display", R.font.bebas_neue, sampleText = "CINEMA POSTER"),

        // Script & Calligraphy
        StudioFontOption("pacifico", "Pacifico", "Calligraphy", R.font.pacifico, sampleText = "Summer Vibes"),
        StudioFontOption("lobster", "Lobster", "Calligraphy", R.font.lobster, sampleText = "Retro Logo"),
        StudioFontOption("dancing_script", "Dancing Script", "Calligraphy", R.font.dancing_script, sampleText = "Eid Mubarak"),
        StudioFontOption("caveat", "Caveat", "Calligraphy", R.font.caveat, sampleText = "Handwritten Note"),

        // Monospace
        StudioFontOption("jetbrains_mono", "JetBrains Mono", "Mono", R.font.jetbrains_mono, sampleText = "CODE // 4K")
    )

    private val typefaceCache = mutableMapOf<String, Typeface>()

    fun findById(id: String): StudioFontOption =
        fonts.find { it.id == id } ?: fonts.first()

    fun composeFontFamily(option: StudioFontOption): FontFamily =
        FontFamily(Font(option.fontRes))

    fun getBaseTypeface(context: Context, option: StudioFontOption): Typeface {
        val assetKey = "${option.id}_${option.fontRes}"
        return typefaceCache.getOrPut(assetKey) {
            val fromAsset = runCatching {
                Typeface.createFromAsset(context.assets, "fonts/${option.id}.ttf")
            }.getOrNull()
            fromAsset ?: ResourcesCompat.getFont(context, option.fontRes) ?: Typeface.DEFAULT
        }
    }

    fun getTypeface(context: Context, option: StudioFontOption, bold: Boolean, italic: Boolean): Typeface {
        val base = getBaseTypeface(context, option)
        val effectiveBold = bold || option.defaultBold
        val effectiveItalic = italic || option.defaultItalic
        if (!effectiveBold && !effectiveItalic) return base
        val style = when {
            effectiveBold && effectiveItalic -> Typeface.BOLD_ITALIC
            effectiveBold -> Typeface.BOLD
            else -> Typeface.ITALIC
        }
        return runCatching { Typeface.create(base, style) }.getOrDefault(base)
    }
}
