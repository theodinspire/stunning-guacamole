package com.theodinspire;

import java.io.*;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eric T Cormack on 3/29/17.
 *
 * An application for counting tokens
 */
public class Tokenizer {
    private int paragraphCount;
    private int sentenceCount;
    private List<String> tokens;
    private Map<String, Integer> distribution;
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("# of paragraphs = ");   builder.append(this.paragraphCount);        builder.append(System.lineSeparator());
        builder.append("# of sentences = ");    builder.append(this.sentenceCount);         builder.append(System.lineSeparator());
        builder.append("# of tokens = ");       builder.append(this.tokens.size());         builder.append(System.lineSeparator());
        builder.append("# of types = ");        builder.append(this.distribution.size());   builder.append(System.lineSeparator());
    
        builder.append("\n================================\n");
        for (String token : this.getSortedDistinctTokens()) {
            builder.append(token);                          builder.append(" ");
            builder.append(this.distribution.get(token));   builder.append(System.lineSeparator());
        }
        
        return builder.toString();
    }
    
    private Tokenizer(String content) {
        paragraphCount = countParagraphs(content);
        sentenceCount = countSentences(content);
        tokens = tokenize(content);
        distribution = buildDistribution(tokens);
    }
    
    private List<String> getSortedDistinctTokens() {
        List<String> tokenList = new LinkedList<>(distribution.keySet());
        tokenList.sort((String a, String b) -> {
            if (distribution.get(a).equals(distribution.get(b))) return a.compareTo(b);
            else return distribution.get(b).compareTo(distribution.get(a));
        });
        
        return tokenList;
    }
    
    private static Map<String, Integer> buildDistribution(List<String> tokens) {
        Map<String, Integer> distro = new HashMap<>();
        
        for (String token : tokens) {
            if (distro.containsKey(token)) distro.put(token, distro.get(token) + 1);
            else distro.put(token, 1);
        }
        
        return distro;
    }
    
    private static List<String> tokenize(String content) {
        if (content == null) return new LinkedList<>();
        
        //  Clitics
        //content = content.replaceAll("(?<![\\w-])won't(?![\\w-])", "will not");
        //content = content.replaceAll("(?<![\\w-])can't(?![\\w-])", "cannot");
        content = content.replaceAll("n't(?![\\w-])", " not");
        content = content.replaceAll("'ll(?![\\w-])", " will");
        content = content.replaceAll("'ve(?![\\w-])", " have");
        content = content.replaceAll("'d(?![\\w-])", " would");
        content = content.replaceAll("'re(?![\\w-])", " are");
        //content = content.replaceAll("(?<=([Ss]?[Hh]e|[Ii]t)|[TtWw]?[Hh]ere)'s(?![\w-])", " is");
        content = content.replaceAll("(?<=([Ss]?[Hh]e|[Ii]t))'s(?![\\w-])", " is");
        content = content.replaceAll("(?<=\\w)'s(?![\\w-])", " 's");
        content = content.replaceAll("I'm(?![\\w-])", "I am");
        
        //  Punctuation
        // Quotes
        content = content.replace("\"", " \" ");
        // Preceding tick
        content = content.replaceAll("(?<!\\w)'(?![\\W(s\\s)])", " ' ");
        // Following tick
        content = content.replaceAll("(?<=\\w)'(?!\\w)", " ' ");
        // Parentheses
        content = content.replace("(", " ( ");
        content = content.replace(")", " ) ");
        // Curly braces
        content = content.replace("{", " { ");
        content = content.replace("}", " } ");
        // Square braces
        content = content.replace("[", " [ ");
        content = content.replace("]", " ] ");
        
        // Trailing full stop
        content = content.replaceAll("\\.(?!\\w)", " . ");
        // Leading full stop
        content = content.replaceAll("(?<!\\w)\\.", " . ");
        // Comma
        content = content.replaceAll(",(?!\\d)", " , ");
        // Semicolon
        content = content.replace(";", " ; ");
        // Colon
        content = content.replace(":", " : ");
        // Bang
        content = content.replace("!", " ! ");
        // Query
        content = content.replace("?", " ? ");
        // Dollar sign
        content = content.replaceAll("\\s*\\$(?=\\d)", " \\$ ");
        // That fake em-dash
        content = content.replace("--", " -- ");
        // Slash
        content = content.replaceAll("(?<!\\bw)/", " / ");
        // Star
        content = content.replace("*", " * ");
        // Tilde
        content = content.replace("~", " ~ ");
        
        //  Test
        //System.out.println(content);
        
        return Arrays.asList(content.split("[\\s ]+"));
    }
    
    private static int countParagraphs(String content) {
        return countRegex(content, "^|(\\n\\n[^\\n]\\S*.+)");
    }
    
    private static int countSentences(String content) {
        BreakIterator breaker = BreakIterator.getSentenceInstance(Locale.US);
        breaker.setText(content);
        
        int count = 0;
        
        for (int end = breaker.next(); end != BreakIterator.DONE; end = breaker.next()) {
            //System.out.println(content.substring(start, end));
            ++count;
        }
        
        return count;
    }
    
    private static int countRegex(String content, String regex) {
        int count = 0;
        Matcher matcher = Pattern.compile(regex).matcher(content);
        
        while (matcher.find()) {
            ++count;
        }
        
        return count;
    }
    
    private static Tokenizer fromFile(String filename) {
        String contents = "";
    
        try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
            StringBuilder builder = new StringBuilder();
        
            for (String line = input.readLine(); line != null; line = input.readLine()) {
                builder.append(line);
                builder.append("\n");
            }
        
            contents = builder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return new Tokenizer(contents);
    }
    
    
    //  Main
    public static void main(String[] args) {
        String inputFilename;
        String outputFilename;
        
        if (args.length >= 1) inputFilename = args[0];
        else {
            System.out.println("Correct entry format is \"java Tokenizer [input path] [output path]\"");
            return;
        }
        
        if (args.length >= 2) outputFilename = args[1];
        else outputFilename = "output.txt";
        
        
        Tokenizer tokenizer = Tokenizer.fromFile(inputFilename);
        String output = tokenizer.toString();
        
        System.out.println(output);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            writer.write(output);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
