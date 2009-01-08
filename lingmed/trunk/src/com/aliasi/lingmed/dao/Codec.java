/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.dao;

import com.aliasi.lingmed.lucene.LuceneAnalyzer;

import org.apache.lucene.document.Document;

/**
 * A <code>Codec</code> converts between documents and 
 * objects of the generic type parameter and visa versa.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
public interface Codec<E> {

   /**
    * Converts an object of the generic type to a Lucene document.
    */
    public Document toDocument(E e);
 
    /**
     * Converts a Lucene document to an object of the generic type.
     */
    public E toObject(Document d);


    /**
     * Return the Lucene analyzer for this codec.
     *
     * @return The Lucene analyzer for this codec.
     */
    public LuceneAnalyzer getAnalyzer();

}