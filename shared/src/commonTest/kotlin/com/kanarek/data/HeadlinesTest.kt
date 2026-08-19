package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeadlinesTest {
    private val now = 1_700_000_000_000L

    private fun item(
        title: String,
        source: String,
        ageHours: Double = 0.0,
        image: String? = null,
    ) = NewsItem(
        title = title,
        link = "https://$source.example/${title.hashCode()}",
        summary = "",
        imageUrl = image,
        source = source,
        publishedAtMillis = now - (ageHours * 3_600_000.0).toLong(),
    )

    @Test
    fun emptyInputYieldsEmpty() {
        assertEquals(emptyList<NewsItem>(), Headlines.rank(emptyList(), nowMillis = now))
    }

    @Test
    fun newerOutranksOlderWhenOtherwiseEqual() {
        val older = item("Alpha beta gamma delta", "A", ageHours = 48.0)
        val newer = item("Zeta theta kappa lambda", "B", ageHours = 1.0)
        val ranked = Headlines.rank(listOf(older, newer), topSources = emptySet(), nowMillis = now)
        assertEquals(newer.link, ranked.first().item.link)
    }

    @Test
    fun imageOutranksNoImageWhenSameAge() {
        val plain = item("Alpha beta gamma delta", "A", ageHours = 5.0)
        val withPic = item("Zeta theta kappa lambda", "B", ageHours = 5.0, image = "https://x/y.jpg")
        val ranked = Headlines.rank(listOf(plain, withPic), topSources = emptySet(), nowMillis = now)
        assertEquals(withPic.link, ranked.first().item.link)
    }

    @Test
    fun topSourceOutranksRegularWhenSameAge() {
        val regular = item("Alpha beta gamma delta", "Randomblog", ageHours = 5.0)
        val top = item("Zeta theta kappa lambda", "Reuters", ageHours = 5.0)
        val ranked = Headlines.rank(listOf(regular, top), topSources = setOf("reuters"), nowMillis = now)
        assertEquals(top.link, ranked.first().item.link)
    }

    @Test
    fun corroboratedStoryOutranksFresherSingleton() {
        val title = "Government announces major budget reform package today"
        val a = item(title, "SourceA", ageHours = 12.0)
        val b = item("Major budget reform package announced by government", "SourceB", ageHours = 12.0)
        val c = item("Budget reform package: government announces major changes", "SourceC", ageHours = 12.0)
        val fresh = item("Local bakery wins regional pastry contest", "SourceD", ageHours = 0.5)
        val ranked = Headlines.rank(listOf(fresh, a, b, c), topSources = emptySet(), nowMillis = now)
        assertTrue(ranked.first().item.source in setOf("SourceA", "SourceB", "SourceC"))
    }

    @Test
    fun headlinesRespectsLimit() {
        val items = (1..10).map { item("Story number $it about something", "S$it", ageHours = it.toDouble()) }
        assertEquals(3, Headlines.headlines(items, topSources = emptySet(), limit = 3, nowMillis = now).size)
    }

    @Test
    fun unrelatedTitlesDoNotCorroborate() {
        val x = item("Weather forecast sunny tomorrow", "A", ageHours = 10.0)
        val y = item("Stock market closes higher today", "B", ageHours = 10.0)
        val z = item("Football team signs new striker", "C", ageHours = 10.0)
        val ranked = Headlines.rank(listOf(x, y, z), topSources = emptySet(), nowMillis = now)
        assertEquals(3, ranked.size)
    }
}
