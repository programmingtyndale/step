// Chris Bradshaw
/**************************************************************************************************
 * Copyright (c) 2013, Directors of the Tyndale STEP Project                                      *
 * All rights reserved.                                                                           *
 *                                                                                                *
 * Redistribution and use in source and binary forms, with or without                             *
 * modification, are permitted provided that the following conditions                             *
 * are met:                                                                                       *
 *                                                                                                *
 * Redistributions of source code must retain the above copyright                                 *
 * notice, this list of conditions and the following disclaimer.                                  *
 * Redistributions in binary form must reproduce the above copyright                              *
 * notice, this list of conditions and the following disclaimer in                                *
 * the documentation and/or other materials provided with the                                     *
 * distribution.                                                                                  *
 * Neither the name of the Tyndale House, Cambridge (www.TyndaleHouse.com)                        *
 * nor the names of its contributors may be used to endorse or promote                            *
 * products derived from this software without specific prior written                             *
 * permission.                                                                                    *
 *                                                                                                *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS                            *
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT                              *
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS                              *
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE                                 *
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                           *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,                           *
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;                               *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER                               *
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT                             *
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING                                 *
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF                                 *
 * THE POSSIBILITY OF SUCH DAMAGE.                                                                *
 **************************************************************************************************/

package com.tyndalehouse.step.tools.analysis;

import com.tyndalehouse.step.core.utils.StringConversionUtils;
import com.tyndalehouse.step.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chrisburrell
 */
public class BerkeleyOutputConverter2 {
    private static final Map<String, String> entries = new HashMap<String, String>(12000);
    private static final Map<String, String> greekEntries = new HashMap<String, String>(12000);

    public static void main(String[] args) throws IOException {

        //This file needs some variables set to work properly on one's system. They follow here.
        // Stefan's
        final String root = "C:\\Users\\Stefan Bosman\\Dropbox\\autoTag\\BibleSample\\";
        final String strongs = FileUtils.readFileToString(new File(root + "NT.s"));        // Original Text in Strong Numbers; strongs # in a file for a section of verses; each verse on a new line
        final String other = FileUtils.readFileToString(new File(root + "NT.u"));          // Target Language in Stems Only; stems only -- Done with Paratext?; each verse on a new line
        final String results = FileUtils.readFileToString(new File(root + "NT.training.align")); // Original Language Aligned with Target Language; alignment from Berkeley; each verse on a new line
        final String keyFile = FileUtils.readFileToString(new File(root + "NT.keyList.txt"));    // Book/Chapter/Verse Division as Key; refs only (indicates verses)
        final String strJSwordPath = "C:\\Users\\Stefan Bosman\\AppData\\Roaming\\JSword\\step\\entities\\definition"; //path to the JSword directory
        final String strOutputFileName = "C:\\Users\\Stefan Bosman\\Dropbox\\autoTag\\BibleSample\\outfilename.txt"; //file in which the output is written

/**
 * David's
 final String root = "C:\\Users\\chbradsh\\Documents\\GitHub\\dev\\BibleSample\\";
 final String strongs = FileUtils.readFileToString(new File(root + "NT.s"));        // strongs #
 final String other = FileUtils.readFileToString(new File(root + "NT.u"));          // stems only
 final String results = FileUtils.readFileToString(new File(root + "NT.training.align")); // alignment from Berkeley
 final String keyFile = FileUtils.readFileToString(new File(root + "NT.keyList.txt"));    // refs only
 */
/**
 * Chris'
 final String strongs = FileUtils.readFileToString(new File("c:\\temp\\bible.s"));
 final String other = FileUtils.readFileToString(new File("c:\\temp\\bible.o"));
 final String results = FileUtils.readFileToString(new File("c:\\temp\\training.align"));
 final String keyFile = FileUtils.readFileToString(new File("c:\\temp\\keyList.txt"));
 */

        List<String[]> strongSentences = splitByWord(strongs);
        List<String[]> otherSentences = splitByWord(other);
        List<String[]> resultSentences = splitByWord(results);
        List<String[]> keyList = splitByWord(keyFile);

        final File path = new File(strJSwordPath);
//        final File path = new File("C:\\Users\\David IB\\AppData\\Roaming\\JSword\\step\\entities\\definition");
//        final File path = new File("C:\\Users\\chbradsh\\AppData\\Roaming\\JSword\\step\\entities\\definition");
        FSDirectory directory = FSDirectory.open(path);
        final IndexSearcher indexSearcher = new IndexSearcher(directory);
        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strOutputFileName), "UTF8"));
//        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\David IB\\Dropbox\\STEP-Tagging\\autoTag\\BibleSample\\ChrisExperiments\\NT.tagging+Gk.txt"), "UTF8"));
//        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\chbradsh\\Documents\\GitHub\\dev\\BibleSample\\outfilename.txt"), "UTF8"));

        String resultTagging = parseResults(resultSentences, strongSentences, otherSentences, indexSearcher, keyList, out);
        out.close();
        //Postprocessing
        resultTagging = postStringProcessing (strOutputFileName);
        FileUtils.writeStringToFile(new File(strOutputFileName + "postprocessed.txt"), resultTagging );

    }

    //Stefan 12/01/2016: PostProcesses the output into a better readable format
    private static String postStringProcessing (final String strFileName)  throws IOException {
        //Get Data
        String strOutputFileContents = FileUtils.readFileToString(new File(strFileName)); //read the contents of the output file

        //Process Data
        //-removeDoubleQuotes
        strOutputFileContents = postStringProcessing_removeDoubleQuotes (strOutputFileContents);
        //-TagDifferentTargetWordsTaggedWithSameSourceWord

        int intCountMatches = 0;
        //As long as there are still Matches, keep on Matching: Bad use of resources, will improve hopefully at some point
        while (strOutputFileContents != postStringProcessing_TagDifferentTargetWordsTaggedWithSameSourceWord (strOutputFileContents)) {
            strOutputFileContents = postStringProcessing_TagDifferentTargetWordsTaggedWithSameSourceWord (strOutputFileContents);
            intCountMatches = intCountMatches +1;
        }
        System.out.println("Number of Match-cycles; " + intCountMatches);

        //Return Data
        return strOutputFileContents;
    }

    private static String tester () {
        return "";
    }

    //Stefan 13/01/2016: TagDifferentTargetWordsTaggedWithSameSourceWord
    private static String postStringProcessing_TagDifferentTargetWordsTaggedWithSameSourceWord (String strInput) {
        /*
        * when a Greek word is used to tag more than one Swahili word, it needs to be marked.
        eg in v.16 pais is tagged to both "watoto" and "kiume"  I think a good way to mark this is to preceded all occurrences with an "~"
        (we want to try to get each Greek word occurring only once if poss.)
        eg watoto       ~016-G3816{child= pais}
        wote            014-G3956{all= pas}
        wa              015-G3588{the/this/who=ho}
        */
        //      Init FD
        //      Identify WORD                       :
        //      Identify MATCH
        //      NOT in same verse AND a DIFFERENT WORD

        //Init
        String strIndicatorDoubleOccurrence = "\tDouble Match"; //The tagging of a Double Match

        String strPattern = ""; //   "(?s)";//

        //Sample entry (FULL): 40_Mat.002.016-002		Herode	G2264{Herod=Ἡρώδης}

        //***Match first occurrence
        //Sample entry section: BackRef#1: 40_Mat.002.016
        strPattern = strPattern  + "([0-9]{2}\\_[a-zA-Z]{2,5}\\.[0-9]{3}\\.[0-9]{3})";
        //Sample entry section: BackRef#2: -002\t\t
        strPattern = strPattern  + "(\\-[0-9]{3})\t\t"; //Line, Book#,BookName,Chap,Verse--Storing this for backref, followed by the matching word
        //Sample entry section: BackRef#3: Herode\t (only here when we got a tab and more garbage it won't work, but I donno if I need to catch this too)
        strPattern = strPattern  + "(\\w{2,})"; //Target Language (Swahili) Word--Storing this for backref, maybe followed by garbage, e.g. another word or punctuation
        //Sample entry section: BackRef#4:
        strPattern = strPattern  + "([^\t]{0,}\t)"; //Some pre-tab runnish
        //Sample entry section: BackRef#5: G2264{Herod=Ἡρώδης}
        strPattern = strPattern  + "([A-Z][0-9]{1,5}\\{[^=]{1,}[=][^}]{1,}\\})"; //Match Strong indication--Storing this for backref(\3) (probably could just do [AHG] (Aramaic, Greek, Hebrew)
        //Sample entry section: (Negative Lookahead) & BackRef#6: Possible Garbage at the end of the Original Line
        strPattern = strPattern  + "(?!\\s?" + strIndicatorDoubleOccurrence + ")([^\n]*)[\n]"; //exclude lines that have already been matched
        //Sample entry section: BackRef#7 and #8: Other lines (Note $8 is not referenced later on, on purpose, because it is duplicating)
        strPattern = strPattern  + "(([^\n]*[\n])*)";
        //Rematch with a second occurrence, verse wise
        strPattern = strPattern  + "\\1"; //1st backref
        //Sample entry section: BackRef#9: Different Position in the verse
        strPattern = strPattern  + "(\\-[0-9]{3})\t\t"; //Word Number is different
        //Sample entry section: BackRef#10: Different Target Language Word
        strPattern = strPattern  + "(?!\\3)(\\w{2,})"; //Exclude places with the exact same word //Word is different too
        //Sample entry section: BackRef#11: Different Postword Rubbish
        strPattern = strPattern  + "([^	]{0,}	)";
        //Rematch with a second occurrence, Original Language Match-wise
        strPattern = strPattern  + "\\5"; //The Original stuff is the same. If this is all true, we got a match

        /* Handy for Debug
        Pattern pattern = Pattern.compile(strPattern, Pattern.DOTALL); //, Pattern.MULTILINE  | Pattern.MULTILINE
        Matcher matcher = pattern.matcher(strInput);
        System.out.println("found: " + strPattern + ":::");
        System.out.println("Did we have a match?: " + matcher.find()+ ":::");
        */

        //Initial Match + Additions
        String strReplacePattern = "$1$2\t\t$3$4$5$6" + strIndicatorDoubleOccurrence + ": ($3 [$2], $10 [$9])\n";
        //Inbetween Lines
        strReplacePattern = strReplacePattern + "$7";
        //Counterpart Match + Additions
        strReplacePattern = strReplacePattern + "$1$9\t\t$10$11$5$6" + strIndicatorDoubleOccurrence + ": ($3 [$2], $10 [$9])";
        System.out.println(strReplacePattern);

        //Return altered strInput
        return strInput.replaceAll(strPattern, strReplacePattern);
    }

    //Stefan 13/01/2016: removeDoubleQuotes
    private static String postStringProcessing_removeDoubleQuotes (final String strInput) {
        final String strOriginalExpression = "\""; //We need to get rid of double quotes
        final String strReplacingExpression = ""; //We do not need anything in its stead
        return Pattern.compile(strOriginalExpression).matcher(strInput).replaceAll(strReplacingExpression);
    }

    private static String parseResults(final List<String[]> resultSentences, final List<String[]> strongSentences, final List<String[]> otherSentences, final IndexSearcher indexSearcher, final List<String[]> keyList, final BufferedWriter out) throws IOException {
        StringBuilder resultingTagging = new StringBuilder(8000000);
        int prev;
        prev = -1;

        for (int i = 0; i < resultSentences.size(); i++) {
            String[] sentence = resultSentences.get(i);

            String ref = keyList.get(i)[0];
            if (i % 200 == 0) {
                System.out.println(ref);
            }

            out.write('\n');
            out.write("$");

            prev =-1;
            boolean first = true;
            sentence = reOrder(sentence);
            for (String word : sentence) {

                String[] stringIndexes = word.split("-");

                try {
                    int[] indexes = new int[]{Integer.parseInt(stringIndexes[0]), Integer.parseInt(stringIndexes[1])};
                    if (indexes[0] == 0 && indexes[1] == 0) {      // not sure what this used to be for
                        //            continue;
                    }



                    //find word in sentence in each bible.
                    String strong = strongSentences.get(i)[indexes[0]];
                    String other = otherSentences.get(i)[indexes[1]];
//                    System.out.println("strong " + strong + " " + indexes[0]);
                    if (indexes[1] != prev) {   // add ref
                        out.write("\n");
                        out.write(ref);
                        out.write("-");
                        out.write(String.format("%03d", indexes[0] + 1));
                        out.write("\t");
                    }
                    if (indexes[1]-1 != prev) {   // add words not aligned
                        for (int j = prev+1; j < indexes[1]; j++) {
                            out.write(otherSentences.get(i)[j]);
                            out.write(" ");
                        }
                    }
                    if (indexes[1] != prev) {   // add aligned word
                        out.write("\t");
                        out.write(other);
                        out.write("\t");
                    }
                    prev = indexes[1];
                    out.write(strong);
                    out.write("{");

                    appendLexicalEntry(indexSearcher, strong, out);
                    out.write("=");
                    appendGreekEntry(indexSearcher, strong, out);
                    out.write("} ");

                    //add next Greek word(s) if not tagged
                    int testStrong;
                    boolean missingGreek;
                    String checkMissing;
                    if (first){
                        first = false;
                        for (int l=0; l < indexes[0]; l++){
                            missingGreek = true;
                            checkMissing = Integer.toString(l) + "-";
                            for (int m = 0; m < sentence.length; m++) {
                                if (sentence[m].startsWith(checkMissing)) {
                                    missingGreek = false;
                                    break;
                                }
                            }
                            if (missingGreek) {
                                String missingStrong = strongSentences.get(i)[l];
                                out.write("\n");
                                out.write(ref);
                                out.write("-");
                                out.write(String.format("%03d", l+1));
                                out.write("\t");
                                out.write("\t\t" + missingStrong);
                                out.write("{");

                                appendLexicalEntry(indexSearcher, missingStrong, out);
                                out.write("=");
                                appendGreekEntry(indexSearcher, missingStrong, out);
                                out.write("} ");
                            }
                        }

                    }

                    for (int n=indexes[0]+1; n < strongSentences.get(i).length; n++){
//                        System.out.println(n+1);
                        missingGreek = true;
                        testStrong = n;
                        checkMissing = Integer.toString(testStrong) + "-";
                        for (int k = 0; k < sentence.length; k++) {
                            if (sentence[k].startsWith(checkMissing)) {
                                missingGreek = false;
                                break;
                            }
                        }
                        if (!missingGreek) break;
                        String missingStrong = strongSentences.get(i)[testStrong];
                        out.write("\n");
                        out.write(ref);
                        out.write("-");
                        out.write(String.format("%03d", testStrong+1));
                        out.write("\t");
                        out.write("\t\t" + missingStrong);
                        out.write("{");

                        appendLexicalEntry(indexSearcher, missingStrong, out);
                        out.write("=");
                        appendGreekEntry(indexSearcher, missingStrong, out);
                        out.write("} ");
                    }

                } catch (Exception e) {
                    System.out.println("Error in verse " + ref + " for word: " + word);
                    System.out.println(e.getMessage());
                }
            }


            // get unaligned end of sentence
            int otherLength = otherSentences.get(i).length;
            if (prev < otherLength) {
                out.write("\n");
                out.write(ref);
                out.write("-999");
                out.write("\t");

                for (int j = prev+1; j <  otherLength; j++) {
                    out.write(otherSentences.get(i)[j]);
                    out.write(" ");
                }
                out.write("\t\t~");
            }
        }
        return resultingTagging.toString();
    }

    private static String[] reOrder(final String[] sentence) {
        List<String> words = Arrays.asList(sentence);

        Collections.sort(words, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                if (o1 == null || o1.length() == 0) {
                    return 1;
                }

                if (o2 == null || o2.length() == 0) {
                    return -1;
                }


                return ((Integer) Integer.parseInt(o1.split("-")[1])).compareTo(Integer.parseInt(o2.split("-")[1]));
            }
        });

        return words.toArray(new String[words.size()]);
    }

    private static void appendLexicalEntry(final IndexSearcher indexSearcher, String strong, BufferedWriter out) throws IOException {
        if (strong.length() > 5 && strong.charAt(1) == '0') {
            strong = strong.substring(0, 1) + strong.substring(2);
        }

        String gloss = entries.get(strong);
        if (gloss == null) {

            final TopDocs lexicalEntries = indexSearcher.search(new TermQuery(new Term("strongNumber", StringConversionUtils.getStrongPaddedKey(strong))), Integer.MAX_VALUE);
            if (lexicalEntries.scoreDocs.length > 0) {
                gloss = indexSearcher.doc(lexicalEntries.scoreDocs[0].doc).get("stepGloss");
            } else {
                gloss = "";
            }
            entries.put(strong, gloss);
        }
        out.write(gloss);
    }

    private static void appendGreekEntry(final IndexSearcher indexSearcher, String strong, final BufferedWriter out) throws IOException {
        if (strong.length() > 5 && strong.charAt(1) == '0') {
            strong = strong.substring(0, 1) + strong.substring(2);
        }

        String greek = greekEntries.get(strong);
        if (greek == null) {

            final TopDocs lexicalEntries = indexSearcher.search(new TermQuery(new Term("strongNumber", StringConversionUtils.getStrongPaddedKey(strong))), Integer.MAX_VALUE);
            if (lexicalEntries.scoreDocs.length > 0) {
                greek = indexSearcher.doc(lexicalEntries.scoreDocs[0].doc).get("accentedUnicode");
            } else {
                greek = "";
            }
            greekEntries.put(strong, greek);
        }
        out.write(greek);
    }

    private static List<String[]> splitByWord(final String strongs) {
        final String[] sentences = strongs.split("\r?\n");
        List<String[]> sss = new ArrayList<String[]>(64000);


        for (String sentence : sentences) {
            final String[] split = org.apache.commons.lang3.StringUtils.split(sentence, ' ');
            sss.add(split);
        }

        return sss;
    }
}
