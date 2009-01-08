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

package com.aliasi.lingmed.lucene;

/**
 * Standard field names for Lucene indexes.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
public class Fields {

    public static final String ID_FIELD = "ID";
    public static final String DEFAULT_FIELD = "ID";
    public static final String XML_FIELD = "rawXML";

    public static final String RAW_TEXT_FIELD = "rawTEXT";

    public static final String LAST_FILE_FIELD = "LASTFILE";
    public static final String LAST_FILE_VALUE = "lastfile";
    public static final String FILE_NAME_FIELD = "FILENAME";

    private Fields() {}

}
