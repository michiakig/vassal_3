package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathIsAbsoluteTest extends AbstractPathMethodTest {
  protected final String input;

  public PathIsAbsoluteTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).isAbsolute());
  }
}
