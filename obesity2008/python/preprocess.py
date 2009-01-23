#Carlos Cano 
#Ver 1.1 Date 18 jun 08


from string import strip, split, lower
import sys
import re
import os

negative_tokens = ["no", "not", "without"]
pronouns = {'MASC': ["he", "his", "him", "himself"], 'FEM': ["she", "her", "hers", "herself"]}




###############################################
## 1.- Normalization of disease names  	     ##
###############################################

def create_synonym_dict (file_disease_synonyms):
        """
        Parses a file with a list of synonyms per disease
        Input: filename
	Required File Format: 
		Example: 
			DiseaseA
			Synonym1, Synonym2, Synonym3
			Synonym4
						 
			DiseaseB 
			Synonym1
			Synonym2
			...
		Summary: '\n\n' delimits diseases. '\n' or ',' delimits different synonyms for the disease
        Output: Dictionary {diseaseX: [list of synonyms for diseaseX]}
        """
        f_synonyms = open(file_disease_synonyms, "r")
        f_synonyms_content = f_synonyms.read()
        f_synonyms.close()

        dictionary_synonyms = {}       #output var

        disease_blocks = f_synonyms_content.split("\n\n")      #list with a chunk of text for every disease
        for disease_block in disease_blocks:
                lines = disease_block.splitlines()
                disease_name = lines[0].strip()
		dictionary_synonyms[disease_name] = [disease_name]
                for line in lines[1:]:
                        for synonym in map(strip, line.split(',')):
                                synonym = synonym.lower()
                                dictionary_synonyms[disease_name].append(synonym)

        return dictionary_synonyms


	
def replace_synonym_occurrences (medical_records, dictionary_synonyms):
	"""
	Function that replaces disease occurrences in medical_records with their standard name 
	according to dictionary_synonyms
	Input: 
		medical_records: text
		dictionary_synonyms: Dictionary {diseaseX: [list of synonyms for diseaseX]}
        Output: string with the resultant text
        """

	for disease in dictionary_synonyms.keys():
		expr = '|'.join([r'\b'+(r'\s'.join(synonym.split()))+r'\b' for synonym in dictionary_synonyms[disease]])
		#print expr
		pattern_search = re.compile (expr, re.IGNORECASE)
		medical_records = pattern_search.sub(disease, medical_records)

	return medical_records
	

###############################################
## 2.- Addition of DRUG categories  	     ##
###############################################

def create_drug_dict (file_drugs):
        """
        Parses a file with a list of drugs per disease
        Input: filename. 
	Required File Format: 
		Example: 
			DiseaseA
			DrugA1, DrugA2, DrugA3
			DrugA4
			DrugA5
			 
			DiseaseB 
			...
		Summary: '\n\n' delimits diseases. '\n' or ',' delimits different drugs for the disease
        Output: Dictionary {drugX: [list of diseases for drugX]}
        """
        f_drugs = open(file_drugs, "r")
        f_drugs_content = f_drugs.read()
        f_drugs.close()

        dictionary_drugs = {}   #output var

        disease_blocks = f_drugs_content.split("\n\n")  #list with a chunk of text for every disease
        for disease_block in disease_blocks:
                lines = disease_block.splitlines()
                disease_name = lines[0].strip()
                for line in lines[1:]:
                        for drug in map(strip, line.split(',')):
                                drug = drug.lower()
                                if (dictionary_drugs.has_key(drug)):
                                        if (disease_name not in dictionary_drugs[drug]):
                                                dictionary_drugs[drug].append(disease_name)
                                else:
                                        dictionary_drugs[drug] = [disease_name]
        return dictionary_drugs


def replace_drug_occurrences (medical_records, dictionary_drugs):
        """
	For every occurrence in medical_records of a drug in dictionary_drugs, this function adds DISEASEX_DRUG 
	after that occurrence in the text, for all the DISEASEX associated to that drug according to dictionary_drug
       	Input: 
		medical_records: text
		dictionary_drugs: Dictionary {drugX: [list of diseases for drugX]}
	Output: string with the resultant text
        """

        regexp = r'\b%s\b'
        for drug in dictionary_drugs.keys():
                drug_pattern = re.compile(regexp % drug, re.IGNORECASE)
                medical_records = drug_pattern.sub(drug+" "+(" ".join(disease+"_DRUG" for disease in dictionary_drugs[drug])), medical_records)
                #medical_records= drug_pattern.sub((" ".join(disease+"_DRUG" for disease in dictionary_drugs[drug])), medical_records)

        return medical_records

###############################################
## 3.- Functions for the addition of GENDER  ##
###############################################

def guess_gender (text, dict_pronouns):
	"""
	Input: 
		text is the text containing ONE patient record
		dict_pronouns: dictionary {'MASC': [list of masc pronouns], 'FEM': [list fem. pronouns]}
	Output: the function returns the key ('MASC' or 'FEM') which has more occurrences of the 
		associated pronouns in the text
	"""
	hints = {}
	max = ('', 0)
	for key in dict_pronouns.keys():
		expr = '|'.join([r'\b'+pronoun+r'\b' for pronoun in dict_pronouns[key]])
		pattern_search = re.compile (expr, re.IGNORECASE)
		hints[key] = len(pattern_search.findall(text))
		if (hints[key] > max[1]):
			max = (key, hints[key])
	
	return max[0]
	


def add_gender (records, dict_pronouns):
	"""
	Function that adds GENDER_MASC or GENDER_FEM to the text of the records according
	to the mayority presence of masc. or fem. pronouns in the text. The modified text is returned. 
	Input: 
		records: content of the file with the records for all the patients
		dict_pronouns: dictionary {'MASC': [list of masc pronouns], 'FEM': [list fem. pronouns]}
	Output: string with the resultant text. The label GENDER_X is added at the begining of the text. 
	"""

	expr_search = r'(<doc id=\")(.*?)(\">.*?<text>)(.*?)(</text>.*?</doc>)'
	pattern_search  = re.compile (expr_search, re.DOTALL)
	matches = pattern_search.findall (records)
	for match in matches: 
		gender = guess_gender (match[3], dict_pronouns)
		new_expr_search = r'(<doc id=\")(%s)(\">.*?<text>)(.*?)(</text>.*?</doc>)'
		new_pattern_search = re.compile (new_expr_search % match[1], re.DOTALL)
		records = new_pattern_search.sub (''.join(match[0:3])+" GENDER_"+gender+" "+''.join(match[3:]) , records)

	return records

###############################################
## 4.- Addition of Binary Negated Tokens     ##
###############################################

def negate_token(mo):
        if ((mo.group(0)).lower() in negative_tokens) or (mo.group(0)[0:3] == "NO_"):
                return mo.group(0)
        else :
                return "NO_"+mo.group(0)


def negate_sequence (mo_list):
        pattern2 = re.compile(r'\b(\S+?)\b')
        return mo_list.group(0)+" [ "+pattern2.sub(negate_token, mo_list.group(1), 10)+" ]"


def add_negated_tokens (records, negative_tokens):
	"""
	Function that adds binary negation tokens with the prefix "NO_": 
		not have increased cholesterol -> not, have, increased, cholesterol, -> no_have, no_increased, no_cholesterol 
	Input: 
		records: string with the text
		negative_tokens: list of the negative tokens to consider
        Output: string with the resultant text. The binary negation tokens are added after the actual sentence
	in []. For example, if the input text is "not have increased cholesterol", the text "not have increased 
	cholesterol [NO_have NO_increased NO_cholesterol]" is returned. 
        """
        regexp = r'\b%s\b(.*?)[.;]'

        for word in negative_tokens:
                pattern = re.compile(regexp%word , re.IGNORECASE|re.DOTALL)
                records = pattern.sub(negate_sequence, records)
        return records





###############################################
## Some other utilities			     ##
###############################################

def replace_special_symbols (text):
	text = text.replace ('&amp;', 'and')
	text = text.replace('&lt;', 'less than')
	text = text.replace('&gt;', 'greater than')
	text = text.replace('&apos;', "" )
	text = text.replace('&quot;', "" )

	return text

def print_dict (dict, filename):
        file = open (filename, "w")
        for key in dict.keys():
                file.write(key+":")
                for elem in dict[key]:
                        file.write("\t"+elem)
                file.write("\n")
        file.close()

def create_output_filename (file_medical_records):
        """Creates the output file name from the input file name, adding "_preprocessed" before the extension. """
        aux = file_medical_records.split('.')
        output_file_name = '.'.join(aux[:-1])+"_preprocessed."+aux[-1]
        return output_file_name



###############################################
## MAIN 				     ##
###############################################
def helpProgram():
	print "\nCorrect usage:\n\tpython preprocess.py <FileIn> <FileOut> [-n|-normalize <FileSynonyms>] [-d|-drug <FileDrugs>] [-g|-gender] [-b|-binary] [-v] [-h]"
	print "\t-n|-normalize : Normalizes diseases according to the information provided in <FileSynonyms>"
	print "\t-d|-drug : Adds drug categories according to <FileDrugs>"
	print "\t-g|-gender : Adds gender guess"
	print "\t-b|-binary : Adds binary negated tokens"
	print "\t-v : Verbose Mode"
	print "\t-h : Prints this help"

if __name__ == '__main__':
	
	if len(sys.argv) < 3:
		helpProgram()
		sys.exit(-1)

	file_medical_records =  sys.argv[1]	
	if not os.path.isfile(file_medical_records):	
		print "Cannot find file: " + file_medical_records
		sys.exit(-1)
	
	output_file = sys.argv[2]		
		
	#previous values used for the files: 
	#file_medical_records = "../obesity_patient_records_training2.xml"
	#output_file = "obesity_patient_records_training2_preprocessed.xml"
	#file_drugs = "../lists/drug_list3.txt"
	#file_diseases = "../lists/disease_synonyms.txt"		
	
	normalize = False
	file_diseases = None
	drug = False
	file_drugs = None
	gender = False
	binaryNegatedTokens = False
	verbose = False
	
	i = 3
	while i < len(sys.argv):
	
		if (sys.argv[i] == "-v" or sys.argv[i] == "-verbose"):
			verbose = True
		elif (sys.argv[i] == "-n" or sys.argv[i] == "-normalize"):
			normalize = True
			i+=1
			file_diseases = sys.argv[i]	
			if not os.path.isfile(file_diseases):
				print "Cannot find file: " + file_diseases
				sys.exit(-1)
		elif (sys.argv[i] == "-d" or sys.argv[i] == "-drug"):
			drug = True
			i+=1
			file_drugs = sys.argv[i]	
			if not os.path.isfile(file_drugs):
				print "Cannot find file: " + file_drugs
				sys.exit(-1)
		elif (sys.argv[i] == "-g" or sys.argv[i] == "-gender"):
			gender = True
		elif (sys.argv[i] == "-b" or sys.argv[i] == "-binary"):
			binaryNegatedTokens = True
		elif sys.argv[i] == "-h":
			helpProgram()
			sys.exit(-1)
		else:
			print "Unknown flag " + sys.argv[i]
			helpProgram()
			sys.exit(-1)
		i+=1


	if (not (normalize or drug or gender or binaryNegatedTokens)):
		print "No options specified: no preprocess needed"
		helpProgram()
		sys.exit(-1)
		

	f_records = open(file_medical_records, "r")
	records = f_records.read()
	f_records.close()
	
	records = replace_special_symbols (records)

	if normalize: 
		if verbose: sys.stderr.write( "Normalizing disease names ...")
		synonym_dict = create_synonym_dict (file_diseases)
		records = replace_synonym_occurrences (records, synonym_dict)
		if verbose: sys.stderr.write( "done!\n")
	if drug: 
		if verbose: sys.stderr.write("Adding drug categories ...")
		drug_dict = create_drug_dict (file_drugs)
		records = replace_drug_occurrences (records, drug_dict)
		if verbose: sys.stderr.write("done!\n")
	if gender: 
		if verbose: sys.stderr.write("Adding gender ...") 
		records = add_gender(records, pronouns )
		if verbose: sys.stderr.write("done!\n")
	if binaryNegatedTokens: 
		if verbose: sys.stderr.write("Adding binary negated tokens ...")
		records = add_negated_tokens (records, negative_tokens)
		if verbose: sys.stderr.write("done!\n")
			
	f_out = open (output_file, "w")
	f_out.write(records)
	f_out.close()
