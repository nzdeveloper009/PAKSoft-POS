package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class ShopAvatarTest {
    @Test
    fun `initials keep their size when the avatar has its own size`() {
        assertEquals(1f, initialsScale(nominalPx = 168f, measuredPx = 168))
    }

    @Test
    fun `initials shrink and grow with an avatar measured smaller or larger mid transition`() {
        assertEquals(0.5f, initialsScale(nominalPx = 168f, measuredPx = 84))
        assertEquals(1.5f, initialsScale(nominalPx = 104f, measuredPx = 156))
    }

    @Test
    fun `an empty nominal size leaves the initials alone`() {
        assertEquals(1f, initialsScale(nominalPx = 0f, measuredPx = 84))
    }
}
