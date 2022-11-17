package de.kai_morich.simple_usb_terminal;

import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

final class TextUtil {

    @ColorInt static int caretBackground = 0xff666666;

    final static String newline_crlf = "\r\n";
    final static String newline_lf = "\n";

    public static String textUtilPattern = setPattern();
    public static String setPattern(){
        int PACKET_LEN = 1000;

        //To finish on value 3.
        int pattern_len = PACKET_LEN +2;

        int digit = 0x31;
        char zero = (char) 0x30;
        char new_line = (char) 0x0a;
        char carriage_return = (char) 0x0d;
        char hash_mark = (char) 0x23 ;

        String packet = "";
        packet += zero;
        while(pattern_len > 0){
            packet += (char) digit;
            digit ++;
            if (digit > (0x31 + 8)){
                digit = 0x31;
            }
            pattern_len--;
        }

        packet += hash_mark;
        packet += carriage_return;// \r
        packet += new_line;// \n




//        System.out.println(packet.substring(pattern_len-10,pattern_len));
//        System.out.println(packet.substring(0,pattern_len));
//        System.out.println(packet.length());
        return packet;
    }

    static byte[] fromHexString(final CharSequence s) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte b = 0;
        int nibble = 0;
        for(int pos = 0; pos<s.length(); pos++) {
            if(nibble==2) {
                buf.write(b);
                nibble = 0;
                b = 0;
            }
            int c = s.charAt(pos);
            if(c>='0' && c<='9') { nibble++; b *= 16; b += c-'0';    }
            if(c>='A' && c<='F') { nibble++; b *= 16; b += c-'A'+10; }
            if(c>='a' && c<='f') { nibble++; b *= 16; b += c-'a'+10; }
        }
        if(nibble>0)
            buf.write(b);
        return buf.toByteArray();
    }

    static String toHexString(final byte[] buf) {
        return toHexString(buf, 0, buf.length);
    }

    static String toHexString(final byte[] buf, int begin, int end) {
        StringBuilder sb = new StringBuilder(3*(end-begin));
        toHexString(sb, buf, begin, end);
        return sb.toString();
    }

    static void toHexString(StringBuilder sb, final byte[] buf) {
        toHexString(sb, buf, 0, buf.length);
    }

    static void toHexString(StringBuilder sb, final byte[] buf, int begin, int end) {
        for(int pos=begin; pos<end; pos++) {
            if(sb.length()>0)
                sb.append(' ');
            int c;
            c = (buf[pos]&0xff) / 16;
            if(c >= 10) c += 'A'-10;
            else        c += '0';
            sb.append((char)c);
            c = (buf[pos]&0xff) % 16;
            if(c >= 10) c += 'A'-10;
            else        c += '0';
            sb.append((char)c);
        }
    }

    /**
     * use https://en.wikipedia.org/wiki/Caret_notation to avoid invisible control characters
     */
    static CharSequence toCaretString(CharSequence s, boolean keepNewline) {
        return toCaretString(s, keepNewline, s.length());
    }

    static CharSequence toCaretString(CharSequence s, boolean keepNewline, int length) {
        boolean found = false;
        for (int pos = 0; pos < length; pos++) {
            if (s.charAt(pos) < 32 && (!keepNewline ||s.charAt(pos)!='\n')) {
                found = true;
                break;
            }
        }
        if(!found)
            return s;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for(int pos=0; pos<length; pos++)
            if (s.charAt(pos) < 32 && (!keepNewline ||s.charAt(pos)!='\n')) {
                sb.append('^');
                sb.append((char)(s.charAt(pos) + 64));
                sb.setSpan(new BackgroundColorSpan(caretBackground), sb.length()-2, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.append(s.charAt(pos));
            }
        return sb;
    }


    static class HexWatcher implements TextWatcher {

        private final TextView view;
        private final StringBuilder sb = new StringBuilder();
        private boolean self = false;
        private boolean enabled = false;

        HexWatcher(TextView view) {
            this.view = view;
        }

        void enable(boolean enable) {
            if(enable) {
                view.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                view.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            enabled = enable;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!enabled || self)
                return;

            sb.delete(0,sb.length());
            int i;
            for(i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                if(c >= '0' && c <= '9') sb.append(c);
                if(c >= 'A' && c <= 'F') sb.append(c);
                if(c >= 'a' && c <= 'f') sb.append((char)(c+'A'-'a'));
            }
            for(i=2; i<sb.length(); i+=3)
                sb.insert(i,' ');
            final String s2 = sb.toString();

            if(!s2.equals(s.toString())) {
                self = true;
                s.replace(0, s.length(), s2);
                self = false;
            }
        }
    }

    static char new_line = (char) 0x0a;
    static char carriage_return = (char) 0x0d;
//    static String yoav_pattern = "0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123#" + carriage_return + new_line;


    static String yoav_pattern = "0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123#";

    public static boolean compareStringWithPatternNotConsecutive(String find_patten_in, String pattenToFind){
        int indexPattern = 0;
        int lenPattern = pattenToFind.length();
        boolean found = false;
        for (int i = 0; i < find_patten_in.length() && !found; i++) {
            if(find_patten_in.substring(i, i + 1).equals(pattenToFind.substring(indexPattern, indexPattern + 1))){
                indexPattern++;
            }
            if(indexPattern == lenPattern) {
                found = true;
            }
        }
        return found;
    }


    public static int CountNumberCountFound(String find_patten_in, String pattenToFind){
        boolean[] found_pattern = {false};
        int[] count = {0};
        int index_next_find  = 0;

        boolean[] found_broken = {false};

        while(index_next_find < find_patten_in.length()){
            index_next_find = compareStringWithPatternNotConsecutive(find_patten_in, pattenToFind,index_next_find,found_pattern,count, found_broken);
        }

        return count[0];
    }

    public static  HashMap<String,Integer> getHistogram(String pattern){
        HashMap<String, Integer> ret_his = new HashMap<>();
        for (int i = 0; i < pattern.length(); i++) {
            String c = pattern.substring(i,i+1);
            if(!ret_his.containsKey(c)){
                ret_his.put(c,1);
            } else {
                Integer currentCount = ret_his.get(c);
                ret_his.put(c,currentCount+1);
            }
        }

        return ret_his;
    }

    public static int compareStringWithPatternNotConsecutive(String find_patten_in, String pattenToFind, int start_index, boolean[] found, int[] count, boolean[] has_broken_pattern){
        int indexPattern = 0;
        int lenPattern = pattenToFind.length();
        found[0] = false;

        for (int i = start_index; i < find_patten_in.length() && !found[0]; i++) {
            String char_find_in = find_patten_in.substring(i, i + 1);
            String char_pattern = pattenToFind.substring(indexPattern, indexPattern + 1);

            if(char_find_in.equals(char_pattern)){
                indexPattern++;

                if(indexPattern == lenPattern) {
                    // a new pattern has been found.
                    found[0] = true;
                    count[0]++;
                    return i;
                }
            }
        }
//        if(!found[0]){
//            if(indexPattern > 0 ){
//                has_broken_pattern[0] = true;
//
//            } else  {
//                int len_find_pattern_in = find_patten_in.length();
//                String last_char = find_patten_in.substring(len_find_pattern_in-1, len_find_pattern_in);
//                if(pattenToFind.contains(last_char)){
//                    has_broken_pattern[0] = true;
//                }
//            }
//        }


        return start_index+1;
    }

    public static boolean compareHistogramHasBrokenPatternIn(String patternToFind, String stringToTestSearch){
        HashMap<String , Integer> histogram_pattern = getHistogram(patternToFind);
        HashMap<String , Integer> histogram_string_to_search_in = getHistogram(stringToTestSearch);

        for (String key: histogram_pattern.keySet()
        ) {
            if(histogram_string_to_search_in.containsKey(key) && histogram_pattern.get(key) < histogram_string_to_search_in.get(key)){
                return true;
            }
        }

        return false;
    }

    public static boolean CheckIfHasBrokenPattern(String find_patten_in, String patternToFind){
        boolean[] found = {false};
        int[] count = {0};

        int start_index = 0;
        int end_index = 0;
        boolean[] has_broken_pattern = {false};
        while(start_index < find_patten_in.length() && !has_broken_pattern[0]){
            end_index = compareStringWithPatternNotConsecutive(find_patten_in, patternToFind, start_index, found, count, has_broken_pattern);
            if(has_broken_pattern[0]){
                break;
            }
            has_broken_pattern[0] = compareHistogramHasBrokenPatternIn(patternToFind, find_patten_in.substring(start_index,end_index));
            start_index = end_index + 1;
        }

        return has_broken_pattern[0];
    }



}
