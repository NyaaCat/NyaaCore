package cat.nyaa.nyaacore.utils;

import org.junit.Test;

import java.util.Arrays;

public class VersionUtilsTest {
    @Test
    public void test1() throws Exception {
        String[] res1 = VersionUtils.splitVersionString("1.17.1R1");
        assert (Arrays.equals(res1, new String[]{"1", "17", "1", "1"}));
        String[] res2 = VersionUtils.splitVersionString("1.17.1R1_2");
        assert (Arrays.equals(res2, new String[]{"1", "17", "1", "1", "2"}));
        String[] res3 = VersionUtils.splitVersionString("1.17.1R1_2A");
        assert (Arrays.equals(res3, new String[]{"1", "17", "1", "1", "2A"}));
    }

    @Test
    public void test2() throws Exception {
        int[] res1 = VersionUtils.splitVersionStringToInt("1.17.1R1");
        assert (Arrays.equals(res1, new int[]{1, 17, 1, 1}));
        int[] res2 = VersionUtils.splitVersionStringToInt("1.17.1R1_2");
        assert (Arrays.equals(res2, new int[]{1, 17, 1, 1, 2}));
        int[] res3 = VersionUtils.splitVersionStringToInt("1.17.1R1_2A");
        assert (Arrays.equals(res3, new int[]{1, 17, 1, 1, 2}));
    }
    @Test
    public void test3() throws Exception {
        boolean res1 = VersionUtils.isVersionGreaterOrEq("1.17.1R1","1.17.1R2");
        assert (!res1);
        boolean res2 = VersionUtils.isVersionGreaterOrEq("1.17.1R1","1.17.1R1");
        assert (res2);
        boolean res3 = VersionUtils.isVersionGreaterOrEq("1.17.1","1.17.1R0");
        assert (!res3);
        boolean res4 = VersionUtils.isVersionGreaterOrEq("1.16.3","1.17.1R0");
        assert (!res4);
        boolean res5 = VersionUtils.isVersionGreaterOrEq("1.18.1R3","1.17.1R0");
        assert (res5);
    }
}
