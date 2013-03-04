package de.is24.util.monitoring;

import org.junit.Test;


public class ValidatingKeyHandlerTest {
  private ValidatingKeyHandler handler = new ValidatingKeyHandler();

  @Test
  public void simpleStringKeyShouldPass() throws Exception {
    handler.handle("simple.key.ShouldWork");
  }

  @Test
  public void underscoreStringKeyShouldPass() throws Exception {
    handler.handle("simple_key.ShouldWork");
  }


  @Test(expected = IllegalArgumentException.class)
  public void colonShouldFail() throws Exception {
    handler.handle("colons:should.fail");
  }

  @Test(expected = IllegalArgumentException.class)
  public void openingSquareBracketsShouldFail() throws Exception {
    handler.handle("square.brackets.[should.fail");
  }

  @Test(expected = IllegalArgumentException.class)
  public void closingSquareBracketsShouldFail() throws Exception {
    handler.handle("square.brackets.should].fail");
  }

}
