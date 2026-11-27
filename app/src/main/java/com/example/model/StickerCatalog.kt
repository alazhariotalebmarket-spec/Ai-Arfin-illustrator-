package com.example.model

enum class StickerCategory(val label: String, val labelBn: String) {
    ALL("All", "সব"),
    SHAPES("Shapes", "শেপ"),
    LINES("Lines", "লাইন"),
    ARROWS("Arrows", "অ্যারো"),
    ICONS("Icons", "আইকন"),
    SOCIAL("Social Media", "সোশ্যাল"),
    FRAMES("Frames & Borders", "ফ্রেম ও বর্ডার"),
    BADGES("Stickers & Badges", "ব্যাজ"),
    ISLAMIC("Islamic", "ইসলামিক"),
    NATURE("Festive", "উৎসব")
}

enum class StickerRenderKind {
    // Islamic & Calligraphy
    BISMILLAH_SEAL, ALLAH_MEDALLION, MUHAMMAD_MEDALLION, EID_MUBARAK_CREST,
    MASHALLAH_BADGE, SUBHANALLAH_CREST, ALHAMDULILLAH_CREST, JUMMA_MUBARAK_CREST,
    RAMADAN_KAREEM_CREST, CRESCENT_STAR, MOSQUE_SILHOUETTE, MIHRAB_ARCH,
    LANTERN_FANOOS, ISLAMIC_8_STAR, ISLAMIC_12_STAR, ARABESQUE_CORNER,
    TASBIH_WREATH, QURAN_STAND, DOME_MINARET, PALM_CRESCENT,

    // Frames & Borders
    FRAME_DOUBLE_GOLD, FRAME_ORNATE_ARCH, FRAME_HEXAGON_TECH, FRAME_CIRCLE_WREATH,
    FRAME_DASHED_TICKET, FRAME_CORNER_BRACKETS, FRAME_RIBBON_HEADER, FRAME_POLAROID,
    FRAME_DIAMOND_ROYAL, FRAME_ROUNDED_NEON, FRAME_BRUSH_BOX, FRAME_STAMP_EDGE,

    // Badges & Ribbons
    BADGE_SEAL_12, BADGE_SEAL_24, BADGE_SHIELD, BADGE_CROWN, BADGE_TROPHY,
    BADGE_VERIFIED, BADGE_NEW_TAG, BADGE_HOT_FLAME, BADGE_OFFER_BURST,
    RIBBON_BANNER, RIBBON_AWARD, RIBBON_BOOKMARK, RIBBON_CORNER_FOLD,
    BADGE_STAR_MEDAL, BADGE_DIAMOND_VIP,

    // Geometric & Stars
    GEO_HEX_GRID, GEO_CONCENTRIC, GEO_SUNBURST, GEO_SPIRAL, GEO_HALFTONE,
    GEO_TRIANGLE_STACK, GEO_WAVE_BARS, GEO_PILL_CLUSTER, GEO_GRID_DOTS,
    GEO_SPARKLE_4, GEO_SPARKLE_GROUP, GEO_CHECKER_ACCENT, GEO_DIAGONAL_STRIPES,
    GEO_ABSTRACT_BLOB, GEO_ZIGZAG, GEO_CROSSHAIR,

    // Lines & Dividers
    LINE_SOLID_ORNATE, LINE_DASHED_RULE, LINE_DOUBLE_BAR, LINE_DIAMOND_DIVIDER,

    // Social Media & UI
    SOC_FACEBOOK, SOC_YOUTUBE, SOC_WHATSAPP, SOC_INSTAGRAM,
    SOC_PLAY_BUTTON, SOC_SUBSCRIBE_PILL, SOC_BELL_RING, SOC_THUMBS_UP,
    SOC_HEART_LIKE, SOC_SHARE_NODES, SOC_COMMENT_BUBBLE, SOC_CAMERA_LENS,
    SOC_MIC_PODCAST, SOC_LOCATION_PIN, SOC_PHONE_CALL, SOC_GLOBE_WEB,
    SOC_AT_HANDLE, SOC_VERIFIED_CHECK, SOC_LIVE_DOT,

    // General Icons
    ICON_CLOCK, ICON_CALENDAR, ICON_STAR, ICON_MAIL,

    // Arrows & Accents
    ARR_RIGHT_BOLD, ARR_CURVED_SWOOSH, ARR_DOUBLE_CHEVRON, ARR_CIRCULAR_LOOP,
    ARR_UP_GROWTH, ARR_DOWN_DOWNLOAD, ARR_DIAGONAL_POINTER, ARR_HAND_UNDERLINE,
    ARR_BRACKET_LEFT, ARR_BRACKET_RIGHT, ARR_SPEECH_POINTER, ARR_LIGHTNING_BOLT,

    // Nature & Festive
    NAT_FLOWER_LOTUS, NAT_LEAF_BRANCH, NAT_SUN_RAYS, NAT_MOON_CLOUDS,
    NAT_FIREWORK, NAT_CONFETTI_BURST, NAT_GIFT_BOX, NAT_HEART_DOUBLE,
    NAT_BUTTERFLY, NAT_FEATHER_QUILL, NAT_MOUNTAIN_PEAK, NAT_OCEAN_WAVE,
    NAT_MUSIC_NOTE, NAT_PEN_NIB, NAT_PALETTE_ART, NAT_SPARK_CROWN,

    // Bangladesh & Commercial Badges
    BD_FLAG_CREST, BD_SHAPLA_FLOWER, BD_SEAL_TEXT
}

data class StickerAsset(
    val id: String,
    val title: String,
    val category: StickerCategory,
    val kind: StickerRenderKind,
    val arabicOrLabel: String = "",
    val defaultPrimaryColor: Long = 0xFFFF7A00L,
    val defaultSecondaryColor: Long = 0xFF1E1E1EL
)

object StickerCatalog {
    val items: List<StickerAsset> = listOf(
        // ==================== SOCIAL MEDIA ICONS (Facebook, YouTube, WhatsApp, Phone, Location) ====================
        StickerAsset("soc_facebook", "Facebook", StickerCategory.SOCIAL, StickerRenderKind.SOC_FACEBOOK, "f", 0xFF1877F2L, 0xFFFFFFFFL),
        StickerAsset("soc_youtube", "YouTube", StickerCategory.SOCIAL, StickerRenderKind.SOC_YOUTUBE, "", 0xFFFF0000L, 0xFFFFFFFFL),
        StickerAsset("soc_whatsapp", "WhatsApp", StickerCategory.SOCIAL, StickerRenderKind.SOC_WHATSAPP, "✆", 0xFF25D366L, 0xFFFFFFFFL),
        StickerAsset("soc_phone", "Phone Call", StickerCategory.SOCIAL, StickerRenderKind.SOC_PHONE_CALL, "☎", 0xFF16A34AL, 0xFFFFFFFFL),
        StickerAsset("soc_loc", "Location Pin", StickerCategory.SOCIAL, StickerRenderKind.SOC_LOCATION_PIN, "", 0xFFEF4444L, 0xFFFFFFFFL),
        StickerAsset("soc_instagram", "Instagram", StickerCategory.SOCIAL, StickerRenderKind.SOC_INSTAGRAM, "", 0xFFE1306CL, 0xFFFFFFFFL),
        StickerAsset("soc_play", "YouTube Play Badge", StickerCategory.SOCIAL, StickerRenderKind.SOC_PLAY_BUTTON, "", 0xFFFF0000L, 0xFFFFFFFFL),
        StickerAsset("soc_sub", "Subscribe Pill", StickerCategory.SOCIAL, StickerRenderKind.SOC_SUBSCRIBE_PILL, "SUBSCRIBE", 0xFFFF0000L, 0xFFFFFFFFL),
        StickerAsset("soc_bell", "Notification Bell", StickerCategory.SOCIAL, StickerRenderKind.SOC_BELL_RING, "", 0xFFF59E0BL, 0xFF1E1E1EL),
        StickerAsset("soc_like", "Thumbs Up", StickerCategory.SOCIAL, StickerRenderKind.SOC_THUMBS_UP, "", 0xFF2563EBL, 0xFFFFFFFFL),
        StickerAsset("soc_heart", "Love Reaction", StickerCategory.SOCIAL, StickerRenderKind.SOC_HEART_LIKE, "", 0xFFEF4444L, 0xFFFFFFFFL),
        StickerAsset("soc_share", "Share Icon", StickerCategory.SOCIAL, StickerRenderKind.SOC_SHARE_NODES, "", 0xFF10B981L, 0xFF1E1E1EL),
        StickerAsset("soc_comment", "Comment Bubble", StickerCategory.SOCIAL, StickerRenderKind.SOC_COMMENT_BUBBLE, "", 0xFF3B82F6L, 0xFFFFFFFFL),
        StickerAsset("soc_globe", "Website Globe", StickerCategory.SOCIAL, StickerRenderKind.SOC_GLOBE_WEB, "", 0xFF0284C7L, 0xFF1E1E1EL),
        StickerAsset("soc_at", "Social @ Handle", StickerCategory.SOCIAL, StickerRenderKind.SOC_AT_HANDLE, "@", 0xFFFF7A00L, 0xFFFFFFFFL),
        StickerAsset("soc_check", "Verified Check", StickerCategory.SOCIAL, StickerRenderKind.SOC_VERIFIED_CHECK, "", 0xFF22C55EL, 0xFFFFFFFFL),
        StickerAsset("soc_live", "LIVE Badge", StickerCategory.SOCIAL, StickerRenderKind.SOC_LIVE_DOT, "LIVE", 0xFFDC2626L, 0xFFFFFFFFL),

        // ==================== LINES & DIVIDERS ====================
        StickerAsset("line_ornate", "Ornate Gold Divider", StickerCategory.LINES, StickerRenderKind.LINE_SOLID_ORNATE, "", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("line_dashed", "Dashed Divider Line", StickerCategory.LINES, StickerRenderKind.LINE_DASHED_RULE, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("line_double", "Double Bar Divider", StickerCategory.LINES, StickerRenderKind.LINE_DOUBLE_BAR, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("line_diamond", "Diamond Accent Line", StickerCategory.LINES, StickerRenderKind.LINE_DIAMOND_DIVIDER, "", 0xFFD4AF37L, 0xFF0F5132L),

        // ==================== UI & DESIGN ICONS ====================
        StickerAsset("ico_clock", "Time / Clock Icon", StickerCategory.ICONS, StickerRenderKind.ICON_CLOCK, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("ico_calendar", "Date / Calendar Icon", StickerCategory.ICONS, StickerRenderKind.ICON_CALENDAR, "", 0xFF2563EBL, 0xFFFFFFFFL),
        StickerAsset("ico_star", "Featured Star Icon", StickerCategory.ICONS, StickerRenderKind.ICON_STAR, "", 0xFFF59E0BL, 0xFF1E1E1EL),
        StickerAsset("ico_mail", "Email Envelope Icon", StickerCategory.ICONS, StickerRenderKind.ICON_MAIL, "", 0xFF0D9488L, 0xFFFFFFFFL),
        StickerAsset("ico_camera", "Camera Lens", StickerCategory.ICONS, StickerRenderKind.SOC_CAMERA_LENS, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("ico_mic", "Podcast Mic", StickerCategory.ICONS, StickerRenderKind.SOC_MIC_PODCAST, "", 0xFF1E1E1EL, 0xFFFF7A00L),

        // 20 Islamic & Calligraphy Elements
        StickerAsset("isl_bismillah", "Bismillah Seal", StickerCategory.ISLAMIC, StickerRenderKind.BISMILLAH_SEAL, "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("isl_allah", "Allah Calligraphy", StickerCategory.ISLAMIC, StickerRenderKind.ALLAH_MEDALLION, "الله", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("isl_muhammad", "Muhammad Crest", StickerCategory.ISLAMIC, StickerRenderKind.MUHAMMAD_MEDALLION, "مُحَمَّد ﷺ", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("isl_eid", "Eid Mubarak Crest", StickerCategory.ISLAMIC, StickerRenderKind.EID_MUBARAK_CREST, "عيد مبارك", 0xFFD4AF37L, 0xFF14362AL),
        StickerAsset("isl_mashallah", "MashaAllah Badge", StickerCategory.ISLAMIC, StickerRenderKind.MASHALLAH_BADGE, "مَا شَاءَ اللهُ", 0xFF0F5132L, 0xFFD4AF37L),
        StickerAsset("isl_subhanallah", "SubhanAllah", StickerCategory.ISLAMIC, StickerRenderKind.SUBHANALLAH_CREST, "سُبْحَانَ اللهِ", 0xFF1B4332L, 0xFFD4AF37L),
        StickerAsset("isl_alhamdulillah", "Alhamdulillah", StickerCategory.ISLAMIC, StickerRenderKind.ALHAMDULILLAH_CREST, "اَلْحَمْدُ لِلّٰهِ", 0xFF1B4332L, 0xFFD4AF37L),
        StickerAsset("isl_jumma", "Jumma Mubarak", StickerCategory.ISLAMIC, StickerRenderKind.JUMMA_MUBARAK_CREST, "جُمُعَة مُبَارَكَة", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("isl_ramadan", "Ramadan Kareem", StickerCategory.ISLAMIC, StickerRenderKind.RAMADAN_KAREEM_CREST, "رَمَضَان كَرِيم", 0xFFD4AF37L, 0xFF1A237EL),
        StickerAsset("isl_crescent", "Crescent & Star", StickerCategory.ISLAMIC, StickerRenderKind.CRESCENT_STAR, "", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("isl_mosque", "Mosque Silhouette", StickerCategory.ISLAMIC, StickerRenderKind.MOSQUE_SILHOUETTE, "", 0xFF0F3D2EL, 0xFFD4AF37L),
        StickerAsset("isl_mihrab", "Mihrab Arch", StickerCategory.ISLAMIC, StickerRenderKind.MIHRAB_ARCH, "", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("isl_lantern", "Islamic Lantern", StickerCategory.ISLAMIC, StickerRenderKind.LANTERN_FANOOS, "", 0xFFD4AF37L, 0xFFFF7A00L),
        StickerAsset("isl_8star", "Rub el Hizb 8-Star", StickerCategory.ISLAMIC, StickerRenderKind.ISLAMIC_8_STAR, "", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("isl_12star", "12-Point Mandala", StickerCategory.ISLAMIC, StickerRenderKind.ISLAMIC_12_STAR, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("isl_arabesque", "Arabesque Corner", StickerCategory.ISLAMIC, StickerRenderKind.ARABESQUE_CORNER, "", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("isl_tasbih", "Islamic Wreath", StickerCategory.ISLAMIC, StickerRenderKind.TASBIH_WREATH, "", 0xFF1B4332L, 0xFFD4AF37L),
        StickerAsset("isl_rehal", "Quran Rehal Stand", StickerCategory.ISLAMIC, StickerRenderKind.QURAN_STAND, "", 0xFF8B5E3CL, 0xFFD4AF37L),
        StickerAsset("isl_dome", "Dome & Minaret", StickerCategory.ISLAMIC, StickerRenderKind.DOME_MINARET, "", 0xFF0F3D2EL, 0xFFD4AF37L),
        StickerAsset("isl_palm", "Palm & Crescent", StickerCategory.ISLAMIC, StickerRenderKind.PALM_CRESCENT, "", 0xFF1B4332L, 0xFFD4AF37L),

        // 16 Frames & Borders
        StickerAsset("frm_double_gold", "Royal Double Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_DOUBLE_GOLD, "", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("frm_ornate_arch", "Ornate Arch Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_ORNATE_ARCH, "", 0xFFD4AF37L, 0xFF0F3D2EL),
        StickerAsset("frm_hex_tech", "Hexagon Tech Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_HEXAGON_TECH, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("frm_circle_wreath", "Laurel Circle Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_CIRCLE_WREATH, "", 0xFFD4AF37L, 0xFF1B4332L),
        StickerAsset("frm_ticket", "Dashed Ticket Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_DASHED_TICKET, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("frm_corners", "Focus Corner Brackets", StickerCategory.FRAMES, StickerRenderKind.FRAME_CORNER_BRACKETS, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("frm_ribbon_top", "Header Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_RIBBON_HEADER, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("frm_polaroid", "Photo Card Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_POLAROID, "", 0xFFFFFFFFL, 0xFF252527L),
        StickerAsset("frm_diamond", "Diamond Crest Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_DIAMOND_ROYAL, "", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("frm_neon", "Rounded Neon Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_ROUNDED_NEON, "", 0xFFFF7A00L, 0xFF00E5FFL),
        StickerAsset("frm_brush", "Artistic Box Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_BRUSH_BOX, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("frm_stamp", "Postage Stamp Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_STAMP_EDGE, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("frm_gold_square", "Minimal Gold Border", StickerCategory.FRAMES, StickerRenderKind.FRAME_DOUBLE_GOLD, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("frm_emerald_arch", "Madrasa Arch Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_ORNATE_ARCH, "", 0xFF0F5132L, 0xFFD4AF37L),
        StickerAsset("frm_navy_crest", "Certificate Border", StickerCategory.FRAMES, StickerRenderKind.FRAME_DIAMOND_ROYAL, "", 0xFF1E3A8AL, 0xFFD4AF37L),
        StickerAsset("frm_crimson_tag", "Promo Box Frame", StickerCategory.FRAMES, StickerRenderKind.FRAME_DASHED_TICKET, "", 0xFFDC2626L, 0xFFFACC15L),

        // 16 Badges & Ribbons
        StickerAsset("bdg_seal12", "12-Point Seal", StickerCategory.BADGES, StickerRenderKind.BADGE_SEAL_12, "", 0xFFFF7A00L, 0xFFFFFFFFL),
        StickerAsset("bdg_seal24", "24-Point Starburst", StickerCategory.BADGES, StickerRenderKind.BADGE_SEAL_24, "", 0xFFDC2626L, 0xFFFACC15L),
        StickerAsset("bdg_shield", "Emblem Shield", StickerCategory.BADGES, StickerRenderKind.BADGE_SHIELD, "", 0xFF1E3A8AL, 0xFFD4AF37L),
        StickerAsset("bdg_crown", "Royal Crown", StickerCategory.BADGES, StickerRenderKind.BADGE_CROWN, "", 0xFFD4AF37L, 0xFF1E1E1EL),
        StickerAsset("bdg_trophy", "Champion Trophy", StickerCategory.BADGES, StickerRenderKind.BADGE_TROPHY, "", 0xFFF59E0BL, 0xFF1E1E1EL),
        StickerAsset("bdg_verified", "Verified Seal", StickerCategory.BADGES, StickerRenderKind.BADGE_VERIFIED, "", 0xFF2563EBL, 0xFFFFFFFFL),
        StickerAsset("bdg_new", "NEW Tag", StickerCategory.BADGES, StickerRenderKind.BADGE_NEW_TAG, "NEW", 0xFFFF7A00L, 0xFFFFFFFFL),
        StickerAsset("bdg_hot", "Trending Flame", StickerCategory.BADGES, StickerRenderKind.BADGE_HOT_FLAME, "", 0xFFEF4444L, 0xFFFACC15L),
        StickerAsset("bdg_offer", "Special Offer Burst", StickerCategory.BADGES, StickerRenderKind.BADGE_OFFER_BURST, "50%", 0xFFDC2626L, 0xFFFFFFFFL),
        StickerAsset("bdg_ribbon", "Title Ribbon", StickerCategory.BADGES, StickerRenderKind.RIBBON_BANNER, "", 0xFFFF7A00L, 0xFFCC4400L),
        StickerAsset("bdg_award", "Award Rosette", StickerCategory.BADGES, StickerRenderKind.RIBBON_AWARD, "", 0xFFD4AF37L, 0xFF991B1BL),
        StickerAsset("bdg_bookmark", "Bookmark Tag", StickerCategory.BADGES, StickerRenderKind.RIBBON_BOOKMARK, "", 0xFF0F5132L, 0xFFD4AF37L),
        StickerAsset("bdg_corner", "Corner Banner", StickerCategory.BADGES, StickerRenderKind.RIBBON_CORNER_FOLD, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("bdg_medal", "Star Medal", StickerCategory.BADGES, StickerRenderKind.BADGE_STAR_MEDAL, "", 0xFFD4AF37L, 0xFF1E3A8AL),
        StickerAsset("bdg_vip", "VIP Diamond", StickerCategory.BADGES, StickerRenderKind.BADGE_DIAMOND_VIP, "VIP", 0xFF1E1E1EL, 0xFFD4AF37L),
        StickerAsset("bdg_admission", "Admission Ribbon", StickerCategory.BADGES, StickerRenderKind.RIBBON_BANNER, "ভর্তি চলছে", 0xFF0F5132L, 0xFFD4AF37L),

        // 18 Geometric & Abstract Shapes
        StickerAsset("geo_hex", "Hexagon Cluster", StickerCategory.SHAPES, StickerRenderKind.GEO_HEX_GRID, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("geo_rings", "Concentric Rings", StickerCategory.SHAPES, StickerRenderKind.GEO_CONCENTRIC, "", 0xFFFF7A00L, 0xFF2563EBL),
        StickerAsset("geo_sunburst", "Sunburst Rays", StickerCategory.SHAPES, StickerRenderKind.GEO_SUNBURST, "", 0xFFF59E0BL, 0xFFFF7A00L),
        StickerAsset("geo_spiral", "Vector Spiral", StickerCategory.SHAPES, StickerRenderKind.GEO_SPIRAL, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("geo_halftone", "Halftone Dots", StickerCategory.SHAPES, StickerRenderKind.GEO_HALFTONE, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("geo_triangles", "Triangle Stack", StickerCategory.SHAPES, StickerRenderKind.GEO_TRIANGLE_STACK, "", 0xFF2563EBL, 0xFF60A5FAL),
        StickerAsset("geo_waves", "Audio Wave Bars", StickerCategory.SHAPES, StickerRenderKind.GEO_WAVE_BARS, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("geo_pills", "Modern Pill Shapes", StickerCategory.SHAPES, StickerRenderKind.GEO_PILL_CLUSTER, "", 0xFFFF7A00L, 0xFF10B981L),
        StickerAsset("geo_dots", "Dot Matrix 4x4", StickerCategory.SHAPES, StickerRenderKind.GEO_GRID_DOTS, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("geo_spark4", "4-Point Sparkle", StickerCategory.SHAPES, StickerRenderKind.GEO_SPARKLE_4, "", 0xFFFACC15L, 0xFFFF7A00L),
        StickerAsset("geo_sparks", "Sparkle Cluster", StickerCategory.SHAPES, StickerRenderKind.GEO_SPARKLE_GROUP, "", 0xFFD4AF37L, 0xFFFF7A00L),
        StickerAsset("geo_checker", "Checker Strip", StickerCategory.SHAPES, StickerRenderKind.GEO_CHECKER_ACCENT, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("geo_stripes", "Diagonal Stripes", StickerCategory.SHAPES, StickerRenderKind.GEO_DIAGONAL_STRIPES, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("geo_blob", "Organic Blob", StickerCategory.SHAPES, StickerRenderKind.GEO_ABSTRACT_BLOB, "", 0xFFFF7A00L, 0xFFFFB74DL),
        StickerAsset("geo_zigzag", "Zigzag Accent", StickerCategory.SHAPES, StickerRenderKind.GEO_ZIGZAG, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("geo_cross", "Target Crosshair", StickerCategory.SHAPES, StickerRenderKind.GEO_CROSSHAIR, "", 0xFFEF4444L, 0xFF1E1E1EL),
        StickerAsset("geo_teal_blob", "Teal Fluid Shape", StickerCategory.SHAPES, StickerRenderKind.GEO_ABSTRACT_BLOB, "", 0xFF0D9488L, 0xFF5EEAD4L),
        StickerAsset("geo_gold_rings", "Gold Orbit Rings", StickerCategory.SHAPES, StickerRenderKind.GEO_CONCENTRIC, "", 0xFFD4AF37L, 0xFF1E1E1EL),

        // 12 Arrows & Pointers
        StickerAsset("arr_right", "Bold Right Arrow", StickerCategory.ARROWS, StickerRenderKind.ARR_RIGHT_BOLD, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("arr_swoosh", "Curved Swoosh Arrow", StickerCategory.ARROWS, StickerRenderKind.ARR_CURVED_SWOOSH, "", 0xFFEF4444L, 0xFF1E1E1EL),
        StickerAsset("arr_chevron", "Double Chevron", StickerCategory.ARROWS, StickerRenderKind.ARR_DOUBLE_CHEVRON, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("arr_loop", "Cycle Loop", StickerCategory.ARROWS, StickerRenderKind.ARR_CIRCULAR_LOOP, "", 0xFF10B981L, 0xFF1E1E1EL),
        StickerAsset("arr_up", "Growth Arrow Up", StickerCategory.ARROWS, StickerRenderKind.ARR_UP_GROWTH, "", 0xFF16A34AL, 0xFF1E1E1EL),
        StickerAsset("arr_down", "Download Pointer", StickerCategory.ARROWS, StickerRenderKind.ARR_DOWN_DOWNLOAD, "", 0xFF2563EBL, 0xFF1E1E1EL),
        StickerAsset("arr_diag", "Diagonal Pointer", StickerCategory.ARROWS, StickerRenderKind.ARR_DIAGONAL_POINTER, "", 0xFFF59E0BL, 0xFF1E1E1EL),
        StickerAsset("arr_underline", "Brush Underline", StickerCategory.ARROWS, StickerRenderKind.ARR_HAND_UNDERLINE, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("arr_bleft", "Quote Bracket L", StickerCategory.ARROWS, StickerRenderKind.ARR_BRACKET_LEFT, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("arr_bright", "Quote Bracket R", StickerCategory.ARROWS, StickerRenderKind.ARR_BRACKET_RIGHT, "", 0xFF1E1E1EL, 0xFFFF7A00L),
        StickerAsset("arr_speech", "Callout Pointer", StickerCategory.ARROWS, StickerRenderKind.ARR_SPEECH_POINTER, "", 0xFFFF7A00L, 0xFFFFFFFFL),
        StickerAsset("arr_bolt", "Lightning Flash", StickerCategory.ARROWS, StickerRenderKind.ARR_LIGHTNING_BOLT, "", 0xFFFACC15L, 0xFF1E1E1EL),

        // 16 Nature & Festive
        StickerAsset("nat_lotus", "Lotus Flower", StickerCategory.NATURE, StickerRenderKind.NAT_FLOWER_LOTUS, "", 0xFFEC4899L, 0xFFFCE7F3L),
        StickerAsset("nat_leaf", "Olive Leaf Branch", StickerCategory.NATURE, StickerRenderKind.NAT_LEAF_BRANCH, "", 0xFF15803DL, 0xFF86EFACL),
        StickerAsset("nat_sun", "Bright Sun", StickerCategory.NATURE, StickerRenderKind.NAT_SUN_RAYS, "", 0xFFF59E0BL, 0xFFFDE047L),
        StickerAsset("nat_moon", "Night Moon & Cloud", StickerCategory.NATURE, StickerRenderKind.NAT_MOON_CLOUDS, "", 0xFFFACC15L, 0xFF64748BL),
        StickerAsset("nat_firework", "Festive Firework", StickerCategory.NATURE, StickerRenderKind.NAT_FIREWORK, "", 0xFFFF7A00L, 0xFFFACC15L),
        StickerAsset("nat_confetti", "Celebration Burst", StickerCategory.NATURE, StickerRenderKind.NAT_CONFETTI_BURST, "", 0xFFEC4899L, 0xFF3B82F6L),
        StickerAsset("nat_gift", "Gift Box", StickerCategory.NATURE, StickerRenderKind.NAT_GIFT_BOX, "", 0xFFDC2626L, 0xFFFACC15L),
        StickerAsset("nat_hearts", "Twin Hearts", StickerCategory.NATURE, StickerRenderKind.NAT_HEART_DOUBLE, "", 0xFFE11D48L, 0xFFFDA4AFL),
        StickerAsset("nat_butterfly", "Vector Butterfly", StickerCategory.NATURE, StickerRenderKind.NAT_BUTTERFLY, "", 0xFF8B5CF6L, 0xFFC4B5FDL),
        StickerAsset("nat_quill", "Calligraphy Quill", StickerCategory.NATURE, StickerRenderKind.NAT_FEATHER_QUILL, "", 0xFF1E1E1EL, 0xFFD4AF37L),
        StickerAsset("nat_mountain", "Mountain Peak", StickerCategory.NATURE, StickerRenderKind.NAT_MOUNTAIN_PEAK, "", 0xFF0F766EL, 0xFF99F6E4L),
        StickerAsset("nat_wave", "Ocean Wave", StickerCategory.NATURE, StickerRenderKind.NAT_OCEAN_WAVE, "", 0xFF0284C7L, 0xFFBAE6FDL),
        StickerAsset("nat_music", "Music Crest", StickerCategory.NATURE, StickerRenderKind.NAT_MUSIC_NOTE, "", 0xFF7C3AEDL, 0xFFDDD6FEL),
        StickerAsset("nat_pennib", "Vector Pen Nib", StickerCategory.NATURE, StickerRenderKind.NAT_PEN_NIB, "", 0xFFFF7A00L, 0xFF1E1E1EL),
        StickerAsset("nat_palette", "Artist Palette", StickerCategory.NATURE, StickerRenderKind.NAT_PALETTE_ART, "", 0xFFFF7A00L, 0xFF3B82F6L),
        StickerAsset("nat_crown_spark", "Celebration Crown", StickerCategory.NATURE, StickerRenderKind.NAT_SPARK_CROWN, "", 0xFFD4AF37L, 0xFFFF7A00L),

        // 8 Bangladesh & Commercial Badges
        StickerAsset("bd_flag", "Bangladesh Crest", StickerCategory.BADGES, StickerRenderKind.BD_FLAG_CREST, "", 0xFF006A4EL, 0xFFF42A41L),
        StickerAsset("bd_shapla", "Shapla (শাপলা ফুল)", StickerCategory.NATURE, StickerRenderKind.BD_SHAPLA_FLOWER, "", 0xFFFFFFFFL, 0xFFD4AF37L),
        StickerAsset("bd_pure", "১০০% খাঁটি সিল", StickerCategory.BADGES, StickerRenderKind.BD_SEAL_TEXT, "১০০% খাঁটি", 0xFF0F5132L, 0xFFFFFFFFL),
        StickerAsset("bd_discount", "বিশেষ ছাড় ব্যাজ", StickerCategory.BADGES, StickerRenderKind.BD_SEAL_TEXT, "বিশেষ ছাড়", 0xFFDC2626L, 0xFFFFFFFFL),
        StickerAsset("bd_halal", "হালাল সিল (Halal)", StickerCategory.ISLAMIC, StickerRenderKind.BD_SEAL_TEXT, "হালাল", 0xFF065F46L, 0xFFD4AF37L),
        StickerAsset("bd_delivery", "হোম ডেলিভারি", StickerCategory.BADGES, StickerRenderKind.BD_SEAL_TEXT, "হোম ডেলিভারি", 0xFFEA580CL, 0xFFFFFFFFL),
        StickerAsset("bd_eid_seal", "ঈদ মোবারক ব্যাজ", StickerCategory.ISLAMIC, StickerRenderKind.BD_SEAL_TEXT, "ঈদ মোবারক", 0xFF1E3A8AL, 0xFFD4AF37L),
        StickerAsset("bd_honor", "শ্রেষ্ঠ সম্মাননা", StickerCategory.BADGES, StickerRenderKind.BD_SEAL_TEXT, "সম্মাননা", 0xFFD4AF37L, 0xFF1E1E1EL)
    )

    fun findById(id: String): StickerAsset =
        items.find { it.id == id } ?: items.first()
}
