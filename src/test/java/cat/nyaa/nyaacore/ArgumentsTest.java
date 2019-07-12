package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArgumentsTest {
    @Test
    public void test1() throws Exception {
        String cmd = "`foo bar` far `bar \\`foo`";
        Arguments arg = Arguments.parse(cmd.split(" "));
        assertNotNull(arg);
        assertEquals("foo bar", arg.nextString());
        assertEquals("far", arg.nextString());
        assertEquals("bar `foo", arg.nextString());
    }

    @Test
    public void test2() throws Exception {
        String cmd = "key:33 a key:66 b key:13 `key2:a b c` `key 3:d e f`";
        Arguments arg = Arguments.parse(cmd.split(" "), null);
        assertNotNull(arg);
        assertEquals("key:33", arg.nextString());
        assertEquals(66, arg.argInt("key"));
        assertEquals("a b c", arg.argString("key2"));
        assertEquals("d e f", arg.argString("key 3"));

        assertEquals("a", arg.nextString());
        assertEquals("b", arg.nextString());
        assertEquals("key:13", arg.nextString());
        assertNull(arg.next());
    }

    @Test
    public void test3() throws Exception {
        String cmd = "t w key:`3` key2:`/co l u:miu_bug` ke3y:`12`";
        Arguments arg = Arguments.parse(cmd.split(" "));
        assertNotNull(arg);
        assertEquals(3, arg.argInt("key"));
        assertEquals("/co l u:miu_bug", arg.argString("key2"));
        assertEquals("t", arg.next());
        assertEquals("12", arg.argString("ke3y"));
        assertEquals("w", arg.nextString());
        try {
            arg.nextString();
        } catch (BadCommandException e) {//seems that no expectThrows available here
            assertEquals("internal.error.no_more_string", e.getMessage());//magic string, but no impact...
        }
    }

    @Test
    public void test4() throws Exception {
        String cmd = "key :`3`";
        Arguments arg = Arguments.parse(cmd.split(" "), null);
        assertNull(arg);
    }

    @Test
    public void test5() throws Exception {
        String cmd = "key: `3`";
        Arguments arg = Arguments.parse(cmd.split(" "), null);
        assertNotNull(arg);
        assertEquals("key:", arg.top());
        assertEquals("", arg.argString("key"));
        assertEquals("3", arg.nextString());
        assertNull(arg.next());
    }
}
