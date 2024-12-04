import java.io.*;
import java.util.*;

public class Voynich {

    // Map to hold dictionaries for different languages
    private static final Map<String, Set<String>> DICTIONARIES = new HashMap<>();

    // English letter frequencies (percentages)
    private static final double[] ENGLISH_FREQUENCIES = {
            8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094,
            6.966, 0.153, 0.772, 4.025, 2.406, 6.749, 7.507, 1.929,
            0.095, 5.987, 6.327, 9.056, 2.758, 0.978, 2.360, 0.150,
            1.974, 0.074
    };

    // Updated Italian letter frequencies (percentages)
    private static final double[] ITALIAN_FREQUENCIES = {
            11.745, 0.927, 4.501, 3.736, 11.281, 1.644, 1.171, 0.734,
            10.143, 0.011, 0.009, 6.013, 2.512, 6.883, 9.832, 3.056,
            0.505, 6.367, 4.981, 5.623, 3.011, 1.838, 0.033, 0.007,
            0.013, 0.003
    };

    // Latin letter frequencies (percentages)
    private static final double[] LATIN_FREQUENCIES = {
            14.000, 12.000, 10.000, 8.500, 8.000, 7.000, 6.500, 6.000,
            5.500, 5.000, 4.500, 4.000, 3.500, 3.000, 2.500, 2.000,
            1.500, 1.000, 0.500, 0.200, 0.100, 0.050, 0.020, 0.010,
            0.005, 0.001
    };

    // Spanish letter frequencies (percentages)
    private static final double[] SPANISH_FREQUENCIES = {
            13.720, 11.525, 8.683, 7.979, 6.712, 6.871, 6.247, 4.967,
            4.632, 3.927, 3.510, 3.356, 2.510, 2.141, 1.492, 1.172,
            1.138, 0.877, 0.725, 0.608, 0.467, 0.271, 0.200, 0.150,
            0.070, 0.050
    };

    public static void main(String[] args) {
        String cipherText = "";

        // Load dictionaries
        try {
            loadDictionary("English", "english_dictionary.txt");
            loadDictionary("Italian", "italian_dictionary.txt");
            loadDictionary("Latin", "latin_dictionary.txt");
            loadDictionary("Spanish", "spanish_dictionary.txt");
        } catch (IOException e) {
            System.err.println("Error loading dictionaries: " + e.getMessage());
            return;
        }

        // Read ciphertext
        try {
            File file = new File("vciphertext.txt");
            Scanner fileScanner = new Scanner(file);
            StringBuilder fileContent = new StringBuilder();

            while (fileScanner.hasNextLine()) {
                fileContent.append(fileScanner.nextLine()).append("\n");
            }
            fileScanner.close();
            cipherText = fileContent.toString().toLowerCase().replaceAll("[^a-z\\s]", ""); // Preserve spaces
        } catch (FileNotFoundException e) {
            System.err.println("Error: File 'vciphertext.txt' not found");
            return;
        }

        // Process each language
        processLanguage("English", cipherText, ENGLISH_FREQUENCIES);
        processLanguage("Italian", cipherText, ITALIAN_FREQUENCIES);
        processLanguage("Latin", cipherText, LATIN_FREQUENCIES);
        processLanguage("Spanish", cipherText, SPANISH_FREQUENCIES);
    }

    public static void processLanguage(String language, String text, double[] expectedFrequencies) {
        System.out.println("\n=== Processing for " + language + " ===");

        String bestGuess = performFrequencyAnalysis(text, expectedFrequencies, language);

        System.out.println("\nBest Guess (Frequency Analysis): " + bestGuess);
        System.out.println("\nBrute Force Attempts:");

        boolean matchFound = false;
        for (int shift = 0; shift < 26; shift++) {
            String decrypted = caesarDecrypt(bestGuess, shift);
            if (checkWithDictionary(language, decrypted)) {
                System.out.println("Potential match found with shift " + shift + ": " + decrypted);
                matchFound = true;
            }
        }

        if (!matchFound) {
            System.out.println("No matches found for " + language);
        }
    }

    public static String performFrequencyAnalysis(String text, double[] expectedFrequencies, String language) {
        int[] observedCounts = new int[26];
        int totalLetters = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                observedCounts[c - 'a']++;
                totalLetters++;
            }
        }

        double[] observedFrequencies = new double[26];
        for (int i = 0; i < 26; i++) {
            observedFrequencies[i] = 100.0 * observedCounts[i] / totalLetters;
        }

        System.out.printf("%-10s %-10s %-10s%n", "Letter", "Observed%", language + "%");
        for (int i = 0; i < 26; i++) {
            System.out.printf("%-10c %-10.2f %-10.2f%n", (char) ('a' + i), observedFrequencies[i], expectedFrequencies[i]);
        }

        return decryptUsingChiSquare(text, observedFrequencies, expectedFrequencies);
    }

    public static String decryptUsingChiSquare(String text, double[] observedFrequencies, double[] expectedFrequencies) {
        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE;

        for (int shift = 0; shift < 26; shift++) {
            String decryptedText = caesarDecrypt(text, shift);
            double chiSquare = calculateChiSquare(decryptedText, expectedFrequencies);

            if (chiSquare < lowestChiSquare) {
                lowestChiSquare = chiSquare;
                bestDecryption = decryptedText;
            }
        }

        return bestDecryption;
    }

    public static double calculateChiSquare(String text, double[] expectedFrequencies) {
        int[] observedCounts = new int[26];
        int totalLetters = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                observedCounts[c - 'a']++;
                totalLetters++;
            }
        }

        double chiSquare = 0.0;
        for (int i = 0; i < 26; i++) {
            double observed = observedCounts[i];
            double expected = totalLetters * expectedFrequencies[i] / 100;
            if (expected > 0) {
                chiSquare += Math.pow(observed - expected, 2) / expected;
            }
        }

        return chiSquare;
    }

    public static String caesarDecrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = 'a';
                char decryptedChar = (char) ((c - base - shift + 26) % 26 + base);
                result.append(decryptedChar);
            } else {
                result.append(c); // Preserve non-alphabet characters
            }
        }
        return result.toString();
    }

    public static void loadDictionary(String language, String filePath) throws IOException {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String word;
            while ((word = reader.readLine()) != null) {
                words.add(word.toLowerCase());
            }
        }
        DICTIONARIES.put(language, words);
        System.out.println(language + " dictionary loaded with " + words.size() + " words.");
    }

    public static boolean checkWithDictionary(String language, String text) {
        Set<String> dictionary = DICTIONARIES.get(language);
        if (dictionary == null) return false;

        String[] words = text.split("\\s+");
        for (String word : words) {
            if (dictionary.contains(word)) return true;
        }
        return false;
    }
}