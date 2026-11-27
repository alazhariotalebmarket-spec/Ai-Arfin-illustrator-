package com.example.model

import java.util.UUID

internal object TemplateCatalogPart1 {
    val templates: List<DesignTemplate> = listOf(
        // 1. মাদ্রাসা ভর্তি বিজ্ঞপ্তি
        DesignTemplate(
            id = "tpl_madrasa_admission",
            title = "Madrasa Admission Poster",
            titleBn = "মাদ্রাসা ভর্তি বিজ্ঞপ্তি",
            category = "মাদ্রাসা",
            sizeLabel = "Story 1080×1350",
            subtitle = "Green + White • Mosque • ভর্তি চলছে",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "মাদ্রাসা ভর্তি বিজ্ঞপ্তি",
                    category = "মাদ্রাসা",
                    artboardWidth = 1080,
                    artboardHeight = 1350,
                    backgroundColor = 0xFF0B4D36L,
                    backgroundGradientEnd = 0xFF062C1EL,
                    layers = listOf(
                        ShapeLayer(name = "White Card", x = 50f, y = 260f, width = 980f, height = 1030f, fillColor = 0xFFFFFFFFL, cornerRadius = 36f),
                        StickerLayer(name = "Mosque Icon", x = 400f, y = 50f, width = 280f, height = 190f, stickerId = "isl_mosque", primaryColor = 0xFFFFFFFFL, secondaryColor = 0xFF0B4D36L),
                        ShapeLayer(name = "Admission Pill", x = 220f, y = 300f, width = 640f, height = 130f, fillColor = 0xFF0B4D36L, cornerRadius = 65f),
                        TextLayer(name = "Headline", x = 220f, y = 320f, width = 640f, height = 100f, text = "ভর্তি চলছে", fontId = "baloo_da_bold", fontSize = 72f, color = 0xFFFFFFFFL, bold = true),
                        TextLayer(name = "Madrasa Name", x = 90f, y = 465f, width = 900f, height = 130f, text = "দারুল উলুম আদর্শ মাদ্রাসা", fontId = "noto_serif_bn_bold", fontSize = 62f, color = 0xFF0B4D36L, bold = true),
                        TextLayer(name = "Details", x = 90f, y = 620f, width = 900f, height = 340f, text = "বিভাগ: নূরানী • নাজেরা • হিফজ ও কিতাব\nতারিখ: ১লা জানুয়ারি থেকে ভর্তি শুরু\nস্থান: ঢাকা রোড, কেন্দ্রীয় মসজিদ সংলগ্ন", fontId = "hind_siliguri", fontSize = 42f, color = 0xFF1E1E1EL),
                        ShapeLayer(name = "Contact Strip", x = 100f, y = 1080f, width = 880f, height = 120f, fillColor = 0xFF0B4D36L, cornerRadius = 24f),
                        TextLayer(name = "Mobile Number", x = 120f, y = 1105f, width = 840f, height = 80f, text = "যোগাযোগ: +880 1934-411199", fontId = "hind_siliguri_bold", fontSize = 42f, color = 0xFFFFD54FL)
                    )
                )
            }
        ),
        // 2. মাহফিল পোস্টার
        DesignTemplate(
            id = "tpl_mahfil_poster",
            title = "Waz Mahfil Poster",
            titleBn = "মাহফিল পোস্টার",
            category = "ইসলামিক",
            sizeLabel = "Poster 1080×1350",
            subtitle = "Dark Blue + Golden • Speaker, Date & Place",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "মাহফিল পোস্টার",
                    category = "ইসলামিক",
                    artboardWidth = 1080,
                    artboardHeight = 1350,
                    backgroundColor = 0xFF0D1B3EL,
                    backgroundGradientEnd = 0xFF060C1DL,
                    layers = listOf(
                        StickerLayer(name = "Gold Frame", x = 30f, y = 30f, width = 1020f, height = 1290f, stickerId = "frm_double_gold", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0D1B3EL),
                        StickerLayer(name = "Islamic Pattern", x = 360f, y = 70f, width = 360f, height = 120f, stickerId = "isl_bismillah", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0D1B3EL),
                        TextLayer(name = "Mahfil Title", x = 80f, y = 220f, width = 920f, height = 200f, text = "বিশাল ওয়াজ ও\nদোয়ার মাহফিল", fontId = "noto_serif_bn_bold", fontSize = 80f, color = 0xFFFFD54FL, shadowEnabled = true),
                        ShapeLayer(name = "Speaker Box", x = 100f, y = 470f, width = 880f, height = 430f, fillColor = 0xFF162857L, strokeColor = 0xFFD4AF37L, strokeWidth = 4f, cornerRadius = 28f),
                        TextLayer(name = "Speaker Info", x = 130f, y = 510f, width = 820f, height = 360f, text = "প্রধান বক্তা: মাওলানা আরফিন ইসলাম\nতারিখ: আগামী শুক্রবার, বাদ আসর\nস্থান: কেন্দ্রীয় ঈদগাহ ময়দান\nমোবাইল: +880 1700-000000", fontId = "hind_siliguri", fontSize = 44f, color = 0xFFFFFFFFL),
                        StickerLayer(name = "Star Ornament", x = 460f, y = 940f, width = 160f, height = 160f, stickerId = "isl_8star", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0D1B3EL),
                        TextLayer(name = "Invite Note", x = 100f, y = 1140f, width = 880f, height = 100f, text = "সকল মুসলিম ভাই-বোনদের আমন্ত্রণ", fontId = "baloo_da_bold", fontSize = 42f, color = 0xFFFDE047L)
                    )
                )
            }
        ),
        // 3. ঈদ মোবারক কার্ড
        DesignTemplate(
            id = "tpl_eid_mubarak",
            title = "Eid Mubarak Card",
            titleBn = "ঈদ মোবারক কার্ড",
            category = "উৎসব",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Golden + Green • Chand Tara • ঈদ মোবারক",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "ঈদ মোবারক কার্ড",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF0A3828L,
                    backgroundGradientEnd = 0xFF041C14L,
                    layers = listOf(
                        StickerLayer(name = "Gold Frame", x = 36f, y = 36f, width = 1008f, height = 1008f, stickerId = "frm_double_gold", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0A3828L),
                        StickerLayer(name = "Chand Tara", x = 390f, y = 110f, width = 300f, height = 260f, stickerId = "isl_crescent", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0A3828L),
                        StickerLayer(name = "Calligraphy", x = 330f, y = 385f, width = 420f, height = 135f, stickerId = "isl_eid", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0A3828L),
                        TextLayer(name = "Eid Bangla", x = 80f, y = 550f, width = 920f, height = 140f, text = "ঈদ মোবারক", fontId = "galada_display", fontSize = 98f, color = 0xFFFFFFFFL, shadowEnabled = true),
                        TextLayer(name = "Eid Message", x = 100f, y = 720f, width = 880f, height = 170f, text = "পবিত্র ঈদের আনন্দ ছড়িয়ে পড়ুক সবার মাঝে\nশুভেচ্ছান্তে: আপনার নাম লিখুন", fontId = "hind_siliguri", fontSize = 38f, color = 0xFFFFD54FL)
                    )
                )
            }
        ),
        // 4. জুম্মা মোবারক পোস্ট
        DesignTemplate(
            id = "tpl_jumma_mubarak",
            title = "Jummah Mubarak Post",
            titleBn = "জুম্মা মোবারক পোস্ট",
            category = "ইসলামিক",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Green Theme • Mosque • Hadith Text",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "জুম্মা মোবারক পোস্ট",
                    category = "ইসলামিক",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF0F4C34L,
                    backgroundGradientEnd = 0xFF07291BL,
                    layers = listOf(
                        StickerLayer(name = "Mihrab Arch", x = 60f, y = 50f, width = 960f, height = 980f, stickerId = "isl_mihrab", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF0F4C34L),
                        StickerLayer(name = "Mosque", x = 380f, y = 140f, width = 320f, height = 220f, stickerId = "isl_mosque", primaryColor = 0xFFFFFFFFL, secondaryColor = 0xFF0F4C34L),
                        TextLayer(name = "Jumma Title", x = 120f, y = 390f, width = 840f, height = 130f, text = "জুম্মা মোবারক", fontId = "noto_serif_bn_bold", fontSize = 84f, color = 0xFFFFD54FL),
                        TextLayer(name = "Hadith Text", x = 150f, y = 540f, width = 780f, height = 260f, text = "“সপ্তাহের শ্রেষ্ঠ দিন হলো জুম্মার দিন।\nবেশি বেশি দরুদ পাঠ করুন ও দোয়া করুন।”", fontId = "hind_siliguri", fontSize = 42f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Footer", x = 180f, y = 840f, width = 720f, height = 80f, text = "তারিখ ও আপনার পেজের নাম", fontId = "hind_siliguri_bold", fontSize = 32f, color = 0xFFA7F3D0L)
                    )
                )
            }
        ),
        // 5. বিবাহের কার্ড
        DesignTemplate(
            id = "tpl_biye_card",
            title = "Biye Card",
            titleBn = "বিবাহের কার্ড",
            category = "উৎসব",
            sizeLabel = "Story 1080×1920",
            subtitle = "Pink + Golden • Flower Design • শুভ বিবাহ",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "বিবাহের কার্ড",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1920,
                    backgroundColor = 0xFFFFF0F5L,
                    backgroundGradientEnd = 0xFFFCE7F3L,
                    layers = listOf(
                        StickerLayer(name = "Gold Crest Frame", x = 45f, y = 60f, width = 990f, height = 1800f, stickerId = "frm_double_gold", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFFFFF0F5L),
                        StickerLayer(name = "Lotus Top", x = 400f, y = 140f, width = 280f, height = 240f, stickerId = "nat_lotus", primaryColor = 0xFFDB2777L, secondaryColor = 0xFFFBCFE8L),
                        TextLayer(name = "Title", x = 100f, y = 430f, width = 880f, height = 140f, text = "শুভ বিবাহ", fontId = "galada_display", fontSize = 96f, color = 0xFF9D174DL),
                        TextLayer(name = "Couple Names", x = 100f, y = 640f, width = 880f, height = 220f, text = "বর: মোঃ ... ইসলাম\nও\nকনে: মোছাঃ ... আক্তার", fontId = "noto_serif_bn_bold", fontSize = 54f, color = 0xFF831843L),
                        ShapeLayer(name = "Divider", x = 240f, y = 920f, width = 600f, height = 8f, shapeType = ShapeType.LINE, fillColor = 0xFFD4AF37L),
                        TextLayer(name = "Event Details", x = 110f, y = 990f, width = 860f, height = 420f, text = "তারিখ: ২৫ ডিসেম্বর ২০২৬\nসময়: দুপুর ১:৩০ ঘটিকা\nস্থান: কমিউনিটি সেন্টার, ঢাকা\nযোগাযোগ: +880 1700-000000", fontId = "hind_siliguri", fontSize = 44f, color = 0xFF3F1D2BL),
                        StickerLayer(name = "Twin Hearts", x = 450f, y = 1520f, width = 180f, height = 180f, stickerId = "nat_hearts", primaryColor = 0xFFE11D48L, secondaryColor = 0xFFFDA4AFL)
                    )
                )
            }
        ),
        // 6. জন্মদিনের কার্ড
        DesignTemplate(
            id = "tpl_birthday_card",
            title = "Birthday Card",
            titleBn = "জন্মদিনের কার্ড",
            category = "উৎসব",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Colorful • Balloon • শুভ জন্মদিন",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "জন্মদিনের কার্ড",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF311B92L,
                    backgroundGradientEnd = 0xFF7B1FA2L,
                    layers = listOf(
                        StickerLayer(name = "Celebration Burst", x = 60f, y = 60f, width = 260f, height = 260f, stickerId = "nat_confetti", primaryColor = 0xFFFACC15L, secondaryColor = 0xFF38BDF8L),
                        StickerLayer(name = "Firework", x = 760f, y = 60f, width = 260f, height = 260f, stickerId = "nat_firework", primaryColor = 0xFFFF7A00L, secondaryColor = 0xFFFACC15L),
                        ShapeLayer(name = "Balloon 1", x = 220f, y = 120f, width = 140f, height = 175f, shapeType = ShapeType.CIRCLE, fillColor = 0xFFEC4899L),
                        ShapeLayer(name = "Balloon 2", x = 720f, y = 140f, width = 140f, height = 175f, shapeType = ShapeType.CIRCLE, fillColor = 0xFF38BDF8L),
                        TextLayer(name = "Birthday Title", x = 80f, y = 340f, width = 920f, height = 160f, text = "শুভ জন্মদিন!", fontId = "atma_bold", fontSize = 96f, color = 0xFFFACC15L, shadowEnabled = true),
                        TextLayer(name = "Person Name", x = 100f, y = 530f, width = 880f, height = 120f, text = "নাম লিখুন (HAPPY BIRTHDAY)", fontId = "hind_siliguri_bold", fontSize = 52f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Wish Text", x = 120f, y = 700f, width = 840f, height = 180f, text = "তোমার আগামী দিনগুলো হোক আনন্দময় ও সাফল্যমণ্ডিত।", fontId = "hind_siliguri", fontSize = 38f, color = 0xFFE9D5FFL)
                    )
                )
            }
        ),
        // 7. দোকান উদ্বোধন ব্যানার
        DesignTemplate(
            id = "tpl_shop_opening",
            title = "Shop Opening Banner",
            titleBn = "দোকান উদ্বোধন ব্যানার",
            category = "ব্যবসা",
            sizeLabel = "Banner 1ft × 2ft",
            subtitle = "Orange + White • শুভ উদ্বোধন",
            documentBuilder = {
                CanvasDocument.createSmart(
                    name = "দোকান উদ্বোধন ব্যানার",
                    targetWidthPx = 7200,
                    targetHeightPx = 3600,
                    dpi = 300,
                    category = "ব্যবসা",
                    backgroundColor = 0xFFFF7A00L,
                    smartLabel = "Smart Mode • Banner 1ft × 2ft (300 DPI)"
                ).copy(
                    backgroundGradientEnd = 0xFFCC4400L,
                    layers = listOf(
                        ShapeLayer(name = "White Panel", x = 60f, y = 60f, width = 1320f, height = 600f, fillColor = 0xFFFFFFFFL, cornerRadius = 36f),
                        StickerLayer(name = "Ribbon Tag", x = 420f, y = 90f, width = 600f, height = 120f, stickerId = "bdg_ribbon", primaryColor = 0xFFFF7A00L, secondaryColor = 0xFFFFFFFFL),
                        TextLayer(name = "Opening Headline", x = 100f, y = 110f, width = 1240f, height = 110f, text = "শুভ উদ্বোধন!", fontId = "baloo_da_bold", fontSize = 72f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Shop Name", x = 100f, y = 250f, width = 1240f, height = 130f, text = "বিসমিল্লাহ এন্টারপ্রাইজ ও ফ্যাশন হাউজ", fontId = "noto_serif_bn_bold", fontSize = 68f, color = 0xFF1E1E1EL),
                        TextLayer(name = "Details", x = 120f, y = 410f, width = 1200f, height = 180f, text = "তারিখ: আগামীকাল সকাল ১০টা • স্থান: নিউ মার্কেট রোড\nযোগাযোগ: +880 1700-000000 • সবাই আমন্ত্রিত!", fontId = "hind_siliguri", fontSize = 40f, color = 0xFFCC4400L)
                    )
                )
            }
        ),
        // 8. চাকরির বিজ্ঞপ্তি
        DesignTemplate(
            id = "tpl_job_circular",
            title = "Job Circular Poster",
            titleBn = "চাকরির বিজ্ঞপ্তি",
            category = "ব্যবসা",
            sizeLabel = "Poster 1080×1350",
            subtitle = "Blue + White • Clean Corporate Layout",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "চাকরির বিজ্ঞপ্তি",
                    category = "ব্যবসা",
                    artboardWidth = 1080,
                    artboardHeight = 1350,
                    backgroundColor = 0xFF1E3A8AL,
                    backgroundGradientEnd = 0xFF172554L,
                    layers = listOf(
                        ShapeLayer(name = "Top White Sheet", x = 60f, y = 230f, width = 960f, height = 1040f, fillColor = 0xFFFFFFFFL, cornerRadius = 32f),
                        TextLayer(name = "Header", x = 80f, y = 70f, width = 920f, height = 130f, text = "নিয়োগ বিজ্ঞপ্তি • WE ARE HIRING", fontId = "hind_siliguri_bold", fontSize = 60f, color = 0xFFFFFFFFL),
                        ShapeLayer(name = "Role Badge", x = 140f, y = 285f, width = 800f, height = 110f, fillColor = 0xFF2563EBL, cornerRadius = 20f),
                        TextLayer(name = "Role Title", x = 140f, y = 305f, width = 800f, height = 80f, text = "পদ: গ্রাফিক ডিজাইনার ও মার্কেটিং", fontId = "hind_siliguri_bold", fontSize = 46f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Requirements", x = 110f, y = 450f, width = 860f, height = 520f, text = "যোগ্যতা: ন্যূনতম এইচ.এস.সি / স্নাতক\nবেতন: আলোচনা সাপেক্ষে (আকর্ষণীয়)\nকর্মস্থল: ঢাকা / রিমোট\nআবেদনের শেষ তারিখ: ৩০ তারিখ", fontId = "hind_siliguri", fontSize = 42f, color = 0xFF1E293BL, align = TextAlignMode.LEFT),
                        TextLayer(name = "Apply Info", x = 110f, y = 1080f, width = 860f, height = 120f, text = "সিভি পাঠান: hr@company.com\nকল করুন: +880 1700-000000", fontId = "hind_siliguri_bold", fontSize = 38f, color = 0xFF1E3A8AL)
                    )
                )
            }
        ),
        // 9. কোচিং ভর্তি পোস্টার
        DesignTemplate(
            id = "tpl_coaching_admission",
            title = "Coaching Admission",
            titleBn = "কোচিং ভর্তি পোস্টার",
            category = "মাদ্রাসা",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Yellow + Black • Student Badge",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "কোচিং ভর্তি পোস্টার",
                    category = "মাদ্রাসা",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFFFACC15L,
                    backgroundGradientEnd = 0xFFEAB308L,
                    layers = listOf(
                        ShapeLayer(name = "Black Banner", x = 60f, y = 70f, width = 960f, height = 210f, fillColor = 0xFF111111L, cornerRadius = 28f),
                        TextLayer(name = "Coaching Name", x = 80f, y = 100f, width = 920f, height = 160f, text = "সাকসেস একাডেমিক কোচিং\nভর্তি চলছে!", fontId = "anek_bangla_bold", fontSize = 58f, color = 0xFFFACC15L),
                        StickerLayer(name = "Student Award", x = 760f, y = 320f, width = 240f, height = 240f, stickerId = "bdg_trophy", primaryColor = 0xFF111111L, secondaryColor = 0xFFFACC15L),
                        TextLayer(name = "Subjects", x = 80f, y = 340f, width = 660f, height = 420f, text = "শ্রেণি: ৬ষ্ঠ - দ্বাদশ (বিজ্ঞান/মানবিক)\nঅভিজ্ঞ শিক্ষক ও সাপ্তাহিক পরীক্ষা\nশুরু: নতুন ব্যাচ ১ তারিখ", fontId = "hind_siliguri_bold", fontSize = 42f, color = 0xFF111111L, align = TextAlignMode.LEFT),
                        ShapeLayer(name = "Footer Strip", x = 60f, y = 860f, width = 960f, height = 130f, fillColor = 0xFF111111L, cornerRadius = 24f),
                        TextLayer(name = "Phone", x = 80f, y = 890f, width = 920f, height = 80f, text = "ঠিকানা: কলেজ রোড • +880 1700-000000", fontId = "hind_siliguri_bold", fontSize = 40f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 10. ফেসবুক পেজ প্রমোশন
        DesignTemplate(
            id = "tpl_fb_page_promo",
            title = "Facebook Page Promo",
            titleBn = "ফেসবুক পেজ প্রমোশন",
            category = "ব্যবসা",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Blue • Like & Follow Buttons",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "ফেসবুক পেজ প্রমোশন",
                    category = "ব্যবসা",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF1877F2L,
                    backgroundGradientEnd = 0xFF0C4A9EL,
                    layers = listOf(
                        ShapeLayer(name = "Card Bg", x = 80f, y = 130f, width = 920f, height = 820f, fillColor = 0xFFFFFFFFL, cornerRadius = 40f),
                        StickerLayer(name = "Like Icon", x = 440f, y = 190f, width = 200f, height = 200f, stickerId = "soc_like", primaryColor = 0xFF1877F2L, secondaryColor = 0xFFFFFFFFL),
                        TextLayer(name = "Page Name", x = 120f, y = 420f, width = 840f, height = 140f, text = "আমাদের ফেসবুক পেজ\nলাইক ও ফলো করুন!", fontId = "hind_siliguri_bold", fontSize = 58f, color = 0xFF111827L),
                        TextLayer(name = "Desc", x = 140f, y = 590f, width = 800f, height = 130f, text = "প্রতিদিন নতুন ডিজাইন, অফার ও টিপস পেতে যুক্ত থাকুন", fontId = "hind_siliguri", fontSize = 36f, color = 0xFF4B5563L),
                        ShapeLayer(name = "Follow Pill", x = 260f, y = 760f, width = 560f, height = 110f, fillColor = 0xFF1877F2L, cornerRadius = 55f),
                        TextLayer(name = "CTA", x = 260f, y = 785f, width = 560f, height = 70f, text = "LIKE • FOLLOW • SHARE", fontId = "poppins_bold", fontSize = 40f, color = 0xFFFFFFFFL)
                    )
                )
            }
        )
    )
}
