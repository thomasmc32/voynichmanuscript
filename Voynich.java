/**
 * Project: Voynich Manuscript
 * Course: IST 242
 * Author: Thomas McConnell
 * Date Developed: 11/24/24
 * Last Date Changed: 11/26/24
 */
//websites i used to get the manuscript, some info, and the table to translate to english letters \/
//https://collections.library.yale.edu/catalog/2002046
//https://en.m.wikipedia.org/wiki/Voynich_manuscript#/media/File%3AVoynich_EVA.svg
//https://en.m.wikipedia.org/wiki/Voynich_manuscript
import java.util.*;

public class Voynich {

    /**
     * Main method to run the program.
     * It processes the ciphertext, analyzes letter frequencies, and applies decryption methods.
     *
     * @param args Command-line arguments.
     */

    public static void main(String[] args) {
        // Ciphertext to be deciphered
        String cipherText = "Tchiuy oFaiis chejaiin dxeedy qoPchegay keodaiiu otayaiin oar\n" +
                "dar cheody okaiin odar okal okair oky daiir qotas okar olaiio\n" +
                "todal eholky qokal shdy qoky otody qokyeea qokair pasy \n" +
                "cnhor olkeedy olkchdy cheedy kaichdy chedaiib or choi karam \n" +
                "daiib chekas olkaiib olkeody ykaiib otaib lar okeedy ykaso \n" +
                "haiib chaky qoechdy otaly chedy dal dy chfhaiib chka qof\n" +
                "pchedar osaiib eeeos kas or als acocthey olkaib aly\n" +
                "yty qokaiub ykal chdy qoky osaib chy kaidy daiy\n" +
                "otar chdy dytchdy";

        // "Clean" the ciphertext (remove spaces and make lowercase)
        String ptext = cipherText.toLowerCase().replaceAll("[^a-z]", "");
        System.out.println("\ni couldn't find a version of the manuscript that was translated using the little table on wikipedia you can see what" +
                "im talking about on the image labeled 'what i used.jpeg' so i had to write out my whole page (page 94) by hand and compare each letter they " +
                "used with the english alphebet. i used frequency analysis to get how frequent they use each letter then compared them to our most used." +
                "when i did the analysis i got nothing back so i thought maybe it was shifted as well. so i did brute force with the ceasear cipher and looked at each one " +
                "to see if any made sense. this still got me nothing back. i then thought maybe if i do it in italian since the origin is from italy maybe that would work" +
                "but it wouldn't the letters i mapped to each letter from the manuscript was english so unless i wanted to completely do it over again using the italian alphebet" +
                "then it wouldn't work, also i did think about redoing it in italian but i couldn't find anything like what i used like on the 'what i used.jpeg' capture" +
                "i also put a bunch of examples of what it looked like when i was writing each letter from the manuscript into the english letter. ");
                //^^ this is just me explaining my thought process i was gonna turn in a seperate document but didnt want you to have to open anything extra

        // Perform frequency analysis to map common letters
        //https://github.com/joeoakes/javaBruteForceFreqAnalysis
        System.out.println("\nFrequency Analysis Results:");
        String frequencyDecryptedText = performFrequencyAnalysis(ptext);
//
        // Perform Caesar cipher brute-force decryption on the frequency analysis result
        //Security Hashing, HMAC, Brute Force, AES, TLS Presentation Joe Oaks  (BRUTE FORCE TO BE EXACT)
        System.out.println("\nBrute Force Attempts on Frequency Analysis:");
        for (int shift = 0; shift < 26; shift++) {
            String decrypted = caesarDecrypt(frequencyDecryptedText, shift);
            System.out.println("Shift " + shift + ": " + decrypted);
        }
    }

    /**
     * Decrypts text using a Caesar cipher shift.
     *
     * @param text  The text to decrypt.
     * @param shift The shift amount for the cipher.
     * @return The decrypted text.
     */
    //https://github.com/joeoakes/javaCaesarCipher \/
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
     * Analyzes letter frequencies in the text and tries to map them to common English letters.
     *
     * @param text The text to analyze.
     * @return A decrypted version of the text based on frequency analysis.
     */
    //    \/ https://github.com/joeoakes/javaBruteForceFreqAnalysis/blob/main/CaesarCipherFrequencyAnalysis.java
    public static String performFrequencyAnalysis(String text) {
        // Common English letter frequencies, most frequent first
        char[] englishLetterFrequency = {'e', 't', 'a', 'o', 'i', 'n', 's', 'h', 'r', 'd', 'l', 'c', 'u', 'm', 'w', 'f', 'g', 'y', 'p', 'b', 'v', 'k', 'j', 'x', 'q', 'z'};

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

        // mapping the most common letters in the text to common English letters
        System.out.println("\nVoynich to English Leters using Frequency Analysis:");
        Map<Character, Character> mapping = new HashMap<>();
        for (int i = 0; i < sortedFrequencies.size(); i++) {
            if (i < englishLetterFrequency.length) {
                mapping.put(sortedFrequencies.get(i).getKey(), englishLetterFrequency[i]);
                System.out.println(sortedFrequencies.get(i).getKey() + " -> " + englishLetterFrequency[i]);
            }
        }

        // Decrypt the text using the frequency map
        System.out.println("\nDecrypted Text (Frequency Analysis):");
        StringBuilder decryptedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (mapping.containsKey(c)) {
                decryptedText.append(mapping.get(c));
            } else {
                decryptedText.append(c); // Keep characters that don't match
            }
        }
        String result = decryptedText.toString();
        System.out.println(result);

        return result;

    }
}