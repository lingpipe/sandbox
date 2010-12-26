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

package com.aliasi.lingmed.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.*;

/**
 * Helper routines for logging
 *
 * @author Mitzi Morris
 * @author Breck Baldwin
 * @version 1.1
 * @since   LingMed1.0
 */

public class Logging {



    // returns String containing formatted StackTrace for logger
    public static String logStackTrace(Throwable t) {
	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	PrintStream pOut = new PrintStream(bOut);
	t.printStackTrace(pOut);
	return bOut.toString();
    }

    private Logging() {}
}
