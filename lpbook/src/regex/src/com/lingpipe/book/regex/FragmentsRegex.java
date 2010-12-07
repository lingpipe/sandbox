package com.lingpipe.book.regex;

import java.util.regex.*;
import static java.util.regex.Pattern.DOTALL;

class FragmentsRegex {

    void abc1() {
        /*x FragmentsRegex.1 */
        Pattern.compile(".").matcher("A").matches();
        Pattern.compile(".",DOTALL).matcher("\n").matches();
        /*x*/

        /*x FragmentsRegex.2 */
        Pattern.compile(".").matcher("\n").matches();
        /*x*/

    }

}