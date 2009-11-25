package VASSAL.tools.nio.file.fs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import VASSAL.tools.nio.file.FileSystem;

import VASSAL.tools.nio.file.PathEndsWithTest;
import VASSAL.tools.nio.file.PathGetNameIntTest;
import VASSAL.tools.nio.file.PathIsAbsoluteTest;
import VASSAL.tools.nio.file.PathNormalizeTest;
import VASSAL.tools.nio.file.PathRelativizeTest;
import VASSAL.tools.nio.file.PathResolveTest;
import VASSAL.tools.nio.file.PathStartsWithTest;

@RunWith(Suite.class)
@SuiteClasses({
  RealUnixPathTest.EndsWithTest.class,
  RealUnixPathTest.GetNameIntTest.class,
  RealUnixPathTest.IsAbsoluteTest.class,
  RealUnixPathTest.NormalizeTest.class,
  RealUnixPathTest.RelativizeTest.class,
  RealUnixPathTest.ResolveTest.class,
  RealUnixPathTest.StartsWithTest.class
})
public class RealUnixPathTest {
  protected static FileSystem fs;

  @BeforeClass
  public static void setupFS() throws IOException {
    final RealFileSystemProvider provider = new RealFileSystemProvider();
    fs = new RealFileSystem(provider);
  }

  @RunWith(Parameterized.class)
  public static class EndsWithTest extends PathEndsWithTest{
    public EndsWithTest(String left, String right, boolean expected) {
      super(RealUnixPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left       Right       Expected
        { "/",        "/",        true  },
        { "/",        "foo",      false },
        { "/",        "/foo",     false },
        { "/foo",     "foo",      true  },
        { "/foo",     "/foo",     true  },
        { "/foo",     "/",        false },
        { "/foo/bar", "bar",      true  },
        { "/foo/bar", "foo/bar",  true  },
        { "/foo/bar", "/foo/bar", true  },
        { "/foo/bar", "/bar",     false },
        { "foo",      "foo",      true  },
        { "foo/bar",  "bar",      true  },
        { "foo/bar",  "foo/bar",  true  }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetNameIntTest extends PathGetNameIntTest{
    public GetNameIntTest(String input, int index,
                          String expected, Class<? extends Throwable> tclass) {
      super(RealUnixPathTest.fs, input, index, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input   Index Expected Throws
        { "a/b/c", -1,  null,    IllegalArgumentException.class }, 
        { "a/b/c",  0,  "a",     null                           },
        { "a/b/c",  1,  "b",     null                           },
        { "a/b/c",  2,  "c",     null                           },
        { "a/b/c",  3,  null,    IllegalArgumentException.class },
        { "/",      0,  null,    IllegalArgumentException.class }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class IsAbsoluteTest extends PathIsAbsoluteTest {
    public IsAbsoluteTest(String input, boolean expected) {
      super(RealUnixPathTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/",    true  },
        { "/tmp", true  },
        { "tmp",  false }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class NormalizeTest extends PathNormalizeTest{
    public NormalizeTest(String input, String expected) {
      super(RealUnixPathTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input                Expected
        { "/",                  "/"         },
        { "foo",                "foo"       },
        { ".",                  null        },
        { "..",                 ".."        },
        { "/..",                "/"         },
        { "/../..",             "/"         },
        { "foo/.",              "foo"       },
        { "./foo",              "foo"       },
        { "foo/..",             null        },
        { "../foo",             "../foo"    },
        { "../../foo",          "../../foo" },
        { "foo/bar/..",         "foo"       },
        { "foo/bar/baz/../..",  "foo"       },
        { "/foo/bar/baz/../..", "/foo"      }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class RelativizeTest extends PathRelativizeTest {
    public RelativizeTest(String left, String right, String expected) {
      super(RealUnixPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Left     Right      Expected
        { "/a/b/c", "/a/b/c",  null         },
        { "/a/b/c", "d/e",     "/a/b/c/d/e" },
        { "/a/b/c", "../../x", "/a/x"       }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class ResolveTest extends PathResolveTest {
    public ResolveTest(String left, String right, String expected) {
      super(RealUnixPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Left   Right   Expected
        { "/tmp", "foo",  "/tmp/foo" },
        { "/tmp", "/foo", "/foo"     },
        { "tmp",  "foo",  "tmp/foo"  },
        { "tmp",  "/foo", "/foo"     }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class StartsWithTest extends PathStartsWithTest{
    public StartsWithTest(String left, String right, boolean expected) {
      super(RealUnixPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left       Right       Expected
        { "/",        "/",        true  },
        { "/",        "/foo",     false },
        { "/foo",     "/",        true  },
        { "/foo",     "/foo",     true  },
        { "/foo",     "/f",       false },
        { "/foo/bar", "/",        true  },
        { "/foo/bar", "/foo",     true  },
        { "/foo/bar", "/foo/bar", true  },
        { "/foo/bar", "/f",       false },
        { "/foo/bar", "foo",      false },
        { "/foo/bar", "foo/bar",  false },
        { "foo",      "foo",      true  },
        { "foo",      "f",        false },
        { "foo/bar",  "foo",      true  },
        { "foo/bar",  "foo/bar",  true  },
        { "foo/bar",  "f",        false },
        { "foo/bar",  "/foo",     false },
        { "foo/bar",  "/foo/bar", false },
      });
    }
  }
}
