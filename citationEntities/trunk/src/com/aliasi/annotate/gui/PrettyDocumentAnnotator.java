package com.aliasi.annotate.gui;

// THIS PROJECT
import com.aliasi.annotate.corpora.AnnotatorCorpusParser;

// LINGPIPE
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Streams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.ScrollPaneConstants.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class PrettyDocumentAnnotator extends JPanel {
    static final long serialVersionUID = 8189242060621377692L;
    static final char[] BOUNDARY_CHAR = { '\u2006' };
    static final String BOUNDARY_SPACE = new String(BOUNDARY_CHAR);

    static final int TOKS_PER_LINE = 12;

    final File mInputFile;
    private final String mInputCharset;
    private final File mOutputFile;
    private final String mOutputCharset;
    private final TokenizerFactory mTokenizerFactory;

    private final String[] mSpanTags;
    private final Set<String> mSpanTagSet = new HashSet<String>();

    private final String[] mChunkTypes;
    private final Set<String> mChunkTypeSet = new HashSet<String>();

    private final String[] mContinueTags;
    private final String[] mInitialTags;

    private Document mDocument;
    private Element[] mSpans;
    private String[][] mTokens;
    private String[][] mWhitespaces;
    private String[][] mTags;

    private TagSelector[][] mTagSelectors;
    private JTextArea[][] mLabels;

    // JPanel this;
    private final JPanel mAnnotationPane;
    private final JButton mCommitButton;
    private final JButton mRevertButton;
    private final JButton mAutoAnnotateButton;
    private final JButton mDiscardButton;

    private final CorpusAnnotator mCorpusAnnotator;

    private final long mStartTime;

    boolean mHasBeenEdited = false;

    public PrettyDocumentAnnotator(CorpusAnnotator corpusAnnotator,
                             File input,
                             String inputCharset,
                             File output,
                             String outputCharset,
                             TokenizerFactory factory,
                             String[] spanTags,
                             String[] chunkTypes)
        throws IOException, JDOMException, SAXException {


        super(new GridBagLayout());

        mCorpusAnnotator = corpusAnnotator;  // may be null
        mInputFile = input;
        mInputCharset = inputCharset;
        mOutputFile = output;
        mOutputCharset = outputCharset;
        mTokenizerFactory = factory;

        Arrays.sort(spanTags);
        mSpanTags = spanTags;
        for (String spanTag : spanTags)
            mSpanTagSet.add(spanTag);

        Arrays.sort(chunkTypes);
        mChunkTypes = chunkTypes;
        for (String chunkType : chunkTypes)
            mChunkTypeSet.add(chunkType);
        mContinueTags = new String[chunkTypes.length + 2];
        mContinueTags[0] = CONTINUE_TAG_LABEL;
        mContinueTags[1] = OUT_TAG_LABEL;
        System.arraycopy(chunkTypes,0,mContinueTags,2,mChunkTypes.length);

        mInitialTags = new String[chunkTypes.length + 1];
        System.arraycopy(mContinueTags,1,mInitialTags,0,
                         mInitialTags.length);

        mAnnotationPane = new JPanel();
        if (!readDocument())
            throw new IOException("Could not read document.");
        System.out.println("read document: " + input.getName());
        System.out.flush();

        JScrollPane annoScrollPane = new JScrollPane(mAnnotationPane);
        annoScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        ActionListener commitAction  = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { finish(); }
                    catch (Exception e2) {
                        e2.printStackTrace(System.out);
                    }
                }
            };
        mCommitButton = new JButton("Commit  (Ctrl+S)");
        mCommitButton.addActionListener(commitAction);

        KeyStroke ctrlS
            = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                     KeyEvent.CTRL_DOWN_MASK);

        mRevertButton = new JButton("Revert");
        mRevertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mHasBeenEdited) {
                        Object[] options = { "OK. Revert.",
                                             "Cancel." };
                        String msg = "Reverting to file on disk deletes uncommitted edits on current file.";
                        int n = JOptionPane.showOptionDialog(mCorpusAnnotator.mTopFrame,
                                                             msg,
                                                             "Confirm Revert",
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             options,
                                                             options[0]);
                        if (n != JOptionPane.YES_OPTION) return;
                    }
                    revert();
                }
            });

        ActionListener autoAnnotateAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mHasBeenEdited) {
                        Object[] options = { "OK. Auto Annotate.",
                                             "Cancel." };
                        String msg = "Auto annotating file deletes uncommitted edits on current file.";
                        int n = JOptionPane.showOptionDialog(mCorpusAnnotator.mTopFrame,
                                                             msg,
                                                             "Confirm Auto Annotate",
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             options,
                                                             options[0]);
                        if (n != JOptionPane.YES_OPTION) return;
                    }
                    autoAnnotate();
                }
            };
        KeyStroke ctrlE
            = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                     KeyEvent.CTRL_DOWN_MASK);

        mAutoAnnotateButton = new JButton("Auto Annotate  (Ctrl+E)");
        mAutoAnnotateButton.addActionListener(autoAnnotateAction);

        ActionListener discardAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (mHasBeenEdited) {
                            Object[] options = { "OK. Discard Edits.",
                                                 "Cancel." };
                            String msg = "Discarding deletes all current edits.  It does not remove the file from disk.";
                            int n = JOptionPane.showOptionDialog(mCorpusAnnotator.mTopFrame,
                                                                 msg,
                                                                 "Confirm Discard",
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.WARNING_MESSAGE,
                                                                 null,
                                                                 options,
                                                                 options[0]);
                            if (n != JOptionPane.YES_OPTION) return;
                        }
                        discard();
                    } catch (InterruptedException e2) {
                        // do nothing
                    }
                }
            };
        KeyStroke del
            = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                                     0); // 0 == no modifiers
        mDiscardButton = new JButton("Discard   (Del)");
        mDiscardButton.addActionListener(discardAction);

        this.add(new JLabel(mInputFile.getName()),
                 new GridBagConstraints(0,0, 2,1, 1.0,0.0,
                                        GridBagConstraints.NORTH,
                                        GridBagConstraints.HORIZONTAL,
                                        new Insets(5,5,5,5),
                                        0,0));
        this.add(annoScrollPane, 
                 new GridBagConstraints(0,1, 2,1, 1.0,1.0,
                                        GridBagConstraints.NORTHWEST,
                                        GridBagConstraints.BOTH,
                                        new Insets(5,5,5,5),
                                        0,0));                      
        this.add(mCommitButton,
                 new GridBagConstraints(0,2, 2,1, 1.0,0.0,
                                        GridBagConstraints.NORTH,
                                        GridBagConstraints.HORIZONTAL,
                                        new Insets(5,5,0,5),
                                        10,10));
        this.add(mRevertButton,
                 new GridBagConstraints(0,3, 1,1, 1.0,0.0,
                                        GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL,
                                        new Insets(5,5,5,5),
                                        0,0));
        this.add(mAutoAnnotateButton,
                 new GridBagConstraints(1,3, 1,1, 1.0,0.0,
                                        GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL,
                                        new Insets(5,5,5,5),
                                        0,0));
        this.add(mDiscardButton,
                 new GridBagConstraints(0,4, 2,1, 1.0,0.0,
                                        GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL,
                                        new Insets(5,5,5,5),
                                        0,0));


        this.registerKeyboardAction(commitAction,
                                    "commit",
                                    ctrlS,
                                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(autoAnnotateAction,
                                    "auto annotate",
                                    ctrlE,
                                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(discardAction,
                                    "discard",
                                    del,
                                    JComponent.WHEN_IN_FOCUSED_WINDOW);


        mStartTime = System.currentTimeMillis();
    }

    void autoAnnotate() {
        mCommitButton.setEnabled(false);
        mRevertButton.setEnabled(false);
        mAutoAnnotateButton.setEnabled(false);
        mDiscardButton.setEnabled(false);
        autoAnnotateDocument();
        mCommitButton.setEnabled(true);
        mRevertButton.setEnabled(true);
        mAutoAnnotateButton.setEnabled(true);
        mDiscardButton.setEnabled(true);
        revalidate();
        focusNextEntity();
        revalidate();
    }

    // autoAnnotateDocument replaces default tag (out-tag)
    // with tags according to current LM.
    void autoAnnotateDocument() {
        System.out.println("autoAnnotate: " + mInputFile.getName() + " # spans: " + mSpans.length);
        System.out.flush();
        for (int i = 0; i < mSpans.length; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < mTokens[i].length; ++j) {
                sb.append(mWhitespaces[i][j]);
                sb.append(mTokens[i][j]);
            }
            // don't need final whitespace
            Chunking chunking = mCorpusAnnotator.mAutoChunker.chunk(sb);
            if (chunking == null) {
                chunking = new com.aliasi.chunk.ChunkingImpl(sb);
            }
            int idx = 0;

            BioTagChunkCodec codec = new BioTagChunkCodec(mTokenizerFactory,true);
            StringTagging tagging = null;
            try {
                tagging = codec.toStringTagging(chunking);
            } catch (IllegalArgumentException e) {
                System.out.println("cannot auto-annotate");
                System.out.flush();
                break;
            }
            List<String> tagList = tagging.tags();
            String[] tags = new String[tagList.size()];
            mTags[i] = tagList.toArray(tags);
            tagsToComboBoxLabels(mTags[i]);
        }
        updateGUI();
        System.out.println("done auto-annotating");
        System.out.flush();
    }

    void focusNextEntity() {
        focusNextEntity(0,0);
    }

    boolean atLeastOneEntity() {
        for (int i = 0; i < mTagSelectors.length; ++i)
            for (int j = 0; j < mTagSelectors[i].length; ++j) {
                if (!OUT_TAG_LABEL.equals(mTagSelectors[i][j].getSelection()))
                    return true;
            }
        return false;
    }

    void focusNextEntity(int iStart, int jStart) {
        //        System.out.println("focusNextEntity");
        if (!atLeastOneEntity()) {
            TagSelector selector = mTagSelectors[0][0];
            selector.requestFocus();
            //            System.out.println("no entities");
            //            System.out.println("focus: 0,0");
            //            System.out.flush();
            return;
        }
        int i = iStart;
        int j = jStart;
        // skip to next tag, if any
        while (i < mTagSelectors.length) {
            if (mTagSelectors[i] == null) return;
            while (j < mTagSelectors[i].length) {
                String selection = mTagSelectors[i][j].getSelection();
                if (!OUT_TAG_LABEL.equals(selection)
                    && !CONTINUE_TAG_LABEL.equals(selection)) {
                    TagSelector selector = mTagSelectors[i][j];
                    selector.requestFocus();
                    //                    System.out.println("focus: i,j: " + i + ", " + j);
                    //                    System.out.flush();
                    return;
                }
                ++j;
            }
            j = 0;
            ++i;
        }
    }

    static void tagsToComboBoxLabels(String[] tags) {
        for (int i = 0; i < tags.length; ++i)
            tags[i] = tagToComboBoxLabel(tags[i]);
    }

    static String tagToComboBoxLabel(String tag) {
        if (tag.equals("O"))
            return OUT_TAG_LABEL;
        if (tag.startsWith("B_"))
            return tag.substring(2);
        return "...";
    }

    // read document, set tags on annotation controls
    boolean readDocument()
        throws IOException, SAXException, JDOMException {
        SAXBuilder builder = new SAXBuilder();

        InputStream in = null;
        InputStreamReader reader = null;
        try {
            in = new FileInputStream(mInputFile);
            reader = new InputStreamReader(in,mInputCharset);
            mDocument = builder.build(in);
        } catch (Exception e) {
            String msg = "There was an exception building document.";
            JOptionPane.showMessageDialog(mCorpusAnnotator.mTopFrame,
                                          "Exception parsing document="
                                          + mInputFile
                                          + " using encoding=" + mInputCharset
                                          + ".  \nSee console for more info.");
            mAnnotationPane.removeAll();
            System.out.println("readDocument() exception=" + e);
            e.printStackTrace(System.out);
            mDocument = null;
            return false;
        } finally {
            Streams.closeQuietly(reader);
            Streams.closeQuietly(in);
        }

        // walk over XML Document, get elements with tag "chunk" with attribute "type", value in ChunkTypeSet
        List<Element> elementList = new ArrayList<Element>();
        Iterator descIt = mDocument.getDescendants();
        while (descIt.hasNext()) {
            Object content = descIt.next();
            if (isSpan(content))
                elementList.add((Element)content);
        }
        // now we've extracted parts of Document to annotate
        mSpans = elementList.<Element>toArray(new Element[elementList.size()]);
        mTokens = new String[mSpans.length][];
        mWhitespaces = new String[mSpans.length][];
        mTags = new String[mSpans.length][];

        for (int i = 0; i < mSpans.length; ++i) {
            readSpan(i,mSpans[i]);
	    //            System.out.println("read span: " + i);
	    //                    System.out.println("tokens: " + mTokens[i].length);
	    //                        System.out.println("whitespaces: " + mWhitespaces[i].length);
	    //                        for (int j=0; j< mTokens[i].length; j++) {
	    //                            System.out.println("i,j:" + i + "," + j
	    //                                               + " ws: |" + mWhitespaces[i][j] + "|"
	    //                                               + " tok: " + mTokens[i][j]
	    //                                               );
	    //                        }
        }

        mTagSelectors = new TagSelector[mSpans.length][];
        mLabels = new JTextArea[mSpans.length][];
        for (int i = 0; i < mTags.length; ++i) {
            int len = mTags[i].length;
            mTagSelectors[i] = new TagSelector[len];
            mLabels[i] = new JTextArea[len];
        }
        updateGUI();
        return true;
    }


    static int lineCt(String text) {
        int result = 1;
        Pattern p = Pattern.compile("\n");
        Matcher m = p.matcher(text);
        while (m.find()) result++;
        return result;
    }

    static final int MIN_TAG_WIDTH = 60;
    void updateGUI() {
        System.out.println("update GUI");
        System.out.flush();
        mAnnotationPane.removeAll();
        mAnnotationPane.setLayout(new BoxLayout(mAnnotationPane,BoxLayout.Y_AXIS));
        StringBuilder sbDisplayText = new StringBuilder();
        for (int i = 0; i < mTags.length; ++i) {
            String containingChunkType = mSpans[i].getAttributeValue("type");
            JLabel containingChunkTypeLabel = new JLabel(containingChunkType);
            JPanel notePane = new JPanel();
            notePane.setLayout(new BoxLayout(notePane,BoxLayout.Y_AXIS));

            JPanel linePane = new JPanel();
            linePane.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));

            JPanel textPane = new JPanel();
            textPane.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));
            String displayText = null;
            JTextArea textDisplay = null;

            String[] tokens = mTokens[i];
            String[] tags = mTags[i];
            String[] whitespaces = mWhitespaces[i];
            // index linebreaks
            int[] idxRow = new int[tokens.length];
            int[] idxCol = new int[tokens.length];
            int curRow = 0;
            int curCol = 0;
            for (int k = 0; k < tokens.length; k++) {
                if (whitespaces[k].contains("\n") || 
                    (curCol > TOKS_PER_LINE
                     && whitespaces[k] != null 
                     && whitespaces[k].length() > 0)) {
                    curRow++;
                    curCol = 0;
                } else  {
                    curCol++;
                }
                idxRow[k] = curRow;
                idxCol[k] = curCol;
            }
            // create display
            for (int k = 0; k < tokens.length; k++) {
                //                System.out.println("k: " + k + " row: " + idxRow[k] + ", col: " + idxCol[k] + " tok: " + tokens[k]);
                if (k > 0 && idxCol[k]==0) {
                    // System.out.println(sbDisplayText.toString());
                    // add current line
                    displayText = sbDisplayText.toString();
                    textDisplay = new JTextArea(displayText,1,displayText.length());
                    textDisplay.setLineWrap(true);
                    textDisplay.setWrapStyleWord(true);
                    textDisplay.setFont(mCorpusAnnotator.mTextFont);
                    textDisplay.setEditable(false);
                    textPane.add(textDisplay);
                    textPane.setAlignmentX(0.0f);
                    textPane.setAlignmentY(0.0f);
                    notePane.add(textPane);
                    linePane.setAlignmentX(0.0f);
                    linePane.setAlignmentY(0.0f);
                    notePane.add(linePane);
                    // reset containers
                    sbDisplayText.setLength(0);
                    textPane = new JPanel();
                    textPane.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));
                    linePane = new JPanel();
                    linePane.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));
                }
                String displayToken = "";
                if  (k == 0) { 
                    displayToken = whitespaces[k] + tokens[k] + whitespaces[1];
                } else if  (k == tokens.length-1) {
                    displayToken = tokens[k];
                } else {
                    displayToken = tokens[k] + whitespaces[k+1];
                }
                displayToken = displayToken.replace("\n","");
                sbDisplayText.append(displayToken);

                TagSelector tagSelector = new TagSelector(i,k);
                JTextArea textLabel = new JTextArea(displayToken);
                textLabel.setFont(mCorpusAnnotator.mTextFont);
                textLabel.setEditable(false);
                mLabels[i][k] = textLabel;
                textLabel.setLineWrap(false);
                textLabel.setOpaque(true);
                textLabel.setBorder(new LineBorder(Color.LIGHT_GRAY));
                Insets insets = textLabel.getInsets();
                insets.bottom = 10;
                insets.top = 5;
                insets.left = 10;
                insets.right = 30;
                if (tagSelector.getSelection().equals(OUT_TAG_LABEL))
                    textLabel.setBackground(BG_COLOR);
                else if (!tagSelector.getSelection().equals(CONTINUE_TAG_LABEL))
                    textLabel.setBackground(ENTITY_START_COLOR);
                else
                    textLabel.setBackground(ENTITY_CONTINUE_COLOR);
                JPanel tokenTagControl = new JPanel();
                tokenTagControl.setLayout(new BoxLayout(tokenTagControl,BoxLayout.Y_AXIS));
                tokenTagControl.add(tagSelector);
                tokenTagControl.add(textLabel);
                // max width of current tag, current token
                int tagWidth = mCorpusAnnotator.mTextFont.getSize() * Math.max(tagSelector.getSelection().length(),4);
                int prefWidth = Math.max(tagWidth,(int)textLabel.getPreferredSize().getWidth());
                tokenTagControl.setPreferredSize(new Dimension(prefWidth,50));
                tokenTagControl.setAlignmentX(0.0f);
                tokenTagControl.setAlignmentY(0.0f);
                linePane.add(tokenTagControl);
            }
            // last row display text
            displayText = sbDisplayText.toString().replace("\n","");
            textDisplay = new JTextArea(displayText,1,displayText.length());
            textDisplay.setLineWrap(true);
            textDisplay.setWrapStyleWord(true);
            textDisplay.setFont(mCorpusAnnotator.mTextFont);
            textDisplay.setEditable(false);
            textPane.add(textDisplay);
            textPane.setAlignmentX(0.0f);
            textPane.setAlignmentY(0.0f);
            notePane.add(textPane);
            // last row token controls
            linePane.setAlignmentX(0.0f);
            linePane.setAlignmentY(0.0f);
            notePane.add(linePane);
            // end get last row

            mAnnotationPane.add(containingChunkTypeLabel);
            mAnnotationPane.add(notePane);
        }
        System.out.println("updateGUI done");
        System.out.flush();
    }
 
    void discard() throws InterruptedException {
        if (mCorpusAnnotator == null) return;
        mCorpusAnnotator.discard(mInputFile.getName());
        mCorpusAnnotator.nextDocument();

    }

    void revert() {
        if (mCorpusAnnotator == null) return;
        mCorpusAnnotator.revert(mInputFile);
    }


    void finish() throws IOException, InterruptedException {
        System.out.println("finish processing file");
        mCorpusAnnotator.finished(mInputFile);

        new SwingWorker<Void,Void>() {
            public Void doInBackground() {
                try {
                    writeDocument();
                    mCorpusAnnotator.trainTagger(mOutputFile);
                    System.out.println("re-trained tagger");
                    System.out.flush();
                }
                catch (Exception e) {
                    System.out.println("exception writing document=" + e);
                    e.printStackTrace(System.out);
                }
                return null;
            }
        }.execute();
    }


    void writeDocument() throws IOException {
        System.out.println("writing file");
        for (int i = 0; i < mSpans.length; ++i)
            updateElement(mSpans[i],mTokens[i],mWhitespaces[i],
                          mTagSelectors[i]);

        Format format = Format.getRawFormat();
        format.setEncoding(mOutputCharset);
        XMLOutputter outputter = new XMLOutputter(format);
        FileOutputStream fileOut = null;
        BufferedOutputStream bufOut = null;
        OutputStreamWriter writer = null;
        try {
            fileOut = new FileOutputStream(mOutputFile);
            bufOut = new BufferedOutputStream(fileOut);
            writer = new OutputStreamWriter(bufOut,mOutputCharset);
            outputter.output(mDocument,writer);
            System.out.println("written");
        } finally {
            Streams.closeQuietly(writer);
            Streams.closeQuietly(bufOut);
            Streams.closeQuietly(fileOut);
        }
    }

    void updateElement(Element elt,
                       String[] toks, String[] whites,
                       TagSelector[] tags) {
        elt.removeContent();
        int i = 0;
        while (true) {
            StringBuilder sb = new StringBuilder();
            while (i < tags.length
                   && tags[i].getSelection().equals(OUT_TAG_LABEL)) {
                sb.append(whites[i]);
                sb.append(toks[i]);
                ++i;
            }
            sb.append(whites[i]);
            Text text = new Text(sb.toString());
            elt.addContent(text);

            if (i >= tags.length)
                return;

            StringBuilder sb2 = new StringBuilder();
            String type = tags[i].getSelection().toString();
            sb2.append(toks[i++]);
            while (i < tags.length
                   && tags[i].getSelection().equals(CONTINUE_TAG_LABEL)) {
                sb2.append(whites[i]);
                sb2.append(toks[i]);
                ++i;
            }
            Element chunk = new Element("chunk");
            chunk.setAttribute("type",type);
            String s2Tmp = sb2.toString();
            Text text2 = new Text(s2Tmp);
            chunk.addContent(text2);
            elt.addContent(chunk);
        }
    }

    void readSpan(int i, Element span) {
        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (Object contentObj : span.getContent()) {
            if (contentObj instanceof Text) {
                Text textContent = (Text) contentObj;
                String text = textContent.getText();
                sb.append(text);
            } else {
                readText(sb,tokenList,whiteList,tagList); // tokenize text already collected
                readChunk((Element)contentObj,tokenList,whiteList,tagList); // tokenize annotation
            }
        }
        readText(sb,tokenList,whiteList,tagList);
        mTokens[i] = tokenList.<String>toArray(new String[tokenList.size()]);
        mWhitespaces[i] = whiteList.<String>toArray(new String[whiteList.size()]);
        mTags[i] = tagList.<String>toArray(new String[tagList.size()]);
    }

    void readText(StringBuilder sb,
                  List<String> tokenList, List<String> whiteList,
                  List<String> tagList) {
        char[] cs = new char[sb.length()];
        sb.getChars(0,sb.length(),cs,0);
        sb.delete(0,sb.length());

        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        while (true) {
            String whitespace = tokenizer.nextWhitespace();
            whiteList.add(whitespace);
            String token = tokenizer.nextToken();
            if (token == null)
                break;
            tokenList.add(token);
            tagList.add(OUT_TAG_LABEL);
        }
    }

    void readChunk(Element chunk,
                   List<String> tokenList, List<String> whiteList,
                   List<String> tagList) {
        String chunkType = chunk.getAttributeValue("type");
        String text = chunk.getText();
        char[] cs = text.toCharArray();
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);

        boolean atFirst = true;
        while (true) {
            String whitespace = tokenizer.nextWhitespace(); 
            String token = tokenizer.nextToken();
            if (token == null) return;
            if (atFirst) {
                tokenList.add(token);
                tagList.add(chunkType);
                atFirst = false;
            } else {
                whiteList.add(whitespace);
                tokenList.add(token);
                tagList.add(CONTINUE_TAG_LABEL);
            }
        }
    }

    TagSelector mMouseStart = null;
    TagSelector mMouseLast = null;

    static class TagSelectionRenderer
        extends JLabel implements ListCellRenderer {
        static final long serialVersionUID = 7179242060621377692L;

        JComboBox mBox;
        public TagSelectionRenderer(JComboBox box) {
            setOpaque(true);
            mBox = box;
        }
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            String label = value.toString();
            setText(label);
            if (OUT_TAG_LABEL.equals(label)) {
                mBox.setBackground(BG_COLOR);
            } else if (CONTINUE_TAG_LABEL.equals(label)) {
                mBox.setBackground(ENTITY_CONTINUE_COLOR);
            } else {
                mBox.setBackground(ENTITY_START_COLOR);
            }
            if (cellHasFocus)
                mBox.setBackground(Color.YELLOW);
            return this;
        }
    }

    class TagSelector extends JComboBox
        implements ActionListener, KeyListener, MouseListener, FocusListener {
        static final long serialVersionUID = 6169242060621366692L;

        final int mSectionIndex;
        final int mTokenIndex;

        TagSelector(int sectionIndex, int tokenIndex) {
            super(tokenIndex == 0
                  || mTags[sectionIndex][tokenIndex-1].equals(OUT_TAG_LABEL)
                  ? mInitialTags
                  : mContinueTags);
            mSectionIndex = sectionIndex;
            mTokenIndex = tokenIndex;
            setEditable(false); // no adding types on the fly
            setSelection(mTags[sectionIndex][tokenIndex]);
            super.addActionListener(this);
            mTagSelectors[sectionIndex][tokenIndex] = this;
            super.addKeyListener(this);

            super.addMouseListener(this);

            super.addFocusListener(this);

            super.setLightWeightPopupEnabled(true);

            setBackground(BG_COLOR);
            setRenderer(new TagSelectionRenderer(this));

            KeyStroke ctrlL
                = KeyStroke.getKeyStroke(KeyEvent.VK_L,
                                     KeyEvent.CTRL_DOWN_MASK);
            ActionListener verticalCenterAction = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        verticalCenter();
                    }
                };

            this.registerKeyboardAction(verticalCenterAction,
                                        "vertical center",
                                        ctrlL,
                                        JComponent.WHEN_FOCUSED);

        }

        void verticalCenter() {
            verticalCenter(32);
            verticalCenter(24);
            verticalCenter(16);
            verticalCenter(8);
            verticalCenter(4);
            verticalCenter(2);
        }

        // this is wrong.
        // tag selector is inside a flow layout (line)
        // line is inside a box layout
        // make line that contains this tag selector middle of screen.
        
        void verticalCenter(int amt) {
            TagSelector current = this;

            TagSelector before = current;
	    int numTagsBefore = Math.min(amt,8);
            for (int i = 0; i < numTagsBefore; ++i) {
                TagSelector previous = before.before(false); // no wrap
                if (previous == null) break;
                before = previous;
            }

            TagSelector after = current;
            for (int i = 0; i < amt; ++i) {
                TagSelector next = after.after();
                if (next == null) break;
                after = next;
            }
            before.scrollRectToVisible(before.getBounds(null));
            after.scrollRectToVisible(after.getBounds(null));
            this.scrollRectToVisible(this.getBounds(null));

        }


        public String toString() {
            return mSectionIndex + "." + mTokenIndex;
        }

        void setSelection(Object tag) {
            setSelectedItem(tag);
        }

        String getSelection() {
            return getSelectedItem().toString();
            // return getValue().toString();
        }

        public void processKeyEvent(KeyEvent e) {
            super.processKeyEvent(e);
        }

        int itemCount() {
            return getItemCount();
        }

        void removeListItem(Object item) {
            removeItem(item);  // only continuation
        }

        void addListItem(Object item) {
            insertItemAt(item,0); // only continuation
        }

        TagSelector before(boolean wrap) {
            if (mTokenIndex > 0)
                return mTagSelectors[mSectionIndex][mTokenIndex-1];
            if (!wrap && mSectionIndex == 0)
                return null;
            for (int sectionIndex = (mSectionIndex == 0
                                     ? (wrap ? mTagSelectors.length-1 : -1)
                                     : mSectionIndex-1);
                 sectionIndex >= 0;
                 --sectionIndex) {
                TagSelector[] selectors = mTagSelectors[sectionIndex];
                if (selectors.length > 0)
                    return selectors[selectors.length-1]; // last elt in sect
            }
            return null;
        }

        TagSelector after() {
            if (mTokenIndex + 1 < mTagSelectors[mSectionIndex].length)
                return mTagSelectors[mSectionIndex][mTokenIndex+1];
            for (int sectionIndex = mSectionIndex + 1;
                 sectionIndex < mTagSelectors.length;
                 ++sectionIndex) {
                if (mTagSelectors[sectionIndex].length > 0)
                    return mTagSelectors[sectionIndex][0];
            }

            return null;
        }

        void autoRegister() {
            autoRegister(this);
        }

        void autoRegister(TagSelector box) {
             box.requestFocus();
        }

        // Focus Listener
        public void focusGained(FocusEvent e) {
            int tokenWindow = 16;

            // int tokenIndexBefore
            // = Math.max(0,mTokenIndex-tokenWindow);
            // TagSelector boxBefore
            // = mTagSelectors[mSectionIndex][tokenIndexBefore];
            // Rectangle rectBefore = boxBefore.getBounds(null);

            // int numTokens = mTagSelectors[mSectionIndex].length;
            // int tokenIndexAfter
            // = Math.min(numTokens-1,mTokenIndex+tokenWindow);
            // TagSelector boxAfter
            // = mTagSelectors[mSectionIndex][tokenIndexAfter];
            // Rectangle rectAfter = boxAfter.getBounds(null);
            // boxAfter.scrollRectToVisible(rectAfter);

            TagSelector box
                = mTagSelectors[mSectionIndex][mTokenIndex];
            Rectangle rectAt = box.getBounds(null);
            box.scrollRectToVisible(rectAt);

            // Rectangle rect
            // = new Rectangle(rectBefore.x,
            // rectBefore.y,
            // rectBefore.width,
            // rectAfter.height
            // + rectAfter.y - rectBefore.y);
        }
        public void focusLost(FocusEvent e) {
            // do nothing
        }

        // KeyListener
        public void keyPressed(KeyEvent e) {
            int keyChar = e.getKeyChar();
            int keyCode = e.getKeyCode();
            switch (keyCode) {
            case KeyEvent.VK_BACK_SPACE:
                TagSelector before = before(true);
                if (before != null)
                    autoRegister(before);
                break;
            case KeyEvent.VK_EQUALS:  // same key on std keyboard
            case KeyEvent.VK_PLUS:
                TagSelector after = after();
                if (after == null) break;
                autoRegister(after);
                if (OUT_TAG_LABEL.equals(getSelection())) break;
                after.setSelection(CONTINUE_TAG_LABEL);
                break;
            case KeyEvent.VK_MINUS:
                TagSelector before2 = before(false);
                if (before2 == null) break;
                autoRegister(before2);
                setSelection(OUT_TAG_LABEL);
                break;
            case KeyEvent.VK_0:
                focusNextEntity(mSectionIndex,mTokenIndex+1);
                break;

            // THESE BOUND BY COMBOBOX:
            // case KeyEvent.VK_ENTER:  // enter
            // case KeyEvent.VK_SPACE:  // space bar
            // case KeyEvent.VK_LEFT:   // left arrow
            // case KeyEvent.VK_RIGHT:  // right arrow

            }

        }
        public void keyReleased(KeyEvent e) {
            //
        }
        public void keyTyped(KeyEvent e) {
            //
        }

        // ActionListener
        public void actionPerformed(ActionEvent e) {
            String tagSelected = getSelection();

            mHasBeenEdited = true;  // atomic in event thread

            // reset labels on box triggering action
            resetLabels();
            int i = mTokenIndex + 1;

            // if action is OUT, reset following non-out labels to OUT
            if (tagSelected.equals(OUT_TAG_LABEL)) {
                while (i < mTagSelectors[mSectionIndex].length) {
                    TagSelector box = mTagSelectors[mSectionIndex][i];
                    if (!CONTINUE_TAG_LABEL.equals(box.getSelection()))
                        break;
                    box.setSelection(OUT_TAG_LABEL);
                    box.resetLabels();
                    ++i;
                }
            }
            // reset label following last reset box
            if (i < mTagSelectors[mSectionIndex].length)
                mTagSelectors[mSectionIndex][i].resetLabels();
        }


        // MouseListener
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
            mouseCopy();
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
            mMouseStart = this;
            mMouseLast = this;
        }
        public void mouseReleased(MouseEvent e) {
            mMouseStart = null;
            mMouseLast = null;
        }

        public void mouseCopy() {
            if (mMouseStart == null) return; // nothing selected

            TagSelector nextBox = null;
            if (mMouseStart.mSectionIndex > mSectionIndex) {
                TagSelector[] boxes = mTagSelectors[mMouseStart.mSectionIndex];
                nextBox = boxes[boxes.length-1];
            } else if (mMouseStart.mSectionIndex < mSectionIndex) {
                nextBox = mMouseStart;
            } else if (mMouseStart.mTokenIndex >= mTokenIndex) {
                nextBox = mMouseStart;
            } else {
                nextBox = this;
            }

            // remove labels that are past
            for (int i = nextBox.mTokenIndex + 1; i <= mMouseLast.mTokenIndex; ++i)
                if (i != mMouseStart.mTokenIndex)
                    mTagSelectors[mSectionIndex][i].setSelection(OUT_TAG_LABEL);

            // set labels just added
            String val = OUT_TAG_LABEL.equals(mMouseStart.getSelection())
                ? OUT_TAG_LABEL
                : CONTINUE_TAG_LABEL;
            for (int i = mMouseLast.mTokenIndex + 1; i <= nextBox.mTokenIndex; ++i)
                mTagSelectors[mSectionIndex][i].setSelection(val);

            mMouseLast = nextBox;
            this.requestFocusInWindow();
        }

        void resetLabels() {
            JTextArea label = mLabels[mSectionIndex][mTokenIndex];
            String tag = getSelection().toString();
            if (OUT_TAG_LABEL.equals(tag))
                label.setBackground(BG_COLOR);
            else if (CONTINUE_TAG_LABEL.equals(tag))
                label.setBackground(ENTITY_CONTINUE_COLOR);
            else
                label.setBackground(ENTITY_START_COLOR);

            if (mTokenIndex == 0) return; // never has continue option
            TagSelector previousBox
                = mTagSelectors[mSectionIndex][mTokenIndex-1];
            if (OUT_TAG_LABEL.equals(previousBox.getSelection())) {
                if (itemCount() == mContinueTags.length)
                    removeListItem(CONTINUE_TAG_LABEL);
            } else {
                if (itemCount() == mInitialTags.length)
                    addListItem(CONTINUE_TAG_LABEL);
            }
        }

    }

    void exit() {
        try {
            System.out.println("Exiting.");
        } catch (Throwable t) {
            System.out.println("err/excpt on exit=" + t);
            t.printStackTrace(System.out);
        } finally {
            System.exit(0);
        }
    }

    boolean isSpan(Object content) {
        if (!(content instanceof Element))
            return false;
        Element span = (Element) content;
        if (!"chunk".equals(span.getQualifiedName()))
            return false;
        if (!mSpanTagSet.contains(span.getAttributeValue("type")))
            return false;
        for (Object contentObj : span.getContent()) {
            if (contentObj instanceof Element) {
                Element contentElt = (Element) contentObj;
                validateChunk(contentElt);
            } else if (!(contentObj instanceof Text)) {
                String msg = "Span contained illegal content."
                    + " Found class=" + contentObj.getClass();
                throw new IllegalArgumentException(msg);
            }
        }
        return true;
    }

    void validateChunk(Element chunk) {
        String foundTag = chunk.getQualifiedName();
        if (!"chunk".equals(foundTag)) {
            String msg = "Elements must be of type chunk."
                + " Found tag=" + foundTag;
            throw new IllegalArgumentException(msg);
        }
        String type = chunk.getAttributeValue("type");
        if (!mChunkTypeSet.contains(type)) {
            String msg = "Unknown type=" + type;
            throw new IllegalArgumentException(msg);
        }

        for (Object contentObj : chunk.getContent()) {
            Class clazz = contentObj.getClass();
            if (Text.class.equals(clazz))
                continue;
            String msg = "Chunk contained illegal content."
                + " Found class=" + clazz;
            throw new IllegalArgumentException(msg);
        }
        String text = chunk.getText();
        Tokenizer tokenizer
            = mTokenizerFactory
            .tokenizer(text.toCharArray(),0,text.length());
        String initialWhitespace = tokenizer.nextWhitespace();
        if (initialWhitespace.length() != 0) {
            String msg = "Illegal initial whitespace in chunk."
                + " text=|" + text + "|"
                + " initial whitespace=|" + initialWhitespace + "|";
            throw new IllegalArgumentException(msg);
        }
        while (tokenizer.nextToken() != null) ;
        String finalWhitespace = tokenizer.nextWhitespace();
        if (finalWhitespace.length() != 0) {
            String msg = "Illegal final whitespace in chunk."
                + " text=|" + text + "|"
                + " final whitespace=|" + finalWhitespace + "|";
            throw new IllegalArgumentException(msg);
        }
    }

    static final String OUT_TAG_LABEL = "-";
    static final String CONTINUE_TAG_LABEL = ". . .";

    static final Color BG_COLOR = new Color(0xF8,0xF8,0xF8);
    static final Color ENTITY_START_COLOR = new Color(0xB8,0xB8,0xB8);
    static final Color ENTITY_CONTINUE_COLOR = new Color(0xD8,0xD8,0xD8);
}
