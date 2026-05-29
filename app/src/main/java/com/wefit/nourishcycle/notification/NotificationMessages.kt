package com.wefit.nourishcycle.notification

import com.wefit.nourishcycle.data.DietPlanData
import com.wefit.nourishcycle.data.MealCategory

/**
 * Creative, motivating notification content for each meal slot.
 *
 * Each slot has a pool of title+body pairs. The message is chosen by
 * rotating through the pool using the day-of-cycle index (0–6), so
 * users get a fresh message each day of the week.
 */
object NotificationMessages {

    data class NotificationContent(val title: String, val body: String)

    // Pool of messages per slot index — 7 messages rotated across the 7-day cycle
    private val messagePool: Map<Int, List<NotificationContent>> = mapOf(

        // ── Slot 0: 7:00 AM — Morning Drink ─────────────────────────
        MealCategory.MORNING_DRINK.slotIndex to listOf(
            NotificationContent(
                "🌿 Rise & Nourish!",
                "Your morning ritual is calling. A warm sip sets the tone for your entire fertility journey today."
            ),
            NotificationContent(
                "☀️ Good Morning, Beautiful!",
                "The sun is up and so is your wellness game. Time for your morning drink — your body is ready!"
            ),
            NotificationContent(
                "🍵 Sip. Breathe. Begin.",
                "Every great day starts with a nourishing moment. Your morning drink is waiting — don't keep it!"
            ),
            NotificationContent(
                "🌱 First Step of the Day",
                "Before the world gets loud, take a quiet moment to nourish yourself. Morning drink time!"
            ),
            NotificationContent(
                "✨ Your Body Woke Up Hungry",
                "After 8 hours of rest, your body deserves its morning ritual first thing. Let's go!"
            ),
            NotificationContent(
                "💚 Nourish Before You Hustle",
                "Your fertility journey is built one sip at a time. Morning drink — your daily superpower!"
            ),
            NotificationContent(
                "🌸 Today's Wellness Starts Now",
                "You showed up for yourself yesterday. Now show up again. Good morning — sip up! 🌿"
            )
        ),

        // ── Slot 1: 8:30 AM — Breakfast ─────────────────────────────
        MealCategory.BREAKFAST.slotIndex to listOf(
            NotificationContent(
                "🥗 Breakfast O'Clock!",
                "Fuel your fertility! A wholesome breakfast powers your hormones, your mood, and your day. Eat well!"
            ),
            NotificationContent(
                "⚡ Power Up Your Morning!",
                "Your body has been fasting all night. It's time to break it with love and nourishment. Dig in!"
            ),
            NotificationContent(
                "🌱 Plant the Seeds of Wellness",
                "What you eat for breakfast shapes your entire day. Make it count for your fertility journey!"
            ),
            NotificationContent(
                "🍽️ You Deserve a Proper Breakfast",
                "No skipping! Your hormones need consistent fuel to thrive. Today's breakfast is fertility medicine."
            ),
            NotificationContent(
                "💪 Strong Bodies Need Strong Meals",
                "Breakfast isn't optional on this journey — it's non-negotiable. Your body is waiting! 🌿"
            ),
            NotificationContent(
                "✨ Feed Your Future",
                "Every bite you take today is an investment in tomorrow. Nourishing breakfast = nourishing life!"
            ),
            NotificationContent(
                "🥘 Eat Like You Love Yourself",
                "Because you do! A warm, wholesome breakfast is the most loving thing you can do right now. 💚"
            )
        ),

        // ── Slot 2: 11:00 AM — Mid-Morning Snack ────────────────────
        MealCategory.MID_MORNING_SNACK.slotIndex to listOf(
            NotificationContent(
                "🌻 Halfway to Lunch!",
                "Don't let your energy dip — your mid-morning snack keeps your blood sugar steady and your mood bright!"
            ),
            NotificationContent(
                "🥜 Snack Smart, Thrive Big",
                "A handful of seeds or a piece of fruit right now does more for your fertility than you realise. Go for it!"
            ),
            NotificationContent(
                "⏰ 11 AM Check-In!",
                "Your body's fuel gauge is reading low. Refuel with your mid-morning snack before the slump hits!"
            ),
            NotificationContent(
                "🍎 Small Bites, Big Wins",
                "Consistent nourishment throughout the day keeps your hormones happy. Snack time is serious self-care!"
            ),
            NotificationContent(
                "✨ Your Metabolism Loves This",
                "Eating every 2-3 hours keeps your body in its happy, fertile zone. Mid-morning snack — don't skip it!"
            ),
            NotificationContent(
                "💚 Seeds of Today, Harvest of Tomorrow",
                "Your fertility-friendly seeds and fruits right now are building something beautiful inside you. Snack up!"
            ),
            NotificationContent(
                "🌿 Nourish the Gap",
                "The space between breakfast and lunch is where energy crashes happen. Be smart — snack mindfully now!"
            )
        ),

        // ── Slot 3: 1:00 PM — Lunch ─────────────────────────────────
        MealCategory.LUNCH.slotIndex to listOf(
            NotificationContent(
                "🍛 Lunch Is Served!",
                "Your body has been going strong all morning. Reward it with a fertility-boosting, wholesome lunch!"
            ),
            NotificationContent(
                "💛 Midday Nourishment Moment",
                "Step away from whatever you're doing — your body deserves this. A proper lunch = a powerful afternoon!"
            ),
            NotificationContent(
                "🌿 The Most Important Midday Habit",
                "Don't eat at your desk, don't rush. Sit, breathe, and nourish yourself. You've earned this lunch!"
            ),
            NotificationContent(
                "⚡ Refuel Your Fertility Engine",
                "Lunch is your body's reset button. Load up on vegetables, wholegrains, and proteins — you're building life!"
            ),
            NotificationContent(
                "🥗 Eat the Rainbow at Lunch",
                "Colourful vegetables, hearty grains, good proteins — your fertility plan is delicious. Lunch O'Clock! 🌸"
            ),
            NotificationContent(
                "✨ Halfway Through Your Wellness Day",
                "You've been amazing this morning! Keep the momentum going with a nourishing, mindful lunch. 💚"
            ),
            NotificationContent(
                "🍽️ Your Body Called — It Wants Lunch",
                "Don't make it wait! A wholesome afternoon meal keeps your energy, mood, and hormones balanced all day."
            )
        ),

        // ── Slot 4: 4:30 PM — Evening Snack ─────────────────────────
        MealCategory.EVENING_SNACK.slotIndex to listOf(
            NotificationContent(
                "🌼 Beat the 4 PM Slump!",
                "Afternoon energy dips are real — but your fertility-friendly snack is the secret weapon. Don't skip it!"
            ),
            NotificationContent(
                "🍊 Almost at the Finish Line!",
                "Just dinner left after this! Your evening snack bridges the gap and keeps cravings at bay. Eat mindfully!"
            ),
            NotificationContent(
                "✨ Nourish Between the Meals",
                "A small, intentional snack now prevents overeating at dinner. Your body and hormones will thank you!"
            ),
            NotificationContent(
                "💪 Keep That Momentum Going",
                "You've nourished yourself beautifully all day — don't drop the ball now. Evening snack time! 🌿"
            ),
            NotificationContent(
                "🌿 Steady Blood Sugar = Happy Hormones",
                "Consistent eating = consistent hormone levels. Your afternoon snack is fertility science in action!"
            ),
            NotificationContent(
                "🥜 Small Bite, Big Impact",
                "A handful of sprouts or a piece of fruit at 4:30 keeps your metabolism firing all evening long. Go!"
            ),
            NotificationContent(
                "💚 Your Afternoon Self-Care Moment",
                "Pause. Breathe. Eat something nourishing. This is your little act of love for your body today. 🌸"
            )
        ),

        // ── Slot 5: 7:30 PM — Dinner ─────────────────────────────────
        MealCategory.DINNER.slotIndex to listOf(
            NotificationContent(
                "🌙 Dinner Time, Queen!",
                "Wind down with a warm, nourishing dinner. Your body is about to enter its overnight healing mode. Fuel it!"
            ),
            NotificationContent(
                "🍜 End Your Day with Love",
                "A light, wholesome dinner = deep sleep + powerful overnight repair. Your fertility journey continues tonight!"
            ),
            NotificationContent(
                "✨ Almost a Perfect Day!",
                "You've nourished yourself beautifully today. One more meal to close the loop — make dinner count! 💚"
            ),
            NotificationContent(
                "🌿 Dinner = Your Body's Nighttime Prep",
                "What you eat at dinner becomes the building blocks your body uses while you sleep. Make it nutritious!"
            ),
            NotificationContent(
                "💛 Slow Down & Savour",
                "Evening meals should be mindful and calm. No rush — eat slowly, chew well, and let dinner nourish you deeply."
            ),
            NotificationContent(
                "🍛 Light is Right for Dinner",
                "A balanced, light dinner supports better sleep and better hormone production overnight. Eat well tonight! 🌙"
            ),
            NotificationContent(
                "🌸 One Last Nourishing Act",
                "Before the evening truly winds down — dinner! You've done so well today. Let's finish strong! ✨"
            )
        ),

        // ── Slot 6: 9:30 PM — Night Drink ────────────────────────────
        MealCategory.NIGHT_DRINK.slotIndex to listOf(
            NotificationContent(
                "🌙 Nightcap Ritual Time",
                "A warm turmeric milk or a cup of warm water signals your body: time to repair, restore, and rejuvenate!"
            ),
            NotificationContent(
                "✨ End the Day Right",
                "Your final nourishment of the day. A warm sip before sleep supports deep rest and overnight healing. 🌿"
            ),
            NotificationContent(
                "🍵 Sip & Let Go",
                "Release the day with a warm, calming drink. Your body has worked hard — reward it with this gentle ritual."
            ),
            NotificationContent(
                "💫 Sleep is Your Superpower",
                "Good sleep = great fertility. Your bedtime drink prepares your body for the magic that happens while you rest!"
            ),
            NotificationContent(
                "🌟 Today Was a Win",
                "You nourished your body beautifully today! Close with your night drink and let your body do the rest. 💚"
            ),
            NotificationContent(
                "🌙 Healing Happens at Night",
                "Your body repairs, balances hormones, and renews itself while you sleep. Give it this final act of love!"
            ),
            NotificationContent(
                "🌸 Gratitude & Nourishment",
                "Before you sleep — one last sip for your wellness. You showed up for yourself today. That's everything. ✨"
            )
        )
    )

    /**
     * Returns creative notification content for a given slot and day index.
     * dayIndex (0–6) rotates through the message pool so messages vary each day.
     */
    fun getContent(slotIndex: Int, dayIndex: Int): NotificationContent {
        val pool = messagePool[slotIndex] ?: defaultContent(slotIndex)
        val message = pool[dayIndex % pool.size]

        // Enrich body with the specific food for that day if available
        return try {
            val dayPlan = DietPlanData.days[dayIndex]
            val slot = dayPlan.slots.find { it.slotIndex == slotIndex }
            val food = slot?.options?.firstOrNull()
            if (food != null) {
                NotificationContent(
                    title = message.title,
                    body = "${message.body}\n\nToday: $food"
                )
            } else message
        } catch (e: Exception) {
            message
        }
    }

    private fun defaultContent(slotIndex: Int) = listOf(
        NotificationContent("🌿 Meal Time!", "Time to nourish your body on your fertility journey. You've got this!")
    )
}
