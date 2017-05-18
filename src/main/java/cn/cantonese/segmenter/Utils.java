package cn.cantonese.segmenter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final String LINE_DELIMITER = "\n";
    public static final String PART_DELIMITER_REGEX = "\\$_\\$";
    public static final String PART_DELIMITER = "$_$";
    public static final String SEG_DELIMITER = "=_=";


    public static boolean isEnglishDigital(String word) {
        return Pattern.matches("^[a-z0-9A-Z]+$", word);
    }

    public static String goldToSource(String gold) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = gold.split(SEG_DELIMITER);
        boolean[] isED = new boolean[tokens.length];
        sb.append(tokens[0]);
        isED[0] = isEnglishDigital(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            isED[i] = isEnglishDigital(tokens[i]);
            if (isED[i-1] && isED[i]) {
                // TODO: 2017/5/17 add a space between two English words? other?
                sb.append(" ");
            }
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    public static String tokensToGold(List<String> tokens) {
        // TODO: 2017/5/18 pay attention to whitespace
        tokens.removeIf((token)->Pattern.matches("\\s+", token));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            sb.append(tokens.get(i));
            if (i != tokens.size()-1) {
                sb.append(SEG_DELIMITER);
            }
        }
        return sb.toString();
    }

    public static List<Integer> toStates(String segSent) {
        List<Integer> states = new ArrayList<>();
        String[] tokens = segSent.split(Utils.SEG_DELIMITER);
        for (String token : tokens) {
            assert token.length() != 0;
            if (token.length() == 1) {
                states.add(3);
            }
            else {
                states.add(0);
                for (int i = 0; i < token.length()-2; i++) states.add(1);
                states.add(2);
            }
        }
        return states;
    }

}
