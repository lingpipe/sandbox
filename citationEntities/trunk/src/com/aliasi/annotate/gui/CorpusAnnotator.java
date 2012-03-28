package com.aliasi.annotate.gui;

// THIS PROJECT
import com.aliasi.annotate.corpora.AnnotatorCorpusParser;
import com.aliasi.annotate.corpora.NoisyTokenizerFactory;

// LINGPIPE
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.io.FileExtensionFilter;

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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.*;
import javax.swing.border.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import org.xml.sax.SAXException;

public class CorpusAnnotator {

    private boolean mAutoAnnotate;

    private File mInDir;
    private File mOutDir;

    private Set<String> mFileNamesLeft;
    private String mFileNameOpen;
    private Set<String> mFileNamesDone;

    private Set<String> mFileNamesQueued;


    private JProgressBar mCorpusProgressBar;

    private Set<String> mSavedFileNames
        = new HashSet<String>();

    private JPanel mFilePanel;
    JPanel mEditorPane;

    private JList mFileList;
    private JList mFileFinishedList;

    private String mInCharset;
    private String mOutCharset;
    private TokenizerFactory mTokenizerFactory;
    private String[] mSpanTags;
    private String[] mChunkTypes;

    JLabel mProgressLabel;
    JLabel mFilesRemainingLabel;
    JLabel mFilesFinishedLabel;

    JFrame mTopFrame;
    JSplitPane mTopSplitPane;

    PrettyDocumentAnnotator mOpenAnnotator;

    // CharLmRescoringChunker mAutoChunker;
    CharLmHmmChunker mAutoChunker;

    LinkedBlockingQueue<PrettyDocumentAnnotator> mAnnotatorQueue;

    SwingWorker<Void,Void> mQueueWorker;

    AnnotatorCorpusParser mAnnotatorTrainingParser;

    ReentrantReadWriteLock mTaggerRwLock
        = new ReentrantReadWriteLock(true); // fair

    Font mTextFont;

    public CorpusAnnotator(File inDir, String inCharset,
                           File outDir, String outCharset,
                           TokenizerFactory tokenizerFactory,
                           String[] spanTags,
                           String[] chunkTypes,
                           int textFontSize) throws Exception {

        this(inDir,inCharset,outDir,outCharset,
             tokenizerFactory,
             spanTags,
             chunkTypes,
             textFontSize,
             true);
    }

    public CorpusAnnotator(File inDir, String inCharset,
                           File outDir, String outCharset,
                           TokenizerFactory tokenizerFactory,
                           String[] spanTags,
                           String[] chunkTypes,
                           int textFontSize,
                           boolean autoAnnotate) throws Exception {

        mAutoAnnotate = autoAnnotate;

        mTextFont = new Font("times new roman",Font.PLAIN,textFontSize);
        // init input params
        mInDir = inDir;
        mInCharset = inCharset;
        mOutDir = outDir;
        mOutCharset = outCharset;
        mTokenizerFactory = tokenizerFactory;
        mSpanTags = spanTags;
        mChunkTypes = chunkTypes;

        // init files from in/out dirs
        mFileNamesLeft
            = Collections.<String>synchronizedSet(new LinkedHashSet<String>());
        File[] filesLeft = inDir.listFiles(XML_FILE_FILTER);
        Arrays.sort(filesLeft);
        for (File inFile : filesLeft)
            mFileNamesLeft.add(inFile.getName());

        mFileNameOpen = null;

        mFileNamesDone
            = Collections.<String>synchronizedSet(new LinkedHashSet<String>());
        if (!mOutDir.isDirectory()) {
            String msg = "Out dir must exit.  dir=" + mOutDir;
            throw new IllegalArgumentException(msg);
        }
        File[] filesDone =  mOutDir.listFiles(XML_FILE_FILTER);
        Arrays.sort(filesDone);
        for (File outFile : filesDone) {
            String outFileName = outFile.getName();
            if (mFileNamesLeft.remove(outFileName)) {
                mFileNamesDone.add(outFileName);
            } else {
                String msg = "Output file w/o input file=" + outFileName;
                System.out.println(msg);
            }
        }

        mFileNamesQueued
            = Collections.<String>synchronizedSet(new HashSet<String>()); // initially empty

        // init GUI objects
        mFilePanel = new JPanel(new GridBagLayout());
        mEditorPane = new JPanel(new GridBagLayout());
        mEditorPane.setBackground(Color.DARK_GRAY);

        mFilesRemainingLabel = new JLabel("Files Remaining");
        mFilePanel.add(mFilesRemainingLabel,
                       new GridBagConstraints(0,0, 1,1, 0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,7,0,5),
                                              0,0));


        String[] fileNamesLeft = mFileNamesLeft.<String>toArray(new String[0]);
        mFileList = new JList(fileNamesLeft);
        mFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mFileList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int index = mFileList.locationToIndex(e.getPoint());
                    String fileName = mFileList.getSelectedValue().toString();
                    try {
                        //                        System.out.println("remaining files, chose: " + fileName);
                        nextDocument(fileName);
                    } catch (InterruptedException e2) {
                        System.out.println("Ignoring interruption=" + e2);
                        return;
                    }
                }
            });
        JScrollPane scrollPane = new JScrollPane(mFileList);
        mFilePanel.add(scrollPane,
                       new GridBagConstraints(0,1, 1,1, 1.0,1.0,
                                              GridBagConstraints.NORTH,
                                              GridBagConstraints.BOTH,
                                              new Insets(2,5,5,5),
                                              0,0));

        mFilesFinishedLabel = new JLabel("Files Finished");
        mFilePanel.add(mFilesFinishedLabel,
                       new GridBagConstraints(0,2, 1,1, 0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,7,0,5),
                                              0,0));
        String[] fileNamesFinished
            = mFileNamesDone.<String>toArray(new String[0]);
        Arrays.sort(fileNamesFinished);
        mFileFinishedList = new JList(fileNamesFinished);
        mFileFinishedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mFileFinishedList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int index = mFileFinishedList.locationToIndex(e.getPoint());
                    String fileName
                        = mFileFinishedList.getSelectedValue().toString();
                    try {
                        //                        System.out.println("completed files, chose: " + fileName);
                        nextDocument(fileName);
                    } catch (InterruptedException e2) {
                        System.out.println("Ignoring interruption=" + e2);
                        return;
                    }
                }
            });
        JScrollPane scrollPaneFinished
            = new JScrollPane(mFileFinishedList);
        mFilePanel.add(scrollPaneFinished,
                       new GridBagConstraints(0,3, 1,1, 1.0,1.0,
                                              GridBagConstraints.NORTH,
                                              GridBagConstraints.BOTH,
                                              new Insets(2,5,5,5),
                                              0,0));

        int numFiles = mFileNamesLeft.size() + mFileNamesDone.size();
        mCorpusProgressBar = new JProgressBar(0,numFiles);
        mCorpusProgressBar.setStringPainted(true);

        mProgressLabel = new JLabel("Corpus Progress");
        mFilePanel.add(mProgressLabel,
                       new GridBagConstraints(0,4, 1,1, 0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(20,7,0,5),
                                              0,0));
        mFilePanel.add(mCorpusProgressBar,
                       new GridBagConstraints(0,5, 1,1, 1.0,0.0,
                                              GridBagConstraints.NORTH,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(2,5,5,5),
                                              0,0));


        mFilePanel.add(Box.createVerticalGlue(),
                       new GridBagConstraints(0,6, 1,1, 1.0,1.0,
                                              GridBagConstraints.NORTH,
                                              GridBagConstraints.VERTICAL,
                                              new Insets(0,0,0,0),
                                              0,0));

        mTopSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mTopSplitPane.setLeftComponent(mFilePanel);
        mTopSplitPane.setRightComponent(mEditorPane);
        mTopSplitPane.setDividerLocation(0.20);
        mTopSplitPane.setOpaque(true);

        mTopFrame = new JFrame("LingPipe Chunk Annotator: " + inDir);
        mTopFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mTopFrame.addWindowListener(new WindowAdapter()  {
                public void windowClosing(WindowEvent e) {
                    if (mFileNameOpen != null
                        && mOpenAnnotator != null
                        && mOpenAnnotator.mHasBeenEdited) {

                        Object[] options = { "OK. Exit.",
                                             "Cancel." };
                        String msg = "Exiting deletes all uncommitted edits.";
                        int n = JOptionPane.showOptionDialog(mTopFrame,
                                                             msg,
                                                             "Confirm Exit",
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             options,
                                                             options[0]);
                        if (n != JOptionPane.YES_OPTION) return;
                    }
                    exit();
                }
            });
        mTopFrame.setContentPane(mTopSplitPane);
        mTopFrame.pack();
        mTopFrame.setResizable(true);
        mTopFrame.setSize(800,600); // size to revert to
        // mTopFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mTopFrame.setVisible(true);

        mAnnotatorQueue = new LinkedBlockingQueue<PrettyDocumentAnnotator>(2);
        mQueueWorker = new SwingWorker<Void,Void>() {
            public Void doInBackground() {
                while (true)
                    if (!queueNext())
                        return null;
            }
        };

        HmmCharLmEstimator lmEstimator
            = new HmmCharLmEstimator(8,256,8.0,false); // false: no smooth tag
        mAutoChunker
            = new CharLmHmmChunker(mTokenizerFactory,lmEstimator);
        // = new CharLmRescoringChunker(mTokenizerFactory,256,10,128,8.0);


        Set<String> containingTagSet = new HashSet<String>();
        for (String tag : mSpanTags)
            containingTagSet.add(tag);
        mAnnotatorTrainingParser
            = new AnnotatorCorpusParser(containingTagSet);
        mAnnotatorTrainingParser.setHandler(mAutoChunker);

        SwingWorker<Void,Void> initializer = new SwingWorker<Void,Void>() {
            public Void doInBackground() {
                trainTagger();
                return null;
            }
            public void done() {
                mQueueWorker.execute(); // execute after tagger done
                try { nextDocument(); }
                catch (InterruptedException e) { }
            }
        };
        initializer.execute();
    }


    boolean queueNext() {
        // only get next and return true if successful; executed in worker
        for (String fileNameLeft : mFileNamesLeft) {
            if (mFileNamesQueued.contains(fileNameLeft)) continue;
            File inFile = new File(mInDir,fileNameLeft);
            File outFile = new File(mOutDir,fileNameLeft);
            PrettyDocumentAnnotator annotator;
            try {
                annotator
                    = new PrettyDocumentAnnotator(this,
                                            inFile,mInCharset,
                                            outFile,mOutCharset,
                                            mTokenizerFactory,
                                            mSpanTags,
                                            mChunkTypes);
                if (mAutoAnnotate)
                    annotator.autoAnnotate();
            } catch (IOException e) {
                System.out.println("QueueNext exception=" + e);
                e.printStackTrace(System.out);
                return false; // couldn't finish
            } catch (SAXException e) {
                System.out.println("QueueNext exception=" + e);
                e.printStackTrace(System.out);
                return false; // couldn't finish
            } catch (JDOMException e) {
                System.out.println("QueueNext exception=" + e);
                e.printStackTrace(System.out);
                return false; // couldn't finish
            }
            try {
                mAnnotatorQueue.put(annotator); // blocks until room
                mFileNamesQueued.add(fileNameLeft);
            } catch (InterruptedException e) {
                System.out.println("Queue Interrupted.");
                return false; // couldn't finish
            }
            return true; // found and added item
        }
        return false; // nothing found
    }


    void trainTagger() {
        int numFiles = mFileNamesDone.size();
        ProgressMonitor monitor
            = new ProgressMonitor(mTopSplitPane,
                                  "Training Tagger.",
                                  "",
                                  -1,
                                  numFiles);
        monitor.setMillisToPopup(100);
        monitor.setMillisToDecideToPopup(100);
        monitor.setProgress(0);

        int progress = 0;
        monitor.setNote("Completed " + progress
                        + "/" + numFiles + " Files.");
        for (String fileName : mFileNamesDone) {
            //            System.out.println("trainining on file: " + fileName);
            File file = new File(mOutDir,fileName);
            trainTagger(file);
            monitor.setProgress(++progress);
            monitor.setNote("Completed " + progress
                            + "/" + numFiles + " Files.");
        }
        System.out.println("Tagger trained on " + numFiles + " files");
        Toolkit.getDefaultToolkit().beep();
        monitor.close();
    }


    Chunking autoChunk(CharSequence in) {
        try {
            mTaggerRwLock.readLock().lock();
            return mAutoChunker.chunk(in);
        } finally {
            mTaggerRwLock.readLock().unlock();
        }
    }

    // only one trainer at a time
    synchronized void trainTagger(File file) {
        try {
            mTaggerRwLock.writeLock().lock();
            mAnnotatorTrainingParser.parse(file);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } finally {
            mTaggerRwLock.writeLock().unlock();
        }
    }

    void setProgress() {
        mCorpusProgressBar.setValue(mFileNamesDone.size());
    }

    void nextDocument() throws InterruptedException {
        //        System.out.println("nextDocument");
        if (mFileNamesLeft.size() == 0) {
            JLabel doneLabel = new JLabel("<html>Finished.<br />No Files Remaining.</html>");
            doneLabel.setBackground(Color.LIGHT_GRAY);
            doneLabel.setForeground(Color.WHITE);
            doneLabel.setFont(new Font("Arial",0,48));
            mEditorPane.removeAll();
            mEditorPane.add(doneLabel);
            mEditorPane.revalidate();
            return;
        }
        //        System.out.println("files remianing: " + mFileNamesLeft.size());
        //        System.out.println("about to create nextDocWorker");
        SwingWorker<PrettyDocumentAnnotator,Void> nextDocWorker
            = new SwingWorker<PrettyDocumentAnnotator,Void>() {

            public PrettyDocumentAnnotator doInBackground() {
                while (true) {
                    try {
                        if (mQueueWorker.isDone()
                            && mAnnotatorQueue.isEmpty())
                            return null;
                        PrettyDocumentAnnotator annotator
                        = mAnnotatorQueue.take(); // waits indefinitely
                        String fileName = annotator.mInputFile.getName();
                        if (mFileNamesLeft.remove(fileName)) {
                            mFileNameOpen = fileName;
                            return annotator;
                        }
                    } catch (InterruptedException e) {
                        return null; // done?
                    }
                }
            }
        };
        nextDocWorker.execute();

        PrettyDocumentAnnotator annotator = null;
        try {
            annotator
                = nextDocWorker.get(); // waits indefinitely
        } catch (ExecutionException e) {
            return; // done
        }

        if (annotator == null)
            return; // done

        mEditorPane.removeAll();
        mEditorPane.add(annotator,
                        new GridBagConstraints(0,0, 1,1, 1.0,1.0,
                                               GridBagConstraints.NORTH,
                                               GridBagConstraints.BOTH,
                                               new Insets(5,5,5,5),
                                               0,0));
        annotator.setVisible(true);
        annotator.requestFocus();  // just in case no entity found
        annotator.focusNextEntity();
        annotator.revalidate();
        mOpenAnnotator = annotator;
        resetFiles();
    }

    void nextDocument(String name) throws InterruptedException {
        if (mFileNameOpen != null && mOpenAnnotator != null) {
            if (mOpenAnnotator.mHasBeenEdited) {
                Object[] options = { "Discard Current Edits.",
                                     "Cancel." };
                String msg = "Opening a new file discards all current edits.";
                int n = JOptionPane.showOptionDialog(mTopFrame,
                                                     msg,
                                                     "Confirm Discard",
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.WARNING_MESSAGE,
                                                     null,
                                                     options,
                                                     options[0]);
                if (n != JOptionPane.YES_OPTION) return;
            }
            discard(mFileNameOpen);
        }

        File inFile = new File(mInDir,name);
        File outFile = new File(mOutDir,name);
        if (mFileNamesDone.remove(name)) {
            createAnnotator(outFile,outFile,false); // no auto-anno
        } else if (mFileNamesLeft.remove(name)) {
            createAnnotator(inFile,outFile,true); // auto anno
        } else {
            return; // already open
        }

        mFileNameOpen = name;
        resetFiles();
    }


    void finished(File inFile) throws InterruptedException {
        String name = inFile.getName();
        PrettyDocumentAnnotator annotator = mOpenAnnotator;
        mOpenAnnotator = null;
        annotator.setVisible(false);
        mEditorPane.removeAll();
        mEditorPane.revalidate(); // lost at end of queue

        mFileNamesLeft.remove(name);
        mFileNameOpen = null;
        mFileNamesDone.add(name);
        resetFiles();

        nextDocument();
    }

    void revert(File inFile) {
        //        System.out.println("REVERT");
        // could simplify this to just redraw after rereading all
        // then no need to change files
        // and this would mean it wouldn't dance around, too

        PrettyDocumentAnnotator annotator = mOpenAnnotator;
        mOpenAnnotator = null;
        annotator.setVisible(false);
        mEditorPane.removeAll();

        mFileNameOpen = null;

        String name = inFile.getName();
        File outFile = new File(mOutDir,name);
        createAnnotator(inFile,outFile,false);

        mFileNamesLeft.remove(name);
        mFileNamesDone.remove(name);
        mFileNameOpen = name;
        resetFiles();
    }

    void discard(String fileName) throws InterruptedException {
        PrettyDocumentAnnotator annotator = mOpenAnnotator;
        mOpenAnnotator = null;
        annotator.setVisible(false);
        mEditorPane.removeAll();
        mEditorPane.revalidate();

        mFileNameOpen = null;

        if (new File(mOutDir,fileName).exists()) {
            mFileNamesDone.add(fileName);
            mFileNamesLeft.remove(fileName);
        } else {
            mFileNamesLeft.add(fileName); // goes to end of linked list
            mFileNamesDone.remove(fileName);
        }
        resetFiles();
    }


    void resetFiles() {
        resetFiles(mFileNamesLeft,mFileList);
        resetFiles(mFileNamesDone,mFileFinishedList);
        setProgress();
    }

    void resetFiles(Set<String> fileNames, JList jList) {
        String[] filesLeft
            = fileNames.<String>toArray(new String[0]);
        Arrays.sort(filesLeft);
        jList.setListData(filesLeft);
    }

    //    static PrettyDocumentAnnotator sAnnotator = null;

    void createAnnotator(final File inFile, final File outFile,
                         final boolean autoAnnotate) {
        final ProgressMonitor monitor
            = new ProgressMonitor(mTopSplitPane,
                                  "Loading Next Document",
                                  "",
                                  -1,
                                  2);
        monitor.setMillisToPopup(1);
        monitor.setMillisToDecideToPopup(1);
        monitor.setProgress(0);
        SwingWorker<Void,Void> createAnnoWorker
            = new SwingWorker<Void,Void>() {
            public Void doInBackground() {
                createAnnotator2(inFile,outFile,autoAnnotate,monitor);
                return null;
            }
        };
        createAnnoWorker.execute();
    }

    void createAnnotator2(File inFile, File outFile, boolean autoAnnotate,
                          final ProgressMonitor monitor) {
        try {
            monitor.setProgress(0);
            monitor.setNote("Loading Annotator");
            final PrettyDocumentAnnotator annotator
                = new PrettyDocumentAnnotator(this,
                                        inFile,mInCharset,
                                        outFile,mOutCharset,
                                        mTokenizerFactory,
                                        mSpanTags,
                                        mChunkTypes);

            monitor.setProgress(1);
            //            sAnnotator = annotator;
            mEditorPane.removeAll();
            mEditorPane.add(annotator,
                            new GridBagConstraints(0,0, 1,1, 1.0,1.0,
                                                   GridBagConstraints.NORTH,
                                                   GridBagConstraints.BOTH,
                                                   new Insets(5,5,5,5),
                                                   0,0));
            String name = inFile.getName();
            mOpenAnnotator = annotator;
            if (autoAnnotate) {
                monitor.setNote("Auto Annotating");
                try {
                    mTaggerRwLock.readLock().lock();
                    annotator.autoAnnotate();
                } finally {
                    mTaggerRwLock.readLock().unlock();
                }
            }
            monitor.setProgress(2);
            annotator.setVisible(true);
            annotator.requestFocus();
            annotator.revalidate();
            //            annotator.focusNextEntity();
            annotator.revalidate();
        } catch (Exception e) {
            String msg = "Exception getting next doc."
                + " inFile=" + inFile
                + " outFile=" + outFile
                + " exception=" + e;
            System.out.println(msg);
            e.printStackTrace(System.out);
        }
    }

    public void exit() {
        // implicitly discard all
        System.exit(0);
    }


    private static final FileFilter XML_FILE_FILTER
        = new FileExtensionFilter("xml",false); // false: no dirs


    public static void main(final String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    launchGui(args);
                }
            });
    }


    static void launchGui(String[] args) {
        try {
            File inDir = new File(args[0]);
            
            String inCharset = args[1];

            File outDir = new File(args[2]);
            String outCharset = args[3];
            String[] annotatedElements = args[4].split(",");

            String[] chunkTypes = args[5].split(",");

            Class tfClass = args[6].getClass();
            TokenizerFactory factory
                = (TokenizerFactory) Class.forName(args[6]).getConstructor(new Class[0]).newInstance(new Object[0]);
            int textFontSize = Integer.parseInt(args[7]);
            boolean autoAnnotate = (args.length < 9) || Boolean.parseBoolean(args[8]);
            System.out.println("Auto annotate=" + autoAnnotate);

            // creates annotator
            final CorpusAnnotator annotator
                = new CorpusAnnotator(inDir,inCharset,
                                      outDir,outCharset,
                                      factory,
                                      annotatedElements,chunkTypes,
                                      textFontSize,
                                      autoAnnotate);
        } catch (Exception e) {
            System.out.println("Exception=" + e);
            e.printStackTrace(System.out);
        }
    }

}
