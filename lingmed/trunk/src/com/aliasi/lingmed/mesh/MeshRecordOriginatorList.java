package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * A {@code MeshRecordOriginatorsList} 
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

    public String originator() {
        return mOriginator;
    }

    public String maintainer() {
        return mMaintainer;
    }

    public String authorizer() {
        return mAuthorizer;
    }

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
            return new MeshRecordOriginatorList(mOriginatorHandler.getText().trim(),
                                                mMaintainerHandler.getText().trim(),
                                                mAuthorizerHandler.getText().trim());
        }
    }


}

