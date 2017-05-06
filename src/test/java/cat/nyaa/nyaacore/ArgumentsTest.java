package cat.nyaa.nyaacore;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArgumentsTest {
    @Test
    public void test1() throws Exception {
        String cmd = "`foo bar` far `bar \\`foo`";
        CommandReceiver.Arguments arg = CommandReceiver.Arguments.parse(cmd.split(" "));
        assertNotNull(arg);
        assertEquals("foo bar", arg.nextString());
        assertEquals("far", arg.nextString());
        assertEquals("bar `foo", arg.nextString());
    }

    @Test
    public void test2() throws Exception {
        String cmd = "key:33 a key:66 b key:13 `key2:a b c` `key 3:d e f`";
        CommandReceiver.Arguments arg = CommandReceiver.Arguments.parse(cmd.split(" "));
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
}