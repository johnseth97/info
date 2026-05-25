package com.johnseth97.info.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NameUtilTest {

    static Stream<Arguments> prettyNameProvider() {
        return Stream.of(
            arguments("OAK_LOG",          "Oak Log"),
            arguments("ZOMBIE_VILLAGER",   "Zombie Villager"),
            arguments("STONE",             "Stone"),
            arguments("A",                 "A"),
            arguments("minecraft",         "Minecraft"),
            arguments("OAK_LOG_SLAB",      "Oak Log Slab"),
            arguments("a_b_c",             "A B C")
        );
    }

    @ParameterizedTest(name = "pretty(\"{0}\") == \"{1}\"")
    @MethodSource("prettyNameProvider")
    void pretty_convertsCorrectly(String input, String expected) {
        assertEquals(expected, NameUtil.pretty(input));
    }

    @Test
    void pretty_returnsEmptyForNull() {
        assertEquals("", NameUtil.pretty(null));
    }

    @Test
    void pretty_returnsEmptyForEmpty() {
        assertEquals("", NameUtil.pretty(""));
    }

    @Test
    void pretty_returnsEmptyForBlank() {
        assertEquals("", NameUtil.pretty("   "));
    }
}
