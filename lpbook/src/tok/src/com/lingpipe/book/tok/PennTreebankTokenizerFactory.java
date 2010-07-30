package com.lingpipe.book.tok;

public class PennTreebankTokenizerFactory
    extends ModifyTokenTokenizerFactory {

    public static TokenizerFactory INSTANCE
        = new PennTreebankTokenizerFactory();

    private PennTreebankTokenizerFactory() {
        super(new RegExTokenizerFactory(BASE_REGEX));
    }

    public String modifyToken(String token) {
        String modToken = REPLACE_MAP.get(token);
        return modToken == null
            ? token
            : modToken;
    }


}