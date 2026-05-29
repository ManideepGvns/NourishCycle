package com.wefit.nourishcycle.data

/**
 * Maps each of the 7 meal slot indices to a display category.
 * This is the single source of truth for donut chart segment labels.
 */
enum class MealCategory(val displayName: String, val slotIndex: Int) {
    MORNING_DRINK("Morning Drink", 0),
    BREAKFAST("Breakfast", 1),
    MID_MORNING_SNACK("Mid-Morning", 2),
    LUNCH("Lunch", 3),
    EVENING_SNACK("Eve Snack", 4),
    DINNER("Dinner", 5),
    NIGHT_DRINK("Night Drink", 6);

    companion object {
        fun fromSlotIndex(index: Int): MealCategory =
            entries.firstOrNull { it.slotIndex == index } ?: MORNING_DRINK
    }
}

data class MealSlot(
    val slotIndex: Int,       // 0–6
    val time: String,
    val category: MealCategory,
    val label: String,        // Short label shown on the card header
    val options: List<String>, // "or" choices displayed as read-only bullets
    val tip: String = ""       // Optional tip shown in a smaller line
)

data class DayPlan(
    val dayIndex: Int,        // 0=Mon … 6=Sun
    val dayName: String,
    val slots: List<MealSlot>
)

object DietPlanData {

    val days: List<DayPlan> = listOf(

        // ── DAY 1 — MONDAY ──────────────────────────────────────────
        DayPlan(
            dayIndex = 0, dayName = "Monday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf("Green tea + a pinch of cinnamon powder"),
                    "You can add honey as sweetener"),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "1 bowl mix sprouts + veggies",
                        "2 veggies dosa with mint coconut chutney",
                        "1 bowl quinoa upma with veggies",
                        "3 carrot-green peas idli + 1 cup sambhar / dal",
                        "2 besan cheela / dosa with veggies"
                    ),
                    "Add seasonal vegetables"),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Sunflower seeds (roasted) – 1 tsp",
                        "Tender coconut water – 100 ml",
                        "Pears / plums"
                    ),
                    "Add banana if coconut water is unavailable"),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "2 oats roti + 1 bowl carrot beetroot salad with chana sabji",
                        "1 bowl quinoa pulao + 1 katori beetroot carrot salad (sautéed)",
                        "1 katori matar paneer + 2 multigrain roti",
                        "2 multigrain roti with soybean curry"
                    ),
                    "Add more veggies"),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf(
                        "Boiled peanuts – 1 cup (100 g)",
                        "1 Halim ladoo"
                    )),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 small cup tofu & vegetable stir fry + roti",
                        "1 veggies loaded multigrain sandwich",
                        "1 bowl oats kichadi with veggies",
                        "1 cup vegetables (cooked) salad"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf(
                        "Plain milk + turmeric (only if hungry)",
                        "Warm water"
                    ))
            )
        ),

        // ── DAY 2 — TUESDAY ─────────────────────────────────────────
        DayPlan(
            dayIndex = 1, dayName = "Tuesday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf(
                        "Peppermint tea",
                        "Green tea with a pinch of cinnamon and 1 cardamom"
                    )),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "2 moong cheela + coriander-mint chutney",
                        "1 bowl wheat rava upma with veggies",
                        "1 bowl carrot-green peas red rice poha",
                        "2 vegetable chapati wrap with mint chutney"
                    )),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Pumpkin seeds – 2 tsp (roasted)",
                        "Pomegranate – 1",
                        "Kiwi – 1 or Orange – 1"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "2 ragi roti + 1 bowl amaranth leaves sabji",
                        "1 katori drumstick sabji + 2 ragi roti + 1 katori beetroot-carrot salad",
                        "1 bowl brown rice with mushroom curry"
                    ),
                    "You can add rasam & buttermilk"),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf("Leafy veggies soup"),
                    "Add pink salt only"),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl tomato basil soup + 1 chapatti",
                        "1 veggies loaded multigrain sandwich",
                        "1 paneer stuffed roti + 1 bowl stir fried veggies"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf("Warm water"))
            )
        ),

        // ── DAY 3 — WEDNESDAY ────────────────────────────────────────
        DayPlan(
            dayIndex = 2, dayName = "Wednesday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf(
                        "Green tea + lemon + honey",
                        "Spearmint tea + honey"
                    )),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "3 moong idlis + 1 bowl sambhar",
                        "2 small paneer stuffed paratha with green chutney",
                        "1 bowl sprouts chaat with veggies",
                        "1 cup sabduna kichadi with veggies"
                    ),
                    "Soak 5 almonds overnight and eat in morning"),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Flax seed – ½ tsp (roasted)",
                        "Pumpkin seed – ½ tsp (roasted)",
                        "Guava – 1 or Orange – 1",
                        "Dates – 2 (optional)"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "1 katori bhindi sabji + 1 bowl little millet + 1 katori carrot tomato salad",
                        "2 multigrain roti with soybean curry",
                        "1 bowl carrot-broccoli (sautéed/steamed) + 2 tsp white rice (without starch)",
                        "2 wheat chapati with channa / mix veg curry"
                    )),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf(
                        "Tomato basil soup – 1 bowl",
                        "Roasted chick peas – 1 cup (optional)"
                    )),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl carrot-mushroom soup + 1 chapatti",
                        "2 uttappam + 1 cup sambhar",
                        "1 garlic roti with carrot paneer sabji",
                        "1 bowl mix veg soup + 1 chapati"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf("1 apple + warm water"))
            )
        ),

        // ── DAY 4 — THURSDAY ─────────────────────────────────────────
        DayPlan(
            dayIndex = 3, dayName = "Thursday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf("Green tea + honey"),
                    "Use organic green tea leaves"),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "3 oats palak idli + 1 katori sambhar",
                        "2 besan cheela with veggies + mint chutney",
                        "1 bowl oats veg upma with lots of veggies",
                        "2 small paneer stuffed paratha with curd"
                    ),
                    "Add cucumber if desired"),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Sunflower seeds – 2 tsp (roasted)",
                        "Pomegranate – 1 or Guava – 1"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "1 cup brown rice with cluster beans sabji",
                        "2 ragi dosa / roti with spinach curry",
                        "1 katori bottle gourd-chana dal sabji",
                        "2 multigrain roti + 1 katori moong dal",
                        "2 chapatti with 1 bowl toor dal / french beans sabji"
                    ),
                    "Curd – ½ cup (homemade)"),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf(
                        "Yellow pumpkin soup",
                        "Green tea"
                    )),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl bottle gourd soup + 1 roti",
                        "1 moong dal cheela + 1 cup stir fried veggies",
                        "1 cup vegetables (cooked) salad"
                    ),
                    "Add very less oil"),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf(
                        "Warm water",
                        "1 guava (optional, only if hungry)"
                    ))
            )
        ),

        // ── DAY 5 — FRIDAY ───────────────────────────────────────────
        DayPlan(
            dayIndex = 4, dayName = "Friday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf("Cinnamon tea + honey"),
                    "Use cinnamon powder"),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "2 palak paratha / chapatti with veggies",
                        "1 bowl quinoa upma with veggies",
                        "2 moong chilla + 2 tbsp flaxseed-garlic chutney",
                        "2 ragi dosa / roti with spinach curry"
                    ),
                    "Add vegetable curry if fresh spinach is unavailable"),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Pumpkin seeds – 1 tsp (roasted)",
                        "Sunflower seeds – 1 tsp (roasted)",
                        "Dates – 5"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "1 cup brown mint rice with rajma curry",
                        "2 chapatti + 1 bowl toor dal / french beans sabji",
                        "2 multigrain roti + 1 bowl masoor dhal"
                    )),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf(
                        "Cooked moong sprouts",
                        "Guava – 1",
                        "Pomegranate – 1"
                    ),
                    "Add pink salt for dressing on sprouts"),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl mix veg soup + 1 chapati",
                        "1 cup foxtail millet upma with veggies",
                        "2 oats dosa with mint chutney"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf("Plain milk + turmeric"),
                    "Drink warm water")
            )
        ),

        // ── DAY 6 — SATURDAY ─────────────────────────────────────────
        DayPlan(
            dayIndex = 5, dayName = "Saturday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf(
                        "Raspberry tea (lemon + cinnamon + honey)",
                        "Green tea"
                    )),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "2 pesarattu (green gram dosa) with onion chutney",
                        "2 palak besan cheela + coriander-mint chutney",
                        "2 multigrain avocado toast",
                        "Wheat rava upma with coconut chutney"
                    )),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Flax seeds – 1 tsp",
                        "Pumpkin seeds – 1 tsp",
                        "Kiwi fruit – 2",
                        "1 apple + 1 orange (optional)"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "1 bowl methi, spinach quinoa kichadi with sautéed veggies",
                        "1 bowl little millet with bhindi sabji",
                        "1 cup brown rice with cluster beans sabji",
                        "1 bowl brown rice with matar paneer curry"
                    ),
                    "Mix veg stir-fry can be added as a side dish"),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf("Banana flower soup – 1 cup"),
                    "Add more turmeric"),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl moong dal soup + 1 veggies dosa",
                        "2 vegetable idly + dal + 1 small cup stir fried veggies",
                        "2 wheat chapatti + paneer sabji",
                        "2 wheat phulka + 1 cup mix veg curry"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf("1 apple"),
                    "Drink warm water")
            )
        ),

        // ── DAY 7 — SUNDAY ───────────────────────────────────────────
        DayPlan(
            dayIndex = 6, dayName = "Sunday",
            slots = listOf(
                MealSlot(0, "7:00 AM", MealCategory.MORNING_DRINK,
                    "Morning Drink",
                    listOf(
                        "Coconut milk – 1 glass",
                        "Green tea + lemon + honey"
                    ),
                    "Don't add any other sweetener"),
                MealSlot(1, "8:30 AM", MealCategory.BREAKFAST,
                    "Breakfast",
                    listOf(
                        "Spinach dosa – 2 with mix veg curry",
                        "1 bowl yellow moong dhal kichadi",
                        "1 bowl oats kichadi with veggies",
                        "2 moong chilla with garlic chutney"
                    )),
                MealSlot(2, "11:00 AM", MealCategory.MID_MORNING_SNACK,
                    "Mid-Morning Snack",
                    listOf(
                        "Watermelon",
                        "Coconut water"
                    )),
                MealSlot(3, "1:00 PM", MealCategory.LUNCH,
                    "Lunch",
                    listOf(
                        "2 multigrain chapati with mix veg curry",
                        "2 ragi balls (mudde) medium sized with peanut chutney",
                        "1 small cup rice with dal + sautéed veggies",
                        "1 bowl methi, spinach quinoa kichadi with sautéed veggies"
                    ),
                    "Non-veg option: mutton or seafood"),
                MealSlot(4, "4:30 PM", MealCategory.EVENING_SNACK,
                    "Evening Snack",
                    listOf(
                        "Orange – 1",
                        "Pomegranate – 1",
                        "Guava – 1"
                    )),
                MealSlot(5, "7:30 PM", MealCategory.DINNER,
                    "Dinner",
                    listOf(
                        "1 bowl ladies finger soup + 1 multigrain roti",
                        "2 wheat phulka + 1 cup mix veg curry",
                        "Daliya / wheat rava upma with coconut chutney"
                    )),
                MealSlot(6, "9:30 PM", MealCategory.NIGHT_DRINK,
                    "Night Drink",
                    listOf("Plain milk (only if hungry)"))
            )
        )
    )
}
