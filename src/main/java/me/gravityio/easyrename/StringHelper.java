package me.gravityio.easyrename;

public class StringHelper {

    public static int getStartWord(int start, String s, boolean forward) {
        start = Math.max(Math.min(s.length() - 1, start), 0);

        boolean started = false;
        if (forward) {
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c != ' ' && started) {
                    return i;
                } else if (c == ' ') {
                    started = true;
                }
            }
            return s.length();
        } else {
            for (int i = start - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (c == ' ' && started) {
                    return i + 1;
                } else if (c != ' ') {
                    started = true;
                }
            }
            return 0;
        }
    }

    /**
     * Finds the first character after a space, either forwards or backwards<br>
     * '|' is the starting index<br>
     * e.g. for forwards: Hello| World would become Hello World<br>
     * e.g. for backwards: Hello Wor|ld would become Hello|ld<br>
     */
    public static int getWord(int start, String s, boolean forward) {
        boolean started = false;
        if (forward) {
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == ' ' && started) {
                    return i;
                } else if (c != ' ') {
                    started = true;
                }
            }
            return s.length();

        } else {
            for (int i = start - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (c == ' ' && started) {
                    return i + 1;
                } else if (c != ' ') {
                    started = true;
                }
            }
            return 0;
        }
    }

//    public static int

}
