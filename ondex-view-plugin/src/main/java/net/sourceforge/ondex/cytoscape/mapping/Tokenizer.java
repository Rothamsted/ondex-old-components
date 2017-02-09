/*
 * OndexView plug-in for Cytoscape
 * Copyright (C) 2010  University of Newcastle upon Tyne
 * 
 * This file is part of OndexView.
 * 
 * OndexView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OndexView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OndexView.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ondex.cytoscape.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 26-Nov-2009
 * Time: 12:38:52
 * To change this template use File | Settings | File Templates.
 */
public class Tokenizer {
    public List<String> tokens(CharSequence chars)
    {
        List<String> tokens = new ArrayList<String>();

        StringBuilder word = new StringBuilder();
        boolean inQuote = false;
        for(int i = 0; i < chars.length(); i++)
        {
            char c = chars.charAt(i);
            switch (c)
            {
                case ' ':
                case '\t':
                case '\n':
                    if (inQuote)
                    {
                        word.append(c);
                    }
                    else
                    {
                        addToken(tokens, word);
                    }
                    break; // skip whitespace
                case '"':
                    inQuote = !inQuote;
                    break;
                default:
                    word.append(c);
            }
        }

        addToken(tokens, word);

        return tokens;
    }

    private void addToken(List<String> tokens, StringBuilder word)
    {
        if(word.length() > 0)
        {
            tokens.add(word.toString());
            word.setLength(0);
        }
    }
}
