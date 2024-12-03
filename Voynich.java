import java.io.*;
import java.util.*;

public class Voynich {

    // Map to hold dictionaries for different languages https://github.com/joeoakes/javaBruteForceFreqAnalysis/
    private static final Map<String, Set<String>> DICTIONARIES = new HashMap<>();

    // English letter frequencies (percentages)
    private static final double[] ENGLISH_FREQUENCIES = {
            8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094,
            6.966, 0.153, 0.772, 4.025, 2.406, 6.749, 7.507, 1.929,
            0.095, 5.987, 6.327, 9.056, 2.758, 0.978, 2.360, 0.150,
            1.974, 0.074
    };

    // Italian letter frequencies (percentages)
    private static final double[] ITALIAN_FREQUENCIES = {
            11.745, 11.745, 11.281, 9.832, 6.883, 6.367, 5.623, 5.308,
            5.136, 4.501, 3.736, 3.011, 2.515, 2.506, 2.097, 1.644,
            1.171, 0.927, 0.877, 0.432, 0.052, 0.034, 0.032, 0.014,
            0.012, 0.000
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

    /**
     * Main method to run the program.
     * It processes the ciphertext, analyzes letter frequencies, and applies decryption methods
     * for English, Latin, Spanish, and Italian.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        String cipherText = "";

        // Load dictionaries for supported languages https://github.com/joeoakes/javaBruteForceDictionary/
       //https://github.com/joeoakes/javaConstitutionManuscript
        try {
            loadDictionary("English", "english_dictionary.txt");
            loadDictionary("Italian", "italian_dictionary.txt");
            loadDictionary("Latin", "latin_dictionary.txt");
            loadDictionary("Spanish", "spanish_dictionary.txt");
        } catch (IOException e) {
            System.err.println("Error loading dictionaries: " + e.getMessage());
            return;
        }

        // Read ciphertext from a flat file
        try {
            //Java_Systems_Integration Flat Files.pptx
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
            return; // Exit the program if the file is not found
        }

        // "Clean" the ciphertext (preserving spaces and converting to lowercase)
        String ptext = cipherText;

        // Perform frequency analysis and decryption for each language
        processLanguage("English", ptext, ENGLISH_FREQUENCIES);
        processLanguage("Italian", ptext, ITALIAN_FREQUENCIES);
        processLanguage("Latin", ptext, LATIN_FREQUENCIES);
        processLanguage("Spanish", ptext, SPANISH_FREQUENCIES);
    }

    /**
     * Processes the ciphertext for a given language.
     * It performs frequency analysis and Caesar cipher brute force.
     *
     * @param language          The name of the language being processed.
     * @param text              The cleaned ciphertext.
     * @param expectedFrequencies The expected frequency percentages for the language.
     */
    public static void processLanguage(String language, String text, double[] expectedFrequencies) {
        System.out.println("\n=== Processing for " + language + " ===");

        System.out.println("\nFrequency Analysis Results:");
        String bestGuess = performFrequencyAnalysis(text, expectedFrequencies, language);
        System.out.println("\nBest Guess (Frequency Analysis): " + bestGuess);

        System.out.println("\nBrute Force Attempts:");
        for (int shift = 0; shift < 26; shift++) {
            String decrypted = caesarDecrypt(bestGuess, shift);
            System.out.println("Shift " + shift + ": " + decrypted);

            if (checkWithDictionary(language, decrypted)) {
                System.out.println("Potential match found with shift " + shift + ": " + decrypted);
            }
        }
    }

    /**
     * Performs frequency analysis and calculates observed frequencies.
     * Compares observed frequencies to expected frequencies.
     *
     * @param text               The text to analyze.
     * @param expectedFrequencies The expected frequencies for the language.
     * @param language           The name of the language.
     * @return A decrypted version of the text based on frequency analysis.
     */
    //
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

        System.out.printf("%-10s %-10s %-10s%n", "Letter", "Voynich%", language + "%");
        for (int i = 0; i < 26; i++) {
            System.out.printf("%-10c %-10.2f %-10.2f%n", (char) ('a' + i), observedFrequencies[i], expectedFrequencies[i]);
        }

        return decryptUsingChiSquare(text, observedFrequencies, expectedFrequencies);
    }

    /**
     * Decrypts text using Chi-Square analysis to find the best match.
     *
     * @param text               The text to decrypt.
     * @param observedFrequencies Observed letter frequencies.
     * @param expectedFrequencies Expected letter frequencies.
     * @return The decrypted text with the best Chi-Square score.
     */
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

    /**
     * Calculates the Chi-Square statistic for the decrypted text.
     *
     * @param text               The decrypted text.
     * @param expectedFrequencies Expected letter frequencies.
     * @return The Chi-Square score.
     */
//https://github.com/joeoakes/javaFrequencyAnalysis and ChatGPT
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
            chiSquare += Math.pow(observed - expected, 2) / expected;
        }

        return chiSquare;
    }

    /**
     * Decrypts text using a Caesar cipher shift.
     */
    //https://github.com/joeoakes/javaBruteForceFreqAnalysis/
    public static String caesarDecrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = 'a'; // All letters are treated as lowercase
                char decryptedChar = (char) ((c - base - shift + 26) % 26 + base);
                result.append(decryptedChar);
            } else {
                result.append(c); // Preserve non-alphabet characters
            }
        }
        return result.toString();
    }

    /**
     * Loads a dictionary for a given language from a file.
     */
    //https://github.com/joeoakes/javaBruteForceDictionary/
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

    /**
     * Checks if a decrypted text contains any words from the dictionary for a given language.
     */
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