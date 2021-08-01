package cat.nyaa.nyaacore.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionUtils {
    public static boolean isVersionGreaterOrEq(String versionStr, String otherStr) {
        int[] versionInt = splitVersionStringToInt(versionStr);
        int[] otherInt = splitVersionStringToInt(otherStr);
        if(Arrays.equals(versionInt, otherInt))return true; // =
        for(int i = 0;i<otherInt.length;i++){
            if (versionInt.length<=i){
                return false;//<
            }
            if(versionInt[i]<otherInt[i])return false;//>
        }
        return versionInt.length>=otherInt.length;
    }

    public static int[] splitVersionStringToInt(String version) {
        String[] splitVersion = splitVersionString(version);
        List<Integer> result = new ArrayList<>();
        for (String s : splitVersion) {
            int versionint;
            try{
                versionint =Integer.parseInt(s.replaceAll("[^\\d]", ""));
            }catch (NumberFormatException ignored){
             versionint = 0;
            }
            result.add(versionint);
        }
        return result.stream().mapToInt(i->i).toArray();
    }
    public static String[] splitVersionString(String version) {
        version = version.replace('-','.');
        version = version.replace('_','.');
        version = version.replace('R','.');
        String[] splitVersion = version.split("\\.");
        List<String> result = new ArrayList<>();
        for (String s : splitVersion) {
            if(!s.equals(""))result.add(s);
        }
        return result.toArray(new String[0]);
    }
}
