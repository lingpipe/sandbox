package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * A {@code MeshRecordOriginatorsList} contains information on
 * one or more of the originator, maintainer or authorizer.  See
 * the individual method documentation for details.
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshRecordOriginatorsList {

    private final String mOriginator;
    private final String mMaintainer;
    private final String mAuthorizer;

    MeshRecordOriginatorsList(String originator,
                              String maintainer,
                              String authorizer) {
        mOriginator = originator;
        mMaintainer = maintainer.length() == 0 ? null : maintainer;
        mAuthorizer = authorizer.length() == 0 ? null : authorizer;
    }

    /**
     * Returns the three-character user name for the NLM editor who
     * created the original record for a concept.
     *
     * @return Three-character name for the originator.
     */
    public String originator() {
        return mOriginator;
    }

    /**
     * Returns the three-character user name for the NLM editor who
     * most recently modified a record.
     *
     * @return Three-character name for the most recent modifier.
     */
    public String maintainer() {
        return mMaintainer;
    }

    /**
     * Returns the three-character user name for the NLM editor or
     * supervisor who authorized the record.
     *
     * @return Three-character name for the authorizer.
     */
    public String authorizer() {
        return mAuthorizer;
    }

    /**
     * Returns a string-based representation of the record
     * originator, maintainer and authorizer.
     *
     * @return String-based representation of originators.
     */
    @Override
    public String toString() {
        return "Originator=" + mOriginator
            + "; Maintainer=" + mMaintainer
            + "; Authorizer=" + mAuthorizer;
    }

    static class Handler extends BaseHandler<MeshRecordOriginatorsList> {
        final TextAccumulatorHandler mOriginatorHandler;
        final TextAccumulatorHandler mMaintainerHandler;
        final TextAccumulatorHandler mAuthorizerHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mOriginatorHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RECORD_ORIGINATOR_ELEMENT,
                        mOriginatorHandler);
            mMaintainerHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RECORD_MAINTAINER_ELEMENT,
                        mMaintainerHandler);
            mAuthorizerHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RECORD_AUTHORIZER_ELEMENT,
                        mAuthorizerHandler);
        }
        @Override
        public void reset() {
            mOriginatorHandler.reset();
            mMaintainerHandler.reset();
            mAuthorizerHandler.reset();
        }
        public MeshRecordOriginatorsList getObject() {
            return new MeshRecordOriginatorsList(mOriginatorHandler.getText().trim(),
                                                 mMaintainerHandler.getText().trim(),
                                                 mAuthorizerHandler.getText().trim());
        }
    }


}

