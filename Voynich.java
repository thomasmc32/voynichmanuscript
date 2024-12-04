/**
 * Project: Voynich Manuscript Solo Project
 * Course: IST 242
 * Author: Thomas McConnell
 * Date Developed: 11/22/24
 * Last Date Changed: 12/4/24
 *
 * Citations:
 * https://github.com/joeoakes/javaBruteForceFreqAnalysis/
 * https://github.com/joeoakes/javaBruteForceDictionary/
 * https://github.com/joeoakes/javaConstitutionManuscript/
 * Java_Systems_Integration Flat Files.pptx
 * https://en.m.wikipedia.org/wiki/Voynich_manuscript
 * https://en.m.wikipedia.org/wiki/Voynich_manuscript#/media/File%3AVoynich_EVA.svg
 * https://collections.library.yale.edu/catalog/2002046
 * https://chatgpt.com/
 */

import java.io.*;
import java.util.*;

public class Voynich {

    /**
     * A map to store dictionaries for different languages.
     * Each dictionary is a set of valid words for the language.
     */
    private static final Map<String, Set<String>> DICTIONARIES = new HashMap<>();

    /**
     * Frequencies of letters in the English language (in percentage).
     */
    private static final double[] ENGLISH_FREQUENCIES = {
            8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094,
            6.966, 0.153, 0.772, 4.025, 2.406, 6.749, 7.507, 1.929,
            0.095, 5.987, 6.327, 9.056, 2.758, 0.978, 2.360, 0.150,
            1.974, 0.074
    };

    /**
     * Italian Frequencies.
     */
    private static final double[] ITALIAN_FREQUENCIES = {
            11.745, 0.927, 4.501, 3.736, 11.281, 1.644, 1.171, 0.734,
            10.143, 0.011, 0.009, 6.013, 2.512, 6.883, 9.832, 3.056,
            0.505, 6.367, 4.981, 5.623, 3.011, 1.838, 0.033, 0.007,
            0.013, 0.003
    };

    /**
     * Latin Frequencies.
     */
    private static final double[] LATIN_FREQUENCIES = {
            14.000, 12.000, 10.000, 8.500, 8.000, 7.000, 6.500, 6.000,
            5.500, 5.000, 4.500, 4.000, 3.500, 3.000, 2.500, 2.000,
            1.500, 1.000, 0.500, 0.200, 0.100, 0.050, 0.020, 0.010,
            0.005, 0.001
    };

    /**
     * Spanish Frequencies.
     */
    private static final double[] SPANISH_FREQUENCIES = {
            13.720, 11.525, 8.683, 7.979, 6.712, 6.871, 6.247, 4.967,
            4.632, 3.927, 3.510, 3.356, 2.510, 2.141, 1.492, 1.172,
            1.138, 0.877, 0.725, 0.608, 0.467, 0.271, 0.200, 0.150,
            0.070, 0.050
    };

    /**
     * The main method of the program.
     * It initializes dictionaries, reads the ciphertext, and performs analysis for each language.
     *
     * @param args Command-line arguments (if you wanna hardcode the cipher).
     */
    public static void main(String[] args) {
        String cipherText = "";

        // Load dictionaries for the englihs, italian, spanish, and latin
        try {
            loadDictionary("English", "english_dictionary.txt");
            loadDictionary("Italian", "italian_dictionary.txt");
            loadDictionary("Latin", "latin_dictionary.txt");
            loadDictionary("Spanish", "spanish_dictionary.txt");
        } catch (IOException e) {
            System.err.println("Error loading dictionaries: " + e.getMessage());
            return;
        }

        // Read the ciphertext input from a file
        try {
            File file = new File("vciphertext.txt");
            Scanner fileScanner = new Scanner(file);
            StringBuilder fileContent = new StringBuilder();

            // Read each line from the file
            while (fileScanner.hasNextLine()) {
                fileContent.append(fileScanner.nextLine()).append("\n");
            }
            fileScanner.close();
            // Clean the ciphertext by removing any characters that arent alphebet
            cipherText = fileContent.toString().toLowerCase().replaceAll("[^a-z\\s]", ""); // Preserve spaces
        } catch (FileNotFoundException e) {
            System.err.println("Error: File 'vciphertext.txt' not found");
            return;
        }

        // Perform analysis and decryption for each language
        processLanguage("English", cipherText, ENGLISH_FREQUENCIES);
        processLanguage("Italian", cipherText, ITALIAN_FREQUENCIES);
        processLanguage("Latin", cipherText, LATIN_FREQUENCIES);
        processLanguage("Spanish", cipherText, SPANISH_FREQUENCIES);
    }

    /**
     * Perform analysis and decryption for a specific language.
     *
     * @param language The language being processed.
     * @param text The ciphertext to analyze.
     * @param expectedFrequencies The letter frequencies of the language.
     */
    public static void processLanguage(String language, String text, double[] expectedFrequencies) {
        System.out.println("\n=== " + language + " ===");

        // Perform frequency analysis and show the best guess
        String bestGuess = performFrequencyAnalysis(text, expectedFrequencies, language);

        System.out.println("\nFrequency Analysis Best Guess for " + language + ": " + bestGuess);
        System.out.println("\nBrute Force:");

        boolean matchFound = false;

        // Attempt decryption with all Caesar cipher shifts and shows if a macth is detected, if so it will print
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

    /**
     * Perform frequency analysis of the text and identify the best guess.
     *
     * @param text The ciphertext to analyze.
     * @param expectedFrequencies The letter frequencies of the language.
     * @param language The language being analyzed.
     * @return The most likely decrypted text based on frequency matching.
     */
    public static String performFrequencyAnalysis(String text, double[] expectedFrequencies, String language) {
        int[] observedCounts = new int[26];
        int totalLetters = 0;

        // Count the occurrences of each letter in the ciphertext **ChatGPT helped here!!**
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

        // Display the observed and expected frequencies for the language
        System.out.printf("%-10s %-10s %-10s%n", "Letter", "Voynich %", language + " %");
        for (int i = 0; i < 26; i++) {
            System.out.printf("%-10c %-10.2f %-10.2f%n", (char) ('a' + i), observedFrequencies[i], expectedFrequencies[i]);
        }

        // Return the best decryption guess based on Chi-Square analysis
        return decryptUsingChiSquare(text, observedFrequencies, expectedFrequencies);
    }

    /**
     * Decrypt the text using Chi-Square analysis to find the best match.
     *
     * @param text The ciphertext to decrypt.
     * @param observedFrequencies The observed letter frequencies in the text.
     * @param expectedFrequencies The expected letter frequencies for the language.
     * @return The decrypted text with the lowest Chi-Square value.
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
     * Calculate the Chi-Square statistic for the text.
     *
     * @param text The text to analyze.
     * @param expectedFrequencies The expected frequencies for the language.
     * @return The Chi-Square statistic.
     */
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

    /**
     * Decrypt the text using the Caesar cipher with a specific shift.
     *
     * @param text The ciphertext to decrypt.
     * @param shift The number of positions to shift.
     * @return The decrypted text.
     */
    public static String caesarDecrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = 'a';
                char decryptedChar = (char) ((c - base - shift + 26) % 26 + base);
                result.append(decryptedChar);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Load a dictionary file for Each language.
     *
     * @param language The language of the dictionary.
     * @param filePath The path to the dictionary file.
     * @throws IOException If the file cannot be loaded.
     */
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
     * Check if the decrypted text contains words from the dictionary.
     *
     * @param language The language of the dictionary.
     * @param text The text to verify.
     * @return True if a match is found, otherwise false.
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