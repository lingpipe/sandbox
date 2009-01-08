import com.aliasi.chunk.*;
import com.aliasi.coref.*;
import com.aliasi.sentences.*;
import com.aliasi.tokenizer.*;
import com.aliasi.util.*;


import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Coref {

    static Pattern EN_PRONOUNS = Pattern.compile("He|he|Him|him");
    
    static File MODEL_FILE
	= new File("models/ne-en-news-muc.chunk");
    
    static TokenizerFactory TOK_FACTORY
	= new IndoEuropeanTokenizerFactory();

    static SentenceModel SENT_MODEL
	= new IndoEuropeanSentenceModel();



    public static void main(String[] args) 
	throws ClassNotFoundException, IOException {

	// create NE chunker
	FileInputStream fileIn = new FileInputStream(MODEL_FILE);
	BufferedInputStream bufIn 
	    = new BufferedInputStream(fileIn);
	ObjectInput objIn
	    = new ObjectInputStream(bufIn);
	HmmChunker neChunker = (HmmChunker) objIn.readObject();
	objIn.close();

	// create sentence chunker
	Chunker sentenceChunker 
	    = new SentenceChunker(TOK_FACTORY,SENT_MODEL);

	for (int i = 0; i < args.length; ++i) {
	    File doc = new File(args[i]);
	    String text = Files.readFromFile(doc);

	    // create coref instance per doc
	    MentionFactory mf = new EnglishMentionFactory();  
	    WithinDocCoref coref = new WithinDocCoref(mf);
	    
	    Chunking sentenceChunking
		= sentenceChunker.chunk(text);
	    
	    Iterator sentenceIt 
		= sentenceChunking.chunkSet().iterator();
	    for (int sentenceNum = 0; sentenceIt.hasNext(); ++sentenceNum) {
		Chunk sentenceChunk = (Chunk) sentenceIt.next();
		String sentenceText 
		    = text.substring(sentenceChunk.start(),
				     sentenceChunk.end());
		System.out.println("Sentence Text=" + sentenceText);
		
		Chunking mentionChunking
		    = neChunker.chunk(sentenceText);

		Set chunkSet = new TreeSet(Chunk.TEXT_ORDER_COMPARATOR);
		chunkSet.addAll(mentionChunking.chunkSet());
		
		java.util.regex.Matcher matcher = EN_PRONOUNS.matcher(sentenceText);
		int pos = 0;
		while (matcher.find(pos)) {
		    Chunk proChunk = ChunkFactory.createChunk(matcher.start(),
							      matcher.end(),
							      "MALE_PRONOUN");
		    Iterator it = chunkSet.iterator();
		    while (it.hasNext()) {
			Chunk chunk = (Chunk) it.next();
			if (overlap(chunk.start(),chunk.end(),
				    proChunk.start(),proChunk.end()))
			    it.remove();
		    }
		    chunkSet.add(proChunk);
		    pos = matcher.end();
		}


		
		Iterator mentionIt = chunkSet.iterator();
		while (mentionIt.hasNext()) {
		    Chunk mentionChunk = (Chunk) mentionIt.next();
		    String mentionText
			= sentenceText.substring(mentionChunk.start(),
						 mentionChunk.end());
		    String mentionType = mentionChunk.type();
		    Mention mention = mf.create(mentionText,mentionType);
		    int mentionId = coref.resolveMention(mention,sentenceNum);
		    System.out.println("     mention text=" + mentionText
				       + " type=" + mentionType
				       + " id=" + mentionId);
		}
	    }
	}
    }

    static boolean overlap(int start1, int end1,
			   int start2, int end2) {
	return java.lang.Math.max(start1,start2)
	    < java.lang.Math.min(end1,end2);
    }
}
