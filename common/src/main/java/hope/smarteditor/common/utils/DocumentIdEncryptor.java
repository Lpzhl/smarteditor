package hope.smarteditor.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DocumentIdEncryptor {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int base = alphabet.length();

    private static final Map<Character, Integer> charToIndexMap = new HashMap<>();
    private static final Map<Integer, Character> indexToCharMap = new HashMap<>();

    static {
        for (int i = 0; i < base; i++) {
            charToIndexMap.put(alphabet.charAt(i), i);
            indexToCharMap.put(i, alphabet.charAt(i));
        }
    }


    public static String encrypt(long documentId) {
        StringBuilder sb = new StringBuilder();
        while (documentId > 0) {
            long remainder = documentId % base;
            sb.append(indexToCharMap.get((int) remainder));
            documentId = documentId / base;
        }
        return sb.reverse().toString();
    }

    public static long decrypt(String encryptedId) {
        long documentId = 0;
        for (int i = 0; i < encryptedId.length(); i++) {
            char c = encryptedId.charAt(i);
            int position = charToIndexMap.get(c);
            documentId = documentId * base + position;
        }
        return documentId;
    }
}
