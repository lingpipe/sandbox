package stemmers;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FastCache;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class Editor {

    static HashMap<String,Decision> sDoneMap = new HashMap<String,Decision>();

    static Stack<String> sWordStack = new Stack<String>();

    static int sMinWordCount = 128;

    static JFrame sMainFrame;
    static JPanel sContentPanel;

    static JPanel sModePanel;
        
    static JLabel sWordTextLabel;

    static JPanel sTextFieldPanel;
    static JLabel sTextFieldLabel1;
    static JLabel sTextFieldLabel2;
    static JTextField sTextField1;
    static JTextField sTextField2;

    static JButton sBackButton;
    static JButton sOkButton;
    static JButton sSkipButton;

    static JEditorPane sDictPane;
    static JScrollPane sDictScrollPane;

    static JLabel sCorpusProgressLabel;
    static JProgressBar sCorpusProgressBar;
    static JProgressBar sExpectedErrorProgressBar;

    static ButtonGroup sModeButtonGroup;
    static JRadioButton sRootButton;
    static JRadioButton sSuffixButton;
    static JRadioButton sCompoundButton;
    static JRadioButton sPrefixButton;
    static JRadioButton sIrregularButton;
    static JRadioButton sNonWordButton;

    static Font sTextFieldFont
        = new Font("Arial",Font.BOLD,24);
    static Font sWordToAnnotateFont
        = new Font("Arial",Font.BOLD,36);

    static Writer sStemsWriter;

    static String sUserName;

    static ObjectToCounterMap sWordCounts;
    static String[] sWords;
    static File sWordCountFile = new File("data/gigawords-nyt.txt");
    static int sNextWord = 0;
    
    static File sCachedDefinitionFile = new File("cache.defs");
    static Writer sCacheWriter;
    static HashMap<String,String> sDefinitionCache
        = new HashMap<String,String>();

    static HashSet<String> sAnnotatedWords = new HashSet<String>();

    static Em4.Model sModel;

    public static void main(String[] args) throws Exception {
        readWordCounts();
        initDefinitionCache();
        readModel(new File(args[0]));
        createUser(args[1]);
        createGUI();
        nextWord();
        showGUI();
    }

    static void initDefinitionCache() throws IOException {
        if (sCachedDefinitionFile.exists()) {
            String defs = Files.readFromFile(sCachedDefinitionFile,
                                             "iso-8859-1");
            String[] lines = defs.split("\\n");
            for (int i = 0; i < lines.length; ++i) {
                String line = lines[i];
                int idx = line.indexOf(',');
                if (idx < 0) continue;
                String word = line.substring(0,idx);
                String def = line.substring(idx+1);
                sDefinitionCache.put(word,def);
            }
        }
        FileOutputStream fileOut = new FileOutputStream(sCachedDefinitionFile,true);
        sCacheWriter = new OutputStreamWriter(fileOut,"iso-8859-1");
        
    }

    static void back() {
        if (sWordStack.isEmpty()) {
            log("Error.  Back button should've been greyed out.");
            return;
        }
        String lastWord = sWordStack.pop();
        if (sWordStack.isEmpty())
            sBackButton.setEnabled(false);
        setWord(lastWord);
    }



    static void nextWord() {
        String currentWord = sWordTextLabel.getText();
        if (currentWord != null && currentWord.length() > 0) {
            sWordStack.push(currentWord);
            sBackButton.setEnabled(true);
        }
        while (sNextWord < sWords.length && sAnnotatedWords.contains(sWords[sNextWord]))
            ++sNextWord;
        if (sNextWord >= sWords.length) {
            log("Finished!");
            exit();
        }
        String word = sWords[sNextWord++];
        setWord(word);
    }

    static void readModel(File file) 
	throws IOException, ClassNotFoundException {

	System.out.println("Reading model.");
        sModel = (Em4.Model) AbstractExternalizable.readObject(file);
	System.out.println("     finished read.");
    }

    static void createUser(String userName) throws IOException {
        sUserName = userName;
        File stemFile = new File("users/" + userName + ".stm");
        if (stemFile.exists()) {
            String stems = Files.readFromFile(stemFile,"iso-8859-1");
            String[] lines = stems.split("\\n");
            for (int i = 0; i < lines.length; ++i) {
                int idx = lines[i].indexOf(',',2);
                String word = idx >= 0 ? lines[i].substring(2,idx) : lines[i].substring(2);
                sAnnotatedWords.add(word);
            }
        }
        FileOutputStream fileOut = new FileOutputStream(stemFile,true);
        sStemsWriter = new OutputStreamWriter(fileOut,"iso-8859-1");
    }

    static void setTextFields(String field1, String field2) {
        sTextFieldPanel.removeAll();
        if (field1 != null) {
            sTextFieldLabel1.setText(field1);
            sTextFieldPanel.add(sTextFieldLabel1,
                                new GridBagConstraints(0,0,1,1,1.0,0.0,
                                                       GridBagConstraints.NORTHWEST,
                                                       GridBagConstraints.HORIZONTAL,
                                                       new Insets(5,6,0,5),0,0));
            sTextFieldPanel.add(sTextField1,
                                new GridBagConstraints(0,1,1,1,1.0,0.0,
                                                       GridBagConstraints.NORTHWEST,
                                                       GridBagConstraints.HORIZONTAL,
                                                       new Insets(3,5,10,5),0,0));
            sTextField1.requestFocus();
        } 
        if (field2 != null) {
            sTextFieldLabel2.setText(field2);
            sTextFieldPanel.add(sTextFieldLabel2,
                                new GridBagConstraints(0,2,1,1,1.0,0.0,
                                                       GridBagConstraints.NORTHWEST,
                                                       GridBagConstraints.HORIZONTAL,
                                                       new Insets(5,6,0,5),0,0));
            sTextFieldPanel.add(sTextField2,
                                new GridBagConstraints(0,3,1,1,1.0,0.0,
                                                       GridBagConstraints.NORTHWEST,
                                                       GridBagConstraints.HORIZONTAL,
                                                       new Insets(3,5,10,5),0,0));
        }
        sTextFieldPanel.invalidate();
        sMainFrame.validate();
        sMainFrame.repaint();
    }

    static void setRoot() {
        sRootButton.setSelected(true);
        setTextFields(null,null);
    }

    static void setSuffix() {
        sSuffixButton.setSelected(true);
        setTextFields("Stem","Suffix");
    }

    static void setCompound() {
        sCompoundButton.setSelected(true);
        setTextFields("Stem 1","Stem 2");
    }

    static void setPrefix() {
        sPrefixButton.setSelected(true);
        setTextFields("Prefix","Stem");
    }

    static void setIrregular() {
        sIrregularButton.setSelected(true);
        setTextFields("Irregular",null);
    }

    static void setNonWord() {
        sNonWordButton.setSelected(true);
        setTextFields(null,null);
    }

    static void createGUI() throws IOException {
        sWordTextLabel = new JLabel("");
        sWordTextLabel.setFont(sWordToAnnotateFont);
        sTextFieldLabel1  = new JLabel("");
        sTextFieldLabel2 = new JLabel("");
        sTextField1 = new JTextField("",16);
        sTextField1.setFont(sTextFieldFont);
        sTextField2 = new JTextField("",16);
        sTextField2.setFont(sTextFieldFont);
        sTextFieldPanel = new JPanel(new GridBagLayout());

        sRootButton = new JRadioButton("Root");
        sSuffixButton = new JRadioButton("Suffix");
        sCompoundButton = new JRadioButton("Compound");
        sPrefixButton = new JRadioButton("Prefix");
        sIrregularButton = new JRadioButton("Irregular");
        sNonWordButton = new JRadioButton("Non-word");
        
        sRootButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setRoot();
                }
            });
        sSuffixButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSuffix();
                }
            });
        sCompoundButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setCompound();
                }
            });
        sPrefixButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setPrefix();
                }
            });
        sIrregularButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setIrregular();
                }
            });
        sNonWordButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setNonWord();
                }
            });

        sRootButton.setMnemonic(KeyEvent.VK_R);
        sSuffixButton.setMnemonic(KeyEvent.VK_S);
        sCompoundButton.setMnemonic(KeyEvent.VK_C);
        sPrefixButton.setMnemonic(KeyEvent.VK_P);
        sIrregularButton.setMnemonic(KeyEvent.VK_I);
        sNonWordButton.setMnemonic(KeyEvent.VK_N);


        sModeButtonGroup = new ButtonGroup();
        sModeButtonGroup.add(sRootButton);
        sModeButtonGroup.add(sSuffixButton);
        sModeButtonGroup.add(sCompoundButton);
        sModeButtonGroup.add(sPrefixButton);
        sModeButtonGroup.add(sIrregularButton);
        sModeButtonGroup.add(sNonWordButton);

        sModePanel = new JPanel(new GridBagLayout());
        sModePanel.add(sRootButton,
                       new GridBagConstraints(0,0,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(0,5,5,5),0,0));
        sModePanel.add(sSuffixButton,
                       new GridBagConstraints(0,1,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,5,0,5),0,0));
        sModePanel.add(sCompoundButton,
                       new GridBagConstraints(0,2,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,5,0,5),0,0));
        sModePanel.add(sPrefixButton,
                       new GridBagConstraints(0,3,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,5,0,5),0,0));
        sModePanel.add(sIrregularButton,
                       new GridBagConstraints(0,4,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,5,0,5),0,0));
        sModePanel.add(sNonWordButton,
                       new GridBagConstraints(0,5,1,1,0.0,0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5,5,5,5),0,0));
        sRootButton.setSelected(true);

        sOkButton = new JButton("OK");
        sOkButton.setMnemonic(KeyEvent.VK_O);
        sOkButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ok();
                }
            });

        ImageIcon backIcon 
            = new ImageIcon(new URL(Files.fileToURLName(new File("src/stemmers/Back24.gif"))));
        ImageIcon forwardIcon
            = new ImageIcon(new URL(Files.fileToURLName(new File("src/stemmers/Forward24.gif"))));


        sBackButton = new JButton("Back",backIcon);
        sBackButton.setHorizontalTextPosition(SwingConstants.TRAILING);
        sBackButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    back();
                }
            });
        sBackButton.setEnabled(false);

        sSkipButton = new JButton("Skip",forwardIcon);
        sSkipButton.setHorizontalTextPosition(SwingConstants.LEADING);
        sSkipButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    skip();
                }
            });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(sBackButton,
                         new GridBagConstraints(0,0,1,1,0.0,1.0,
                                                GridBagConstraints.NORTHWEST,
                                                GridBagConstraints.VERTICAL,
                                                new Insets(5,5,5,5),0,0));
        buttonPanel.add(sOkButton,
                         new GridBagConstraints(1,0,1,1,1.0,1.0,
                                                GridBagConstraints.NORTH,
                                                GridBagConstraints.BOTH,
                                                new Insets(5,5,5,5),0,0));
        buttonPanel.add(sSkipButton,
                         new GridBagConstraints(2,0,1,1,0.0,1.0,
                                                GridBagConstraints.NORTHEAST,
                                                GridBagConstraints.VERTICAL,
                                                new Insets(5,5,5,5),0,0));
                                                

        sDictPane = new JEditorPane();
	sDictPane.setEditable(false);
        sDictPane.setContentType("text/html");
        sDictScrollPane = new JScrollPane(sDictPane,
                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sCorpusProgressLabel = new JLabel("Corpus Progress");
        sCorpusProgressBar = new JProgressBar();
        sCorpusProgressBar.setMaximum(sWords.length);
        setProgress();

        sExpectedErrorProgressBar = new JProgressBar();

        sContentPanel = new JPanel();
        sContentPanel.setLayout(new GridBagLayout());

        sContentPanel.add(sWordTextLabel,
                          new GridBagConstraints(0,0,2,1,1.0,0.0,
                                                 GridBagConstraints.NORTHWEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(5,15,5,5),0,0));
        sContentPanel.add(sModePanel,
                          new GridBagConstraints(0,1,1,1,0.0,0.0,
                                                 GridBagConstraints.NORTHWEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5,5,5,5),0,0));

        sContentPanel.add(sTextFieldPanel,
                          new GridBagConstraints(1,1,1,1,1.0,0.0,
                                                 GridBagConstraints.NORTHWEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(5,5,5,5),0,0));
        sContentPanel.add(buttonPanel,
                          new GridBagConstraints(0,2,2,1,1.0,1.0,
                                                 GridBagConstraints.NORTH,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0,0,0,0),0,0));


        sContentPanel.add(sDictScrollPane,
                          new GridBagConstraints(2,0,1,3,1.0,1.0,
                                                 GridBagConstraints.NORTH,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(5,5,5,5),0,0));
        sContentPanel.add(sCorpusProgressLabel,
                          new GridBagConstraints(0,3,1,1,0.0,0.0,
                                                 GridBagConstraints.EAST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(25,10,15,5),0,0));
        sContentPanel.add(sCorpusProgressBar,
                          new GridBagConstraints(1,3,2,1,1.0,0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(25,5,15,5),0,0));
        
        sMainFrame = new JFrame("Stem Editor (" + sUserName + ")");
        sMainFrame.setContentPane(sContentPanel);
        sMainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exit();
                }
            });
        sMainFrame.setResizable(true);
    }

    static class Decision {
        final String mType;
        final String mWord;
        String mTextField1 = null;
        String mTextField2 = null;
        Decision(String type, String word) {
            mType = type;
            mWord = word;
        }

        public int hashCode() {
            return mWord.hashCode();
        }
        public boolean equals(Object that) {
            Decision thatDecision = (Decision) that;
            return mType.equals(thatDecision.mType)
                && mWord.equals(thatDecision.mWord)
                && mTextField1.equals(thatDecision.mTextField1)
                && mTextField2.equals(thatDecision.mTextField2);
        }
    }

    static void setProgress() {
        sCorpusProgressBar.setValue(sAnnotatedWords.size());
        sCorpusProgressBar.setStringPainted(true);
        
        sCorpusProgressBar.setString(sAnnotatedWords.size() + " / " + sWords.length
                                     + " (" 
                                     + (int)((100.0 * sAnnotatedWords.size()) / sWords.length)
                                     + "%)");
    }

    static String htmlDefinition(String word) {
        String def = sDefinitionCache.get(word);
        if (def != null) {
            System.out.println("found defintion in cache.");
            return def;
        }
        String urlString 
            = "http://www.merriam-webster.com/dictionary/" 
            + URLEncoder.encode(word);
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        try {
            URL url = new URL(urlString);
            in = url.openStream();
            reader = new InputStreamReader(in,"iso-8859-1");
            bufReader = new BufferedReader(reader);
            String line = null;
            while ((line = bufReader.readLine()) != null
                   && line.indexOf("class=\"word_definition\"") < 0) {
            }
            StringBuffer htmlBuf = new StringBuffer();
            while ((line = bufReader.readLine()) != null
                   && line.indexOf("</div>") < 0) {
                if (line.startsWith("Pronunciation")) continue;
                if (line.startsWith("Main Entry:"))
                    line = "<h2>" + line.substring("Main Entry: ".length()) + "</h2>";
                if (line.startsWith("Function:"))
                    line = "<b>" + line.substring("Function: ".length()) + "</b><br />";
                htmlBuf.append(line);
                htmlBuf.append(' ');
            }
            def = htmlBuf.toString();
            int mainIndex = def.indexOf("<h2>");
            if (mainIndex > 0)
                def = def.substring(mainIndex);
            def = def.replaceAll("<img[^>]*>","")
                .replaceAll("<a.[^>]*>","")
                .replaceAll("</a>","")
                .replaceAll("<font[^>]*>","")
                .replaceAll("</font>","");
            if (Strings.allWhitespace(def))
                def = "<b>No definition found.</b>";
            sDefinitionCache.put(word,def);
            sCacheWriter.write(word);
            sCacheWriter.write(',');
            sCacheWriter.write(def.replace('\n',' '));
            sCacheWriter.write('\n');
            return def;
        } catch (IOException e) {
            return "IOException=" + e;
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(reader);
            Streams.closeInputStream(in);
        }
    }


    static void setWord(String word) {
        sWordTextLabel.setText(word);
        String def = htmlDefinition(word);
        sDictPane.setText(def);
        sDictPane.setCaretPosition(0);

        // JScrollBar scrollBar =  sDictScrollPane.getVerticalScrollBar();
        // scrollBar.setValue(0);
        if (sDoneMap.containsKey(word)) {
            Decision decision = sDoneMap.get(word);
            sTextField1.setText(word);
            sTextField2.setText("");
            if (decision.mType.equals("R")) {
                setRoot();
            } else if (decision.mType.equals("S")) {
                setSuffix();
                sTextField1.setText(decision.mTextField1);
                sTextField2.setText(decision.mTextField2);
            } else if (decision.mType.equals("C")) {
                setCompound();
                sTextField1.setText(decision.mTextField1);
                sTextField2.setText(decision.mTextField2);
            } else if (decision.mType.equals("P")) {
                setPrefix();
                sTextField1.setText(decision.mTextField1);
                sTextField2.setText(decision.mTextField2);
            } else if (decision.mType.equals("I")) {
                setIrregular();
                sTextField1.setText(decision.mTextField1);
            } else if (decision.mType.equals("N")) {
                setNonWord();
            }
        } else {
	    Em4.StemScore firstBest = null;
            try {
		firstBest = sModel.firstBest(word);
		
	    } catch (Exception e) {
		System.out.println("Exception in finding first best.");
		firstBest = null;
	    }
            sTextField1.setText("");
            sTextField2.setText("");
            if (firstBest == null) {
                sTextField1.setText(word);
                setRoot();
                return;
            }
            String stem = firstBest.stem();
            String suffix = firstBest.suffix();
            sTextField1.setText(stem);
            if (suffix.length() == 0) {
                setRoot();
            } else {
                sTextField2.setText(suffix);
                setSuffix(); 
            }
        }
        sTextField1.setCaretPosition(0);
        sTextField2.setCaretPosition(0);
        sDictPane.invalidate();
        sContentPanel.validate();
        sContentPanel.repaint();
    }

    static void readWordCounts() throws IOException {
        sWordCounts = new ObjectToCounterMap();
        FileInputStream fileIn = new FileInputStream(sWordCountFile);
        InputStreamReader isReader 
            = new InputStreamReader(fileIn,Strings.UTF8);
        BufferedReader bufReader = new BufferedReader(isReader);
        String line;
        while ((line = bufReader.readLine()) != null)
            readCount(line);
        Streams.closeReader(bufReader);
        Object[] keysByCount = sWordCounts.keysOrderedByCount();
        sWords = new String[keysByCount.length];
        for (int i = 0; i < keysByCount.length; ++i)
            sWords[i] = keysByCount[i].toString();
    }
    static void readCount(String line) {
        if (line.length() == 0) return;
        int i = line.indexOf(' ');
        if (i < 0) {
            log("#Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
        int count = Integer.parseInt(line.substring(i+1));
        if (count < sMinWordCount) return;
        sWordCounts.set(word,count);
    }

    static void ok() {
        String word = sWordTextLabel.getText();
        if (sRootButton.isSelected())
            output("R",word,0);
        else if (sSuffixButton.isSelected()) {
            output("S",word,2);
        } else if (sCompoundButton.isSelected()) {
            output("C",word,2);
        } else if (sPrefixButton.isSelected()) {
            output("P",word,2);
        } else if (sIrregularButton.isSelected()) {
            output("I",word,1);
        } else if (sNonWordButton.isSelected()) {
            output("N",word,0);
        }
        sAnnotatedWords.add(word);
        setProgress();
        nextWord();
    }

    static void skip() {
        output("?",sWordTextLabel.getText(),0);
        nextWord();
    }


    static void output(String type, String word, int numFields) {
        Decision decision = new Decision(type,word);
        if (numFields >= 1) 
            decision.mTextField1 = sTextField1.getText();
        if (numFields >= 2)
            decision.mTextField2 = sTextField2.getText();
        sDoneMap.put(word,decision);
        try {
            sStemsWriter.write(type);
            sStemsWriter.write(',');
            sStemsWriter.write(word);
            if (numFields >= 1) {
                sStemsWriter.write(',');
                String val1 = sTextField1.getText();
                sStemsWriter.write(val1);
            }
            if (numFields >= 2) {
                sStemsWriter.write(',');
                String val2 = sTextField2.getText();
                sStemsWriter.write(val2);
            }
            sStemsWriter.write('\n');
            sStemsWriter.flush();
            

        } catch (IOException e) {
            log("IOException=" + e);
        }
    }



    static void showGUI() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sMainFrame.setVisible(true);
                    sMainFrame.setSize(600,400);
                    sMainFrame.validate();
                    sMainFrame.repaint();
                }
            });
    }

    static void exit() {
        log("Exiting.");
        Streams.closeWriter(sStemsWriter);
        Streams.closeWriter(sCacheWriter);
        System.exit(0);
    }

    static void log(String msg) {
        System.out.println(msg);
    }

}
