package com.example.model

import java.util.UUID

internal object TemplateCatalogPart2 {
    val templates: List<DesignTemplate> = listOf(
        // 11. ইউটিউব থাম্বনেইল
        DesignTemplate(
            id = "tpl_youtube_thumb",
            title = "YouTube Thumbnail",
            titleBn = "ইউটিউব থাম্বনেইল",
            category = "ব্যবসা",
            sizeLabel = "YouTube 1280×720",
            subtitle = "Red + Black • Big Bold Bangla Text • দেখুন",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "ইউটিউব থাম্বনেইল",
                    category = "ব্যবসা",
                    artboardWidth = 1280,
                    artboardHeight = 720,
                    backgroundColor = 0xFF111111L,
                    backgroundGradientEnd = 0xFF450A0AL,
                    layers = listOf(
                        ShapeLayer(name = "Red Slash", x = 770f, y = -60f, width = 580f, height = 840f, fillColor = 0xFFDC2626L, rotation = 12f),
                        TextLayer(name = "Big Hook", x = 64f, y = 110f, width = 740f, height = 280f, text = "মোবাইলে প্রফেশনাল\nগ্রাফিক ডিজাইন!", fontId = "anek_bangla_bold", fontSize = 80f, color = 0xFFFFFFFFL, align = TextAlignMode.LEFT, shadowEnabled = true),
                        ShapeLayer(name = "Watch Pill", x = 64f, y = 470f, width = 380f, height = 110f, fillColor = 0xFFFACC15L, cornerRadius = 55f),
                        TextLayer(name = "Watch CTA", x = 64f, y = 490f, width = 380f, height = 80f, text = "এখনই দেখুন ▶", fontId = "hind_siliguri_bold", fontSize = 48f, color = 0xFF111111L, bold = true),
                        StickerLayer(name = "Play Crest", x = 900f, y = 230f, width = 260f, height = 260f, stickerId = "soc_play", primaryColor = 0xFF111111L, secondaryColor = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 12. ব্যবসার লোগো টেমপ্লেট
        DesignTemplate(
            id = "tpl_business_logo",
            title = "Business Logo",
            titleBn = "ব্যবসার লোগো টেমপ্লেট",
            category = "ব্যবসা",
            sizeLabel = "Logo 1000×1000",
            subtitle = "Editable Shop Name & Geometric Emblem",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "ব্যবসার লোগো",
                    category = "ব্যবসা",
                    artboardWidth = 1000,
                    artboardHeight = 1000,
                    backgroundColor = 0xFF1E1E1EL,
                    layers = listOf(
                        ShapeLayer(name = "Outer Ring", x = 220f, y = 110f, width = 560f, height = 560f, shapeType = ShapeType.CIRCLE, fillColor = 0x00000000L, strokeColor = 0xFFFF7A00L, strokeWidth = 16f),
                        StickerLayer(name = "Hex Emblem", x = 310f, y = 200f, width = 380f, height = 380f, stickerId = "frm_hex_tech", primaryColor = 0xFFFF7A00L, secondaryColor = 0xFFFFFFFFL),
                        StickerLayer(name = "Pen Nib", x = 400f, y = 280f, width = 200f, height = 220f, stickerId = "nat_pennib", primaryColor = 0xFFFF7A00L, secondaryColor = 0xFFFFFFFFL),
                        TextLayer(name = "Shop Name", x = 80f, y = 720f, width = 840f, height = 110f, text = "আরফিন ট্রেডার্স", fontId = "noto_serif_bn_bold", fontSize = 70f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Tagline", x = 140f, y = 830f, width = 720f, height = 70f, text = "QUALITY & TRUST • EST. 2026", fontId = "jost", fontSize = 32f, color = 0xFFFF9E40L)
                    )
                )
            }
        ),
        // 13. ঈদ অফার / দোকানের অফার
        DesignTemplate(
            id = "tpl_eid_offer",
            title = "Eid Offer Poster",
            titleBn = "ঈদ অফার / দোকানের অফার",
            category = "ব্যবসা",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Red + Yellow • ৫০% ছাড় Big Offer",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "ঈদ মেগা অফার",
                    category = "ব্যবসা",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFFB91C1CL,
                    backgroundGradientEnd = 0xFF7F1D1DL,
                    layers = listOf(
                        ShapeLayer(name = "Yellow Burst Bg", x = 220f, y = 220f, width = 640f, height = 340f, fillColor = 0xFFFACC15L, cornerRadius = 48f),
                        TextLayer(name = "Offer Header", x = 100f, y = 75f, width = 880f, height = 120f, text = "ঈদ স্পেশাল মেগা অফার!", fontId = "baloo_da_bold", fontSize = 68f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Discount Big", x = 220f, y = 270f, width = 640f, height = 220f, text = "৫০% ছাড়!", fontId = "anek_bangla_bold", fontSize = 120f, color = 0xFF991B1BL, bold = true),
                        TextLayer(name = "Shop Details", x = 100f, y = 620f, width = 880f, height = 220f, text = "সকল পণ্যে আকর্ষণীয় মূল্যছাড়\nঅফার চলবে চাঁদরাত পর্যন্ত!", fontId = "hind_siliguri", fontSize = 46f, color = 0xFFFFFFFFL),
                        ShapeLayer(name = "Order Pill", x = 240f, y = 870f, width = 600f, height = 110f, fillColor = 0xFF111111L, cornerRadius = 55f),
                        TextLayer(name = "Contact", x = 240f, y = 895f, width = 600f, height = 75f, text = "অর্ডার: +880 1700-000000", fontId = "hind_siliguri_bold", fontSize = 40f, color = 0xFFFACC15L)
                    )
                )
            }
        ),
        // 14. মৃত্যু সংবাদ / শোক বার্তা
        DesignTemplate(
            id = "tpl_shok_barta",
            title = "Shok Barta",
            titleBn = "মৃত্যু সংবাদ / শোক বার্তা",
            category = "ইসলামিক",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Black + White • Respectful Condolence",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "মৃত্যু সংবাদ ও শোক বার্তা",
                    category = "ইসলামিক",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF18181BL,
                    layers = listOf(
                        StickerLayer(name = "Border Frame", x = 40f, y = 40f, width = 1000f, height = 1000f, stickerId = "frm_double_gold", primaryColor = 0xFFE4E4E7L, secondaryColor = 0xFF18181BL),
                        TextLayer(name = "Arabic Header", x = 100f, y = 110f, width = 880f, height = 110f, text = "إِنَّا لِلّٰهِ وَإِنَّا إِلَيْهِ رَاجِعُوْنَ", fontId = "amiri_bold", fontSize = 54f, color = 0xFFFFFFFFL),
                        TextLayer(name = "Headline", x = 100f, y = 250f, width = 880f, height = 110f, text = "গভীর শোক ও সমবেদনা", fontId = "noto_serif_bn_bold", fontSize = 62f, color = 0xFFD4D4D8L),
                        TextLayer(name = "Body Details", x = 110f, y = 420f, width = 860f, height = 390f, text = "মরহুমের নাম: জনাব ...\nমৃত্যুর তারিখ: ...\nজানাজার নামাজ: বাদ জোহর, জামে মসজিদ প্রাঙ্গণ\n\nমহান আল্লাহ তায়ালা তাঁকে জান্নাতুল ফেরদৌস দান করুন।", fontId = "hind_siliguri", fontSize = 40f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 15. রক্তদান পোস্টার
        DesignTemplate(
            id = UUID.randomUUID().toString().let { "tpl_blood_donation" },
            title = "Blood Donation Poster",
            titleBn = "রক্তদান পোস্টার",
            category = "উৎসব",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Red + White • রক্ত দিন জীবন বাঁচান",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "রক্তদান পোস্টার",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFFFFFFFFL,
                    layers = listOf(
                        ShapeLayer(name = "Top Red Header", x = 0f, y = 0f, width = 1080f, height = 340f, fillColor = 0xFFDC2626L),
                        TextLayer(name = "Main Slogan", x = 80f, y = 90f, width = 920f, height = 170f, text = "রক্ত দিন, জীবন বাঁচান!", fontId = "anek_bangla_bold", fontSize = 76f, color = 0xFFFFFFFFL),
                        StickerLayer(name = "Heart Drop", x = 440f, y = 370f, width = 200f, height = 200f, stickerId = "soc_heart", primaryColor = 0xFFDC2626L, secondaryColor = 0xFFFFFFFFL),
                        TextLayer(name = "Group Info", x = 90f, y = 600f, width = 900f, height = 240f, text = "স্বেচ্ছায় রক্তদান কর্মসূচি\nপ্রয়োজনে রক্তের গ্রুপ: O+ / A+ / B+\nতারিখ ও স্থান: আপনার এলাকার নাম", fontId = "hind_siliguri", fontSize = 42f, color = 0xFF1F2937L),
                        ShapeLayer(name = "Call Box", x = 140f, y = 890f, width = 800f, height = 110f, fillColor = 0xFFDC2626L, cornerRadius = 24f),
                        TextLayer(name = "Phone", x = 140f, y = 912f, width = 800f, height = 75f, text = "জরুরি যোগাযোগ: +880 1700-000000", fontId = "hind_siliguri_bold", fontSize = 40f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 16. ক্রিকেট / ফুটবল টুর্নামেন্ট
        DesignTemplate(
            id = "tpl_khela_tournament",
            title = "Khela Tournament Poster",
            titleBn = "ক্রিকেট / ফুটবল টুর্নামেন্ট",
            category = "খেলা",
            sizeLabel = "Poster 1080×1350",
            subtitle = "Green Field • টুর্নামেন্ট ২০২৬",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "টুর্নামেন্ট ২০২৬ পোস্টার",
                    category = "খেলা",
                    artboardWidth = 1080,
                    artboardHeight = 1350,
                    backgroundColor = 0xFF14532DL,
                    backgroundGradientEnd = 0xFF052E16L,
                    layers = listOf(
                        StickerLayer(name = "Champion Trophy", x = 390f, y = 70f, width = 300f, height = 280f, stickerId = "bdg_trophy", primaryColor = 0xFFFACC15L, secondaryColor = 0xFF14532DL),
                        TextLayer(name = "Event Name", x = 80f, y = 370f, width = 920f, height = 210f, text = "বিজয় কাপ ক্রিকেট ও\nফুটবল টুর্নামেন্ট ২০২৬", fontId = "anek_bangla_bold", fontSize = 72f, color = 0xFFFFFFFFL, shadowEnabled = true),
                        ShapeLayer(name = "Match Box", x = 90f, y = 620f, width = 900f, height = 390f, fillColor = 0xFF166534L, strokeColor = 0xFFFACC15L, strokeWidth = 5f, cornerRadius = 32f),
                        TextLayer(name = "Match Info", x = 120f, y = 665f, width = 840f, height = 320f, text = "১ম পুরস্কার: ৫০,০০০ টাকা ও ট্রফি\nএন্ট্রি ফি: ২,০০০ টাকা\nতারিখ: ২০ ডিসেম্বর ২০২৬\nমাঠ: কেন্দ্রীয় স্টেডিয়াম", fontId = "hind_siliguri_bold", fontSize = 42f, color = 0xFFFEF08AL),
                        TextLayer(name = "Organizer", x = 100f, y = 1120f, width = 880f, height = 100f, text = "আয়োজনে: যুব ক্রীড়া সংঘ • +880 1700-000000", fontId = "hind_siliguri", fontSize = 38f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 17. মাদ্রাসার ফলাফল / সংবর্ধনা
        DesignTemplate(
            id = "tpl_songbordhona",
            title = "Songbordhona Poster",
            titleBn = "মাদ্রাসার ফলাফল / সংবর্ধনা",
            category = "মাদ্রাসা",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Golden • Student Photo Frame",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "কৃতি শিক্ষার্থী সংবর্ধনা",
                    category = "মাদ্রাসা",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF291D09L,
                    backgroundGradientEnd = 0xFF140E04L,
                    layers = listOf(
                        StickerLayer(name = "Gold Frame", x = 35f, y = 35f, width = 1010f, height = 1010f, stickerId = "frm_double_gold", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF291D09L),
                        TextLayer(name = "Heading", x = 100f, y = 90f, width = 880f, height = 120f, text = "কৃতি শিক্ষার্থী সংবর্ধনা", fontId = "noto_serif_bn_bold", fontSize = 66f, color = 0xFFFFD54FL),
                        ShapeLayer(name = "Student Photo Placeholder", x = 365f, y = 240f, width = 350f, height = 350f, shapeType = ShapeType.CIRCLE, fillColor = 0xFF423012L, strokeColor = 0xFFD4AF37L, strokeWidth = 10f),
                        TextLayer(name = "Photo Hint", x = 375f, y = 380f, width = 330f, height = 70f, text = "ছবি যুক্ত করুন", fontId = "hind_siliguri", fontSize = 34f, color = 0xFFD4AF37L),
                        TextLayer(name = "Student Info", x = 100f, y = 640f, width = 880f, height = 280f, text = "নাম: হাফেজ মোঃ আব্দুল্লাহ\nফলাফল: বোর্ড স্ট্যান্ড / জিপিএ-৫\nপ্রতিষ্ঠান: আদর্শ ইসলামিক মাদ্রাসা", fontId = "hind_siliguri_bold", fontSize = 44f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 18. হজ্জ মোবারক / ওমরাহ
        DesignTemplate(
            id = "tpl_hajj_mubarak",
            title = "Hajj Mubarak",
            titleBn = "হজ্জ মোবারক / ওমরাহ",
            category = "ইসলামিক",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Green + Gold • Kaaba Motif",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "হজ্জ ও ওমরাহ মোবারক",
                    category = "ইসলামিক",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF063322L,
                    backgroundGradientEnd = 0xFF021A11L,
                    layers = listOf(
                        StickerLayer(name = "Arch", x = 60f, y = 50f, width = 960f, height = 980f, stickerId = "isl_mihrab", primaryColor = 0xFFD4AF37L, secondaryColor = 0xFF063322L),
                        ShapeLayer(name = "Kaaba Body", x = 400f, y = 180f, width = 280f, height = 280f, fillColor = 0xFF111111L, strokeColor = 0xFFD4AF37L, strokeWidth = 6f),
                        ShapeLayer(name = "Kaaba Gold Belt", x = 400f, y = 245f, width = 280f, height = 42f, fillColor = 0xFFD4AF37L),
                        TextLayer(name = "Title", x = 100f, y = 510f, width = 880f, height = 140f, text = "হজ্জ ও ওমরাহ মোবারক", fontId = "noto_serif_bn_bold", fontSize = 72f, color = 0xFFFFD54FL),
                        TextLayer(name = "Dua Text", x = 120f, y = 670f, width = 840f, height = 220f, text = "আল্লাহ আপনার পবিত্র হজ্জ ও ওমরাহ কবুল করুন।\nআমিন • আপনার নাম / এজেন্সির নাম", fontId = "hind_siliguri", fontSize = 40f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 19. নববর্ষ শুভেচ্ছা
        DesignTemplate(
            id = "tpl_pohela_boishakh",
            title = "Pohela Boishakh",
            titleBn = "নববর্ষ শুভেচ্ছা",
            category = "উৎসব",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Red + White • Alpona Design • শুভ নববর্ষ",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "শুভ নববর্ষ ১৪৩৩",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFFFFFFFFL,
                    layers = listOf(
                        ShapeLayer(name = "Red Top", x = 0f, y = 0f, width = 1080f, height = 240f, fillColor = 0xFFDC2626L),
                        ShapeLayer(name = "Red Bottom", x = 0f, y = 840f, width = 1080f, height = 240f, fillColor = 0xFFDC2626L),
                        StickerLayer(name = "Alpona Mandala", x = 350f, y = 180f, width = 380f, height = 380f, stickerId = "isl_12star", primaryColor = 0xFFDC2626L, secondaryColor = 0xFFFFFFFFL),
                        TextLayer(name = "Noboborsho", x = 80f, y = 570f, width = 920f, height = 150f, text = "শুভ নববর্ষ", fontId = "atma_bold", fontSize = 98f, color = 0xFFDC2626L),
                        TextLayer(name = "Greeting", x = 100f, y = 890f, width = 880f, height = 120f, text = "বৈশাখী শুভেচ্ছা ও ভালোবাসা • আপনার নাম", fontId = "hind_siliguri_bold", fontSize = 42f, color = 0xFFFFFFFFL)
                    )
                )
            }
        ),
        // 20. বিজয় দিবস / স্বাধীনতা দিবস
        DesignTemplate(
            id = "tpl_bijoy_dibos",
            title = "Bijoy Dibos Poster",
            titleBn = "বিজয় দিবস / স্বাধীনতা দিবস",
            category = "উৎসব",
            sizeLabel = "Facebook Post 1080×1080",
            subtitle = "Red + Green • Bangladesh Flag Theme",
            documentBuilder = {
                CanvasDocument(
                    id = UUID.randomUUID().toString(),
                    name = "মহান বিজয় দিবস",
                    category = "উৎসব",
                    artboardWidth = 1080,
                    artboardHeight = 1080,
                    backgroundColor = 0xFF006A4EL,
                    backgroundGradientEnd = 0xFF004532L,
                    layers = listOf(
                        ShapeLayer(name = "Red Sun", x = 290f, y = 170f, width = 460f, height = 460f, shapeType = ShapeType.CIRCLE, fillColor = 0xFFF42A41L),
                        TextLayer(name = "Bijoy Title", x = 80f, y = 320f, width = 920f, height = 160f, text = "মহান বিজয় দিবস", fontId = "noto_serif_bn_bold", fontSize = 84f, color = 0xFFFFFFFFL, shadowEnabled = true),
                        TextLayer(name = "Tribute", x = 100f, y = 690f, width = 880f, height = 220f, text = "১৬ই ডিসেম্বর ও ২৬শে মার্চ\nসকল বীর শহীদদের প্রতি বিনম্র শ্রদ্ধা", fontId = "hind_siliguri_bold", fontSize = 44f, color = 0xFFFFFFFFL)
                    )
                )
            }
        )
    )
}
