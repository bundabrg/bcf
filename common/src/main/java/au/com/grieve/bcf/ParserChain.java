/*
 * Copyright (c) 2020-2022 Brendan Grieve (bundabrg) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package au.com.grieve.bcf;

import au.com.grieve.bcf.exception.EndOfLineException;

import java.util.List;

public interface ParserChain {
    /**
     * Return the list of parsers that make up this parser chain
     * @return List of Parsers
     */
    List<Parser<?>> getParsers();

    /**
     * Parse line
     * @param line Input line to parse into objects
     * @param output Output data
     */
    void parse(ParsedLine line, List<Object> output) throws EndOfLineException;

    /**
     * Provide completions for the parsed line
     * @param line Input line to parse
     * @param candidateGroups Completion Candidate Groups
     */
    void complete(ParsedLine line, List<CompletionCandidateGroup> candidateGroups) throws EndOfLineException;


}
