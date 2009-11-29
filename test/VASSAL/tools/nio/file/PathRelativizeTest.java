package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathRelativizeTest extends AbstractPathMethodTest {
  protected final String left;
  protected final String right;
  protected final String expected;

  public PathRelativizeTest(FileSystem fs, String left,
                            String right, String expected) {
    super(fs);

    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  protected void doTest() {
    final Path path = fs.getPath(left).relativize(fs.getPath(right));
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}
