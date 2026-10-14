package com.example.balloonpop.model

import androidx.compose.ui.graphics.Color

/**
 * Zone Companion Mini Balloon Pet:
 * A delightful, loyal balloon pet that accompanies the player through every zone.
 * Floats alongside the player on the World Map and in Gameplay, reacting to combos,
 * cheering on boss battles, and making every zone feel alive and charming.
 */
data class ZonePet(
    val id: String,
    val name: String,
    val title: String,
    val emoji: String,
    val primaryColor: Color,
    val glowColor: Color,
    val trait: String,
    val voiceQuote: String,
    val bossCheerQuote: String
)

object MiniPetRegistry {
    val ZONE_PETS = listOf(
        ZonePet(
            id = "pet_pip",
            name = "Pip",
            title = "Glow-Pup Balloon",
            emoji = "🐶🎈",
            primaryColor = Color(0xFF00F0FF),
            glowColor = Color(0xFF06D6A0),
            trait = "Spawns lucky gold balloons",
            voiceQuote = "Woof! Let's pop 'em all!",
            bossCheerQuote = "Grrr! Smash that Boss Balloon!"
        ),
        ZonePet(
            id = "pet_lumi",
            name = "Lumi",
            title = "Crystal Fox Balloon",
            emoji = "🦊🎈",
            primaryColor = Color(0xFF9D4EDD),
            glowColor = Color(0xFFFF2A75),
            trait = "Senses hidden combos",
            voiceQuote = "Yip! The crystals shine bright!",
            bossCheerQuote = "Aim for the weak spot!"
        ),
        ZonePet(
            id = "pet_sunny",
            name = "Sunny",
            title = "Mesa Hawk Balloon",
            emoji = "🦅🎈",
            primaryColor = Color(0xFFFF7A00),
            glowColor = Color(0xFFFFD700),
            trait = "Warns of danger bombs",
            voiceQuote = "Screee! Fast reflexes win!",
            bossCheerQuote = "Dive-strike the Titan Boss!"
        ),
        ZonePet(
            id = "pet_bubbles",
            name = "Bubbles",
            title = "Coral Octo Balloon",
            emoji = "🐙🎈",
            primaryColor = Color(0xFF00D2FF),
            glowColor = Color(0xFF00F5D4),
            trait = "Creates bubble shields",
            voiceQuote = "Bloop! Deep ocean power!",
            bossCheerQuote = "Ink the boss eyes! We got this!"
        ),
        ZonePet(
            id = "pet_fluff",
            name = "Fluff",
            title = "Sugar Bunny Balloon",
            emoji = "🐰🎈",
            primaryColor = Color(0xFFFF70A6),
            glowColor = Color(0xFFFF9770),
            trait = "Lures rainbow balloon swarms",
            voiceQuote = "Hop! Sweet sugary combos!",
            bossCheerQuote = "Don't give up! Sweet victory awaits!"
        ),
        ZonePet(
            id = "pet_sparky",
            name = "Sparky",
            title = "Cyber Kitten Balloon",
            emoji = "🐱⚡",
            primaryColor = Color(0xFF06D6A0),
            glowColor = Color(0xFF00F0FF),
            trait = "Overcharges lightning strikes",
            voiceQuote = "Purr! Voltage at 100%!",
            bossCheerQuote = "Zap the boss shield with all your might!"
        ),
        ZonePet(
            id = "pet_ignis",
            name = "Ignis",
            title = "Fire Drake Balloon",
            emoji = "🐲🔥",
            primaryColor = Color(0xFFFF3366),
            glowColor = Color(0xFFFFB703),
            trait = "Melts frozen hazards",
            voiceQuote = "Roar! Burn the hazards down!",
            bossCheerQuote = "Breathe fire on the Titan Boss!"
        ),
        ZonePet(
            id = "pet_nova",
            name = "Nova",
            title = "Cosmic Bear Balloon",
            emoji = "🐻✨",
            primaryColor = Color(0xFF7209B7),
            glowColor = Color(0xFF4CC9F0),
            trait = "Freezes stardust time",
            voiceQuote = "Grrr! Starlight protect us!",
            bossCheerQuote = "Summon the cosmic comet strike!"
        ),
        ZonePet(
            id = "pet_aureus",
            name = "Aureus",
            title = "Royal Griffin Balloon",
            emoji = "🦁👑",
            primaryColor = Color(0xFFFFD700),
            glowColor = Color(0xFFFFA000),
            trait = "Crown score bonus multiplier",
            voiceQuote = "Honor! Royal glory awaits!",
            bossCheerQuote = "Dethrone the false balloon king!"
        ),
        ZonePet(
            id = "pet_frosty",
            name = "Frosty",
            title = "Glacial Penguin Balloon",
            emoji = "🐧❄️",
            primaryColor = Color(0xFFA2D2FF),
            glowColor = Color(0xFFBDE0FE),
            trait = "Chills frenzy balloon speeds",
            voiceQuote = "Chirp! Stay frosty, partner!",
            bossCheerQuote = "Freeze the Titan Boss in solid ice!"
        )
    )

    fun getPetForZone(zoneIndex: Int): ZonePet {
        return ZONE_PETS.getOrElse(zoneIndex.coerceAtLeast(0) % ZONE_PETS.size) { ZONE_PETS.first() }
    }
}
