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
        // ==================== 54 BANGLA FONTS ====================
        StudioFontOption("hind_siliguri", "Hind Siliguri", "Bangla", R.font.hind_siliguri),
        StudioFontOption("solaiman_lipi", "SolaimanLipi", "Bangla", R.font.noto_sans_bengali),
        StudioFontOption("noto_serif_bengali", "Noto Serif Bengali", "Bangla", R.font.noto_serif_bengali),
        StudioFontOption("noto_sans_bengali", "Noto Sans Bengali", "Bangla", R.font.noto_sans_bengali),
        StudioFontOption("baloo_da_2", "Baloo Da 2", "Bangla", R.font.baloo_da_2),
        StudioFontOption("tiro_bangla", "Tiro Bangla", "Bangla", R.font.tiro_bangla),
        StudioFontOption("sutonny_mj", "SutonnyMJ", "Bangla", R.font.tiro_bangla, defaultBold = true),
        StudioFontOption("siyam_rupali", "Siyam Rupali", "Bangla", R.font.hind_siliguri),
        StudioFontOption("bensen", "BenSen", "Bangla", R.font.noto_serif_bengali),
        StudioFontOption("charukola", "Charukola", "Bangla", R.font.anek_bangla),
        StudioFontOption("li_ador_noir", "Li Ador Noir", "Bangla", R.font.baloo_da_2, defaultBold = true),
        StudioFontOption("li_alinur", "Li Alinur", "Bangla", R.font.galada),
        StudioFontOption("mina", "Mina", "Bangla", R.font.mina),
        StudioFontOption("atma", "Atma", "Bangla", R.font.atma),
        StudioFontOption("galada_bn", "Galada Bangla", "Bangla", R.font.galada),
        StudioFontOption("anek_bangla", "Anek Bangla", "Bangla", R.font.anek_bangla),
        StudioFontOption("hind_siliguri_bold", "Hind Siliguri Bold", "Bangla", R.font.hind_siliguri, defaultBold = true),
        StudioFontOption("hind_siliguri_italic", "Hind Siliguri Italic", "Bangla", R.font.hind_siliguri, defaultItalic = true),
        StudioFontOption("hind_siliguri_wide", "Hind Siliguri Wide", "Bangla", R.font.hind_siliguri, letterSpacingEm = 0.08f),
        StudioFontOption("noto_serif_bn_bold", "Noto Serif Bengali Bold", "Bangla", R.font.noto_serif_bengali, defaultBold = true),
        StudioFontOption("noto_serif_bn_italic", "Noto Serif Bengali Italic", "Bangla", R.font.noto_serif_bengali, defaultItalic = true),
        StudioFontOption("noto_serif_bn_wide", "Noto Serif Bengali Royal", "Bangla", R.font.noto_serif_bengali, defaultBold = true, letterSpacingEm = 0.06f),
        StudioFontOption("noto_sans_bn_bold", "Noto Sans Bengali Bold", "Bangla", R.font.noto_sans_bengali, defaultBold = true),
        StudioFontOption("noto_sans_bn_wide", "Noto Sans Bengali Wide", "Bangla", R.font.noto_sans_bengali, letterSpacingEm = 0.1f),
        StudioFontOption("baloo_da_bold", "Baloo Da 2 Bold", "Bangla", R.font.baloo_da_2, defaultBold = true),
        StudioFontOption("baloo_da_poster", "Baloo Da Poster", "Bangla", R.font.baloo_da_2, defaultBold = true, letterSpacingEm = 0.05f),
        StudioFontOption("atma_bold", "Atma Display Bold", "Bangla", R.font.atma, defaultBold = true),
        StudioFontOption("atma_playful", "Atma Cartoon", "Bangla", R.font.atma, letterSpacingEm = 0.05f),
        StudioFontOption("mina_bold", "Mina Headline", "Bangla", R.font.mina, defaultBold = true),
        StudioFontOption("mina_italic", "Mina Slanted", "Bangla", R.font.mina, defaultItalic = true),
        StudioFontOption("tiro_bangla_bold", "Tiro Bangla Bold", "Bangla", R.font.tiro_bangla, defaultBold = true),
        StudioFontOption("tiro_bangla_italic", "Tiro Bangla Italic", "Bangla", R.font.tiro_bangla, defaultItalic = true),
        StudioFontOption("anek_bangla_bold", "Anek Bangla Heavy", "Bangla", R.font.anek_bangla, defaultBold = true),
        StudioFontOption("anek_bangla_wide", "Anek Bangla Wide", "Bangla", R.font.anek_bangla, letterSpacingEm = 0.12f),
        StudioFontOption("nikosh", "Nikosh", "Bangla", R.font.tiro_bangla),
        StudioFontOption("nikosh_ban", "NikoshBan", "Bangla", R.font.noto_serif_bengali),
        StudioFontOption("kalpurush", "Kalpurush", "Bangla", R.font.noto_sans_bengali),
        StudioFontOption("kalpurush_bold", "Kalpurush Bold", "Bangla", R.font.noto_sans_bengali, defaultBold = true),
        StudioFontOption("mukti", "Mukti", "Bangla", R.font.mina),
        StudioFontOption("lohit_bengali", "Lohit Bengali", "Bangla", R.font.hind_siliguri),
        StudioFontOption("vrinda", "Vrinda Classic", "Bangla", R.font.tiro_bangla),
        StudioFontOption("shonar_bangla", "Shonar Bangla", "Bangla", R.font.noto_serif_bengali, defaultBold = true),
        StudioFontOption("ekushey_lohit", "Ekushey Lohit", "Bangla", R.font.anek_bangla),
        StudioFontOption("ekushey_pujari", "Ekushey Pujari", "Bangla", R.font.galada),
        StudioFontOption("ekushey_sumit", "Ekushey Sumit", "Bangla", R.font.mina, defaultBold = true),
        StudioFontOption("bensen_handwriting", "BenSen Handwriting", "Bangla", R.font.atma),
        StudioFontOption("rupashi_bangla", "Rupashi Bangla", "Bangla", R.font.baloo_da_2),
        StudioFontOption("adarsha_lipi", "AdarshaLipi", "Bangla", R.font.noto_serif_bengali),
        StudioFontOption("mitra_mono_bn", "Mitra Classic", "Bangla", R.font.mina),
        StudioFontOption("bangla_katha", "Bangla Katha", "Bangla", R.font.galada, defaultBold = true),
        StudioFontOption("shiliguri_poster", "Siliguri Poster Bold", "Bangla", R.font.hind_siliguri, defaultBold = true, letterSpacingEm = 0.04f),
        StudioFontOption("padma_script", "Padma Calligraphy", "Bangla", R.font.galada, defaultItalic = true),
        StudioFontOption("meghna_headline", "Meghna Headline", "Bangla", R.font.anek_bangla, defaultBold = true),
        StudioFontOption("boishakhi_fest", "Boishakhi Festive", "Bangla", R.font.atma, defaultBold = true),

        // ==================== 24 ENGLISH FONTS ====================
        StudioFontOption("poppins", "Poppins", "English", R.font.poppins, sampleText = "আরফিন • Poppins"),
        StudioFontOption("roboto", "Roboto", "English", R.font.roboto, sampleText = "আরফিন • Roboto"),
        StudioFontOption("montserrat", "Montserrat", "English", R.font.montserrat, sampleText = "CREATIVE BRAND"),
        StudioFontOption("inter", "Inter", "English", R.font.inter, sampleText = "Crisp Interface"),
        StudioFontOption("jost", "Jost", "English", R.font.jost, sampleText = "Modern Vector"),
        StudioFontOption("nunito", "Nunito", "English", R.font.nunito, sampleText = "Friendly Rounded"),
        StudioFontOption("raleway", "Raleway", "English", R.font.raleway, sampleText = "Elegant Fashion"),
        StudioFontOption("merriweather", "Merriweather", "English", R.font.merriweather, sampleText = "Editorial Serif"),
        StudioFontOption("poppins_bold", "Poppins Bold", "English", R.font.poppins, defaultBold = true, sampleText = "UI & GRAPHICS"),
        StudioFontOption("poppins_italic", "Poppins Italic", "English", R.font.poppins, defaultItalic = true, sampleText = "Sleek Tagline"),
        StudioFontOption("roboto_bold", "Roboto Bold", "English", R.font.roboto, defaultBold = true, sampleText = "Clean Headline"),
        StudioFontOption("roboto_italic", "Roboto Italic", "English", R.font.roboto, defaultItalic = true, sampleText = "Modern Subtext"),
        StudioFontOption("montserrat_bold", "Montserrat Bold", "English", R.font.montserrat, defaultBold = true, sampleText = "CREATIVE STUDIO"),
        StudioFontOption("montserrat_wide", "Montserrat Tracked", "English", R.font.montserrat, defaultBold = true, letterSpacingEm = 0.15f, sampleText = "LUXURY BRAND"),
        StudioFontOption("inter_bold", "Inter Bold", "English", R.font.inter, defaultBold = true, sampleText = "Product Feature"),
        StudioFontOption("jost_bold", "Jost Bold", "English", R.font.jost, defaultBold = true, sampleText = "Futura Inspired"),
        StudioFontOption("nunito_bold", "Nunito Bold", "English", R.font.nunito, defaultBold = true, sampleText = "Soft & Bold"),
        StudioFontOption("raleway_bold", "Raleway Bold", "English", R.font.raleway, defaultBold = true, sampleText = "Minimal Poster"),
        StudioFontOption("merriweather_bold", "Merriweather Bold", "English", R.font.merriweather, defaultBold = true, sampleText = "Journal Title"),
        StudioFontOption("playfair", "Playfair Display", "English", R.font.playfair_display, sampleText = "Classic Luxury"),
        StudioFontOption("space_grotesk", "Space Grotesk", "English", R.font.space_grotesk, sampleText = "TECH VECTOR"),
        StudioFontOption("oswald", "Oswald", "English", R.font.oswald, sampleText = "THUMBNAIL IMPACT"),
        StudioFontOption("bebas_neue", "Bebas Neue", "English", R.font.bebas_neue, sampleText = "CINEMA POSTER"),
        StudioFontOption("jetbrains_mono", "JetBrains Mono", "English", R.font.jetbrains_mono, sampleText = "CODE // 4K"),

        // ==================== CALLIGRAPHY, DISPLAY & MONO ====================
        StudioFontOption("amiri", "Amiri (Arabic & Islamic)", "Calligraphy", R.font.amiri, sampleText = "আরফিন • بسم الله"),
        StudioFontOption("amiri_bold", "Amiri Bold", "Calligraphy", R.font.amiri, defaultBold = true, sampleText = "আরফিন • محمد"),
        StudioFontOption("galada", "Galada", "Calligraphy", R.font.galada, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("galada_display", "Galada Calligraphy", "Calligraphy", R.font.galada, defaultItalic = true, sampleText = "আরফিন ইলাস্ট্রেটর"),
        StudioFontOption("pacifico", "Pacifico", "Calligraphy", R.font.pacifico, sampleText = "Summer Vibes"),
        StudioFontOption("lobster", "Lobster", "Calligraphy", R.font.lobster, sampleText = "Retro Logo"),
        StudioFontOption("dancing_script", "Dancing Script", "Calligraphy", R.font.dancing_script, sampleText = "Eid Mubarak"),
        StudioFontOption("caveat", "Caveat", "Calligraphy", R.font.caveat, sampleText = "Handwritten Note"),
        StudioFontOption("playfair_bold", "Playfair Bold", "Display", R.font.playfair_display, defaultBold = true, sampleText = "Royal Edition"),
        StudioFontOption("space_grotesk_bold", "Space Grotesk Bold", "Display", R.font.space_grotesk, defaultBold = true, sampleText = "FUTURE LAB"),
        StudioFontOption("oswald_bold", "Oswald Heavy", "Display", R.font.oswald, defaultBold = true, sampleText = "BREAKING 2026"),
        StudioFontOption("jetbrains_mono_bold", "JetBrains Mono Bold", "Mono", R.font.jetbrains_mono, defaultBold = true, sampleText = "VECTOR // SVG")
    )

    fun banglaCount(): Int = fonts.count { it.category == "Bangla" }
    fun englishCount(): Int = fonts.count { it.category == "English" }

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
