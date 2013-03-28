package de.is24.util.monitoring.keyhandler;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class DefaultKeyEscaperTest {
  private static final String TEXT_WITH_CHARACTERS_AND_NUMBERS_123 = "text.with.characters.and.numbers.123";

  @Test
  public void shouldNotTouchFineKeys() {
    DefaultKeyEscaper defaultKeyEscaper = new DefaultKeyEscaper();
    assertThat(defaultKeyEscaper.handle(TEXT_WITH_CHARACTERS_AND_NUMBERS_123)).isEqualTo(
      TEXT_WITH_CHARACTERS_AND_NUMBERS_123);
  }

  @Test
  public void shouldReplaceColon() {
    DefaultKeyEscaper defaultKeyEscaper = new DefaultKeyEscaper();
    assertThat(defaultKeyEscaper.handle("text.with:colon")).isEqualTo("text.with_colon");
  }

  @Test
  public void shouldReplaceEqual() {
    DefaultKeyEscaper defaultKeyEscaper = new DefaultKeyEscaper();
    assertThat(defaultKeyEscaper.handle("text.with=equals")).isEqualTo("text.with_equals");
  }

  @Test
  public void shouldReplaceMultipleOccurrences() {
    DefaultKeyEscaper defaultKeyEscaper = new DefaultKeyEscaper();
    assertThat(defaultKeyEscaper.handle("text:with:multiple:colons=and=equals:signs=a:long.one")).isEqualTo(
      "text_with_multiple_colons_and_equals_signs_a_long.one");
  }

}
