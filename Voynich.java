import java.io.*;
import java.util.*;

public class Voynich {

    // Map to hold dictionaries for different languages
    private static final Map<String, Set<String>> DICTIONARIES = new HashMap<>();

    /**
     * Main method to run the program.
     * It processes the ciphertext, analyzes letter frequencies, and applies decryption methods
     * for English, Latin, Spanish, and Italian.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        String cipherText = "";
        //Java_Systems_Integration Flat Files.pptx JOE OAKS---https://github.com/joeoakes/javaBruteForceDictionary/
        // Load dictionaries for supported languages
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
            File file = new File("vciphertext.txt");
            Scanner fileScanner = new Scanner(file);
            StringBuilder fileContent = new StringBuilder();

            while (fileScanner.hasNextLine()) {
                fileContent.append(fileScanner.nextLine()).append("\n");
            }
            fileScanner.close();
            cipherText = fileContent.toString().toLowerCase().replaceAll("[^a-z]", "");
        } catch (FileNotFoundException e) {
            System.err.println("Error: File 'ciphertext.txt' not found");
            return; // Exit the program if the file is not found
        }

        // "Clean" the ciphertext (remove spaces and make lowercase)
        String ptext = cipherText;

        // Perform frequency analysis and decryption for each language
        processLanguage("English", ptext, new char[]{'e', 't', 'a', 'o', 'i', 'n', 's', 'h', 'r', 'd', 'l', 'c', 'u', 'm', 'w', 'f', 'g', 'y', 'p', 'b', 'v', 'k', 'j', 'x', 'q', 'z'});
        processLanguage("Italian", ptext, new char[]{'e', 'a', 'i', 'o', 'n', 'r', 't', 'l', 's', 'c', 'd', 'u', 'm', 'p', 'v', 'g', 'f', 'b', 'q', 'z', 'h', 'x', 'j', 'k', 'y', 'w'});
        processLanguage("Latin", ptext, new char[]{'u', 'e', 'i', 'a', 't', 'n', 'o', 'r', 's', 'c', 'l', 'm', 'p', 'd', 'g', 'b', 'v', 'h', 'q', 'f', 'x', 'k', 'z', 'y', 'j', 'w'});
        processLanguage("Spanish", ptext, new char[]{'e', 'a', 'o', 's', 'n', 'r', 'i', 'l', 't', 'u', 'd', 'c', 'p', 'm', 'b', 'g', 'v', 'q', 'h', 'f', 'z', 'y', 'j', 'x', 'k', 'w'});
    }

    /**
     * Processes the ciphertext for a given language.
     * It performs frequency analysis and Caesar cipher brute force.
     *
     * @param language         The name of the language being processed.
     * @param text             The cleaned ciphertext.
     * @param letterFrequencies The letter frequency array for the language.
     */
    public static void processLanguage(String language, String text, char[] letterFrequencies) {
        System.out.println("\n=== Processing for " + language + " ===");

        System.out.println("\nFrequency Analysis Results:");
        String frequencyDecryptedText = performFrequencyAnalysis(text, letterFrequencies, language);

        System.out.println("\nBrute Force Attempts:");
        for (int shift = 0; shift < 26; shift++) {
            String decrypted = caesarDecrypt(frequencyDecryptedText, shift);
            System.out.println("Shift " + shift + ": " + decrypted);

            if (checkWithDictionary(language, decrypted)) {
                System.out.println("Potential match found with shift " + shift + ": " + decrypted);
            }
        }
    }

    /**
     * Decrypts text using a Caesar cipher shift.
     *
     * @param text  The text to decrypt.
     * @param shift The shift amount for the cipher.
     * @return The decrypted text.
     */
    //https://github.com/joeoakes/javaCaesarCipher---JOE OAKS
    public static String caesarDecrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = 'a'; // All letters are treated as lowercase
                char decryptedChar = (char) ((c - base - shift + 26) % 26 + base);
                result.append(decryptedChar);
            } else {
                result.append(c); // Keep non-letters as they are
            }
        }
        return result.toString();
    }

    /**
     * Analyzes letter frequencies in the text and tries to map them to common letters in a given language.
     *
     * @param text               The text to analyze.
     * @param languageFrequencies The letter frequencies for the language being analyzed.
     * @param language           The name of the language being analyzed.
     * @return A decrypted version of the text based on frequency analysis.
     */
    //https://github.com/joeoakes/javaBruteForceFreqAnalysis--JOE OAKS java_Security_Brute_Force_AES (3).pptx---JOE OAKS
    public static String performFrequencyAnalysis(String text, char[] languageFrequencies, String language) {
        // Count how often each letter appears
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Sort letters by how often they appear
        List<Map.Entry<Character, Integer>> sortedFrequencies = new ArrayList<>(frequencyMap.entrySet());
        sortedFrequencies.sort((e1, e2) -> e2.getValue() - e1.getValue());

        // Print the frequencies
        System.out.println("Character Frequencies:");
        for (Map.Entry<Character, Integer> entry : sortedFrequencies) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // Compare the most frequent characters to the language frequencies
        Map<Character, Character> mapping = new HashMap<>();
        System.out.println("\nFrequency Comparison for " + language + ":");
        System.out.printf("%-10s %-10s%n", "Voynich", "Compared to " + language + ":");

        for (int i = 0; i < sortedFrequencies.size(); i++) {
            if (i < languageFrequencies.length) {
                char cipherChar = sortedFrequencies.get(i).getKey();
                char mappedChar = languageFrequencies[i];
                mapping.put(cipherChar, mappedChar);
                System.out.printf("%-10c %-10c%n", cipherChar, mappedChar);
            }
        }

        // Decrypt the text using the frequency mapping
        StringBuilder decryptedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (mapping.containsKey(c)) {
                decryptedText.append(mapping.get(c));
            } else {
                decryptedText.append(c); // Keep characters that don't match
            }
        }

        return decryptedText.toString();
    }

    /**
     * Loads a dictionary for a given language from a file.
     *
     * @param language The language of the dictionary.
     * @param filePath The path to the dictionary file.
     * @throws IOException If there is an error reading the file.
     */
    //Chat gpt helped and https://github.com/joeoakes/javaBruteForceDictionary/ helped
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
     *
     * @param language The language to use for the dictionary.
     * @param text     The decrypted text to check.
     * @return True if the text contains valid dictionary words, false otherwise.
     */
    //https://github.com/joeoakes/javaBruteForceDictionary/----JOE OAKS
    public static boolean checkWithDictionary(String language, String text) {
        Set<String> dictionary = DICTIONARIES.get(language);
        if (dictionary == null) return false;

        String[] words = text.split("\\s+"); // Split text into words
        boolean foundMatch = false;

        for (String word : words) {
            if (dictionary.contains(word)) {
                foundMatch = true; // A match was found
                break;
            }
        }

        if (!foundMatch) {
            System.out.println("No words found in dictionary for " + language + ".");
        }

        return foundMatch;
    }
}