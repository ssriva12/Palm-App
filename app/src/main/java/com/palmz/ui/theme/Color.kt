package com.palmz.ui.theme

import androidx.compose.ui.graphics.Color

// 70s sunburst: two colours do the work: rust (structure, actions) and mustard (ratings,
// highlights), sitting on a sun-faded cream ground. No third hue, no gradients.

// --- Light (primary look) ---
val Cream = Color(0xFFF3E9D2) // app ground
val Paper = Color(0xFFFCF6E8) // raised surfaces: clean and clearly lighter than the ground
val PaperSunk = Color(0xFFE4D6AE) // pressed / inset: clearly darker than the ground
val Rust = Color(0xFFD97A2B) // colour one
val OnRust = Color(0xFFFBF3E4)
val RustSoft = Color(0xFFF3D7BA) // soft orange fill for selected states
val OnRustSoft = Color(0xFF4A2E12)
val Mustard = Color(0xFFE0A72E) // colour two
val OnMustard = Color(0xFF3E2A08)
val MustardSoft = Color(0xFFF5E3B8)
val OnMustardSoft = Color(0xFF4A3712)
val InkText = Color(0xFF3B2A1E) // body text
val InkTextSoft = Color(0xFF6B5643) // secondary text
val Edge = Color(0xFFC9B78E)
val EdgeSoft = Color(0xFFDFD1AC)
val ShadowLight = Color(0xFF2C2015) // warm near-black; doubles as card ink (border)
val Danger = Color(0xFFC1442E) // brick red — destructive actions only
val OnDanger = Color(0xFFFBF3E4)
val DangerSoft = Color(0xFFF3D0C6)
val OnDangerSoft = Color(0xFF4A1F14)

// --- Dark: not a dim mirror of light. Near-black ground, brighter accents, cream ink for
// borders so card edges stay legible instead of vanishing into a muddy brown. ---
val Espresso = Color(0xFF150F0A) // app ground, near-black
val EspressoRaised = Color(0xFF332212) // raised surfaces, a real step up from ground
val EspressoSunk = Color(0xFF241809) // pressed / inset: darker than ground
val RustLift = Color(0xFFF0904A) // colour one, lifted
val OnRustLift = Color(0xFF3A1D07)
val RustLiftSoft = Color(0xFF4A2E14)
val OnRustLiftSoft = Color(0xFFF3D3AE)
val MustardLift = Color(0xFFFFC94D) // colour two, lifted
val OnMustardLift = Color(0xFF3E2E08)
val MustardLiftSoft = Color(0xFF4E3D18)
val OnMustardLiftSoft = Color(0xFFF7E3B0)
val InkOnDark = Color(0xFFF1E4CC) // body text; doubles as card ink (border)
val InkOnDarkSoft = Color(0xFFC4B291)
val EdgeDark = Color(0xFF5A4531)
val EdgeSoftDark = Color(0xFF3D2E20)
val DangerLift = Color(0xFFE8604A) // destructive actions only, lifted for contrast on dark
val OnDangerLift = Color(0xFF3A150C)
val DangerLiftSoft = Color(0xFF4A2418)
val OnDangerLiftSoft = Color(0xFFF3C8B8)
