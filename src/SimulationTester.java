import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * for ensuring functionality of DHSAnalysis, ErrorAnalysis, FED, Simulation, and related classes, ultimately DiachronicSimulator as well. 
 * @author Clayton Marr
 *
 */

public class SimulationTester {

	private static final String SYMBS_LOC = "symbolDefs.csv";
	private static final String LEX_LOC = "DebugDummyLexicon.txt"; 
	private static final String CASC_LOC = "DumCasc"; 
	private static final String FI_LOC = "FeatImplications"; 
	private static double ID_WT = 0.5; 
	
	private static String[] featsByIndex; 
	private static HashMap<String, Integer> featIndices;

	private static boolean feats_weighted;
	private static double[] FT_WTS; 
	
	private static HashMap<String, String> phoneSymbToFeatsMap;
	private static HashMap<String, String[]> featImplications; 
	
	
	public static void main(String args[])
	{
		extractSymbDefs(); 
		extractFeatImpls();
		
		System.out.println("Creating SChangeFactory...");
		SChangeFactory theFactory = new SChangeFactory(phoneSymbToFeatsMap, featIndices, featImplications); 
		
		extractCascade(theFactory); 
	}
	
	
	
	private static void extractSymbDefs()
	{
		System.out.println("Collecting symbol definitions...");
		
		List<String> symbDefsLines = new ArrayList<String>();
		String nextLine; 
		
		try 
		{	File inFile = new File(SYMBS_LOC); 
			BufferedReader in = new BufferedReader ( new InputStreamReader (
				new FileInputStream(inFile), "UTF8")); 
			while((nextLine = in.readLine()) != null)	
				symbDefsLines.add(nextLine); 		
			
			in.close(); 
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("Encoding unsupported!");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception!");
			e.printStackTrace();
		}
		
		System.out.println("Symbol definitions extracted!");
		System.out.println("Length of symbDefsLines : "+symbDefsLines.size()); 
		

		//from the first line, extract the feature list and then the features for each symbol.
		featsByIndex = symbDefsLines.get(0).replace("SYMB,", "").split(""+DiachronicSimulator.FEAT_DELIM); 
		
		for(int fi = 0; fi < featsByIndex.length; fi++) featIndices.put(featsByIndex[fi], fi);
		
		//Now we check if the features have weights
		if (symbDefsLines.get(1).split(",")[0].equalsIgnoreCase("FEATURE_WEIGHTS"))
		{
			feats_weighted = true; 
			FT_WTS = new double[featsByIndex.length];
			String[] weightsByIndex = symbDefsLines.get(1).substring(symbDefsLines.get(1).indexOf(",")+1).split(",");
			for(int i = 0 ; i < featsByIndex.length; i++)
				FT_WTS[i] = Double.parseDouble(weightsByIndex[i]); 
			symbDefsLines.remove(1); 
		}	
		else	feats_weighted = false;
		
		//from the rest-- extract the symbol def each represents
		int li = 1; 
		while (li < symbDefsLines.size()) 
		{
			nextLine = symbDefsLines.get(li).replaceAll("\\s+", ""); //strip white space and invisible characters 
			int ind1stComma = nextLine.indexOf(DiachronicSimulator.FEAT_DELIM); 
			String symb = nextLine.substring(0, ind1stComma); 
			String[] featVals = nextLine.substring(ind1stComma+1).split(""+DiachronicSimulator.FEAT_DELIM); 		
			
			String intFeatVals = ""; 
			for(int fvi = 0; fvi < featVals.length; fvi++)
			{
				if(featVals[fvi].equals(""+DiachronicSimulator.MARK_POS))	intFeatVals+= DiachronicSimulator.POS_INT; 
				else if (featVals[fvi].equals(""+DiachronicSimulator.MARK_UNSPEC))	intFeatVals += DiachronicSimulator.UNSPEC_INT; 
				else if (featVals[fvi].equals(""+DiachronicSimulator.MARK_NEG))	intFeatVals += DiachronicSimulator.NEG_INT; 
				else	throw new Error("Error: unrecognized feature value, "+featVals[fvi]+" in line "+li);
			}
			
			phoneSymbToFeatsMap.put(symb, intFeatVals);
			li++; 
		}
	}
	
	public static void extractFeatImpls()
	{
		featImplications = new HashMap<String, String[]>(); 
		
		String nextLine; 
		
		System.out.println("Now extracting info from feature implications file...");
		
		List<String> featImplLines = new ArrayList<String>(); 
		
		try 
		{	BufferedReader in = new BufferedReader ( new InputStreamReader (
				new FileInputStream(featImplsLoc), "UTF-8")); 
			while((nextLine = in.readLine()) != null)	featImplLines.add(nextLine); 		
			in.close(); 
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("Encoding unsupported!");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception!");
			e.printStackTrace();
		}
		
		for(String filine : featImplLines)
		{
			String[] fisides = filine.split(""+IMPLICATION_DELIM); 
			featImplications.put(fisides[0], fisides[1].split(""+FEAT_DELIM));
		}
		
		System.out.println("Done extracting feature implications!");	
	}
	

	public static void extractCascade(SChangeFactory theFactory)
	{
		System.out.println("Now extracting diachronic sound change rules from rules file...");
		
		rulesByTimeInstant = new ArrayList<String>(); 

		String nextRuleLine;
		
		try 
		{	BufferedReader in = new BufferedReader ( new InputStreamReader ( 
				new FileInputStream(cascFileLoc), "UTF-8")); 
			
			while((nextRuleLine = in.readLine()) != null)
			{
				String lineWithoutComments = ""+nextRuleLine; 
				if (lineWithoutComments.contains(""+CMT_FLAG))
						lineWithoutComments = lineWithoutComments.substring(0,
								lineWithoutComments.indexOf(""+CMT_FLAG));
				if(!lineWithoutComments.trim().equals(""))	rulesByTimeInstant.add(lineWithoutComments); 
			}
			in.close();
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("Encoding unsupported!");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception!");
			e.printStackTrace();
		}
		
		//now filter out the stage name declaration lines.
		
		List<String> goldStageNameAndLocList = new ArrayList<String>(); //to be collected 
		//until the end of collection, at which point the appropriate arrays for the custom
		// stages will be created using this List. These ones will be compared to gold.
		List<String>blackStageNameAndLocList = new ArrayList<String>();
			// same as above, but will not be compared to gold. 
		
		goldStagesSet = false; blackStagesSet=false;  
				
		int rli = 0; 
		
		while (rli < rulesByTimeInstant.size())
		{
			String currRule = rulesByTimeInstant.get(rli); 
			
			if ( (""+GOLD_STAGENAME_FLAG+BLACK_STAGENAME_FLAG).contains(""+currRule.charAt(0)))
			{
				if (ignore_stages)	rulesByTimeInstant.remove(rli); 
				else if ( currRule.charAt(0) == GOLD_STAGENAME_FLAG)
				{
					goldStagesSet = true; 
					assert rli != 0: "Error: Stage set at the first line -- this is useless, redundant with the initial stage ";
					
					currRule = currRule.substring(1); 
					assert !currRule.contains(""+GOLD_STAGENAME_FLAG): 
						"Error: stage name flag <<"+GOLD_STAGENAME_FLAG+">> occuring in a place besides the first character in the rule line -- this is illegal: \n"+currRule; 
					assert !currRule.contains(STAGENAME_LOC_DELIM+""):
						"Error: illegal character found in name for custom stage -- <<"+STAGENAME_LOC_DELIM+">>";  
					goldStageNameAndLocList.add(""+currRule+STAGENAME_LOC_DELIM+rli);
					rulesByTimeInstant.remove(rli);  
				}
				else if (currRule.charAt(0) == BLACK_STAGENAME_FLAG)
				{
					blackStagesSet =true;
					currRule = currRule.substring(1); 
					assert !currRule.contains(STAGENAME_LOC_DELIM+""):
						"Error: illegal character found in name for custom stage -- <<"+STAGENAME_LOC_DELIM+">>";  
					blackStageNameAndLocList.add(""+currRule+STAGENAME_LOC_DELIM+rli);
					rulesByTimeInstant.remove(rli); 
				}
				else	rulesByTimeInstant.remove(rli); 
			}
			else	rli++;
		}
		
		NUM_GOLD_STAGES = goldStageNameAndLocList.size(); 
		NUM_BLACK_STAGES =blackStageNameAndLocList.size();
		
		System.out.println("Using "+NUM_GOLD_STAGES+" custom stages."); 
		
		if (NUM_GOLD_STAGES > 0)
		{
			System.out.print("Gold stages: ");
			for (String gs : goldStageNameAndLocList)
				System.out.print(gs.substring(0,gs.indexOf(STAGENAME_LOC_DELIM))+",");
			System.out.println(""); 
		}
		  
		if (NUM_BLACK_STAGES > 0)
		{
			System.out.print("Black stages:");
			for (String bs : blackStageNameAndLocList)
				System.out.print(bs.substring(0,bs.indexOf(STAGENAME_LOC_DELIM))+",");
			System.out.println(""); 
		}
		
		goldStageGoldLexica = new Lexicon[NUM_GOLD_STAGES]; 
		goldStageNames = new String[NUM_GOLD_STAGES];
		blackStageNames = new String[NUM_BLACK_STAGES];
		goldStageInstants = new int[NUM_GOLD_STAGES]; 
		blackStageInstants = new int[NUM_BLACK_STAGES]; 
		
		// parse the rules
		CASCADE = new ArrayList<SChange>();
		
		int cri = 0, gsgi =0 , bsgi = 0, next_gold = -1, next_black = -1;
		if (goldStagesSet)	next_gold = Integer.parseInt(goldStageNameAndLocList.get(gsgi).split(""+STAGENAME_LOC_DELIM)[1]);
		if (blackStagesSet)	next_black = Integer.parseInt(blackStageNameAndLocList.get(bsgi).split(""+STAGENAME_LOC_DELIM)[1]);
		
		for(String currRule : rulesByTimeInstant)
		{
			List<SChange> newShifts = theFactory.generateSoundChangesFromRule(currRule); 
			
			if(DEBUG_RULE_PROCESSING)
			{	System.out.println("Generating rules for rule number "+cri+" : "+currRule);
				for(SChange newShift : newShifts)
					System.out.println("SChange generated : "+newShift+", with type"+newShift.getClass());
			}
			CASCADE.addAll(theFactory.generateSoundChangesFromRule(currRule));
			
			if(goldStagesSet)
			{
				if (cri == next_gold)
				{
					goldStageNames[gsgi] = goldStageNameAndLocList.get(gsgi).split(""+STAGENAME_LOC_DELIM)[0];
					goldStageInstants[gsgi] = CASCADE.size();		
					gsgi += 1;
					if ( gsgi < NUM_GOLD_STAGES)
						next_gold = Integer.parseInt(goldStageNameAndLocList.get(gsgi).split(""+STAGENAME_LOC_DELIM)[1]);
				}
			}
			
			if(blackStagesSet)
			{
				if (cri == next_black)
				{
					blackStageNames[bsgi] = blackStageNameAndLocList.get(bsgi).split(""+STAGENAME_LOC_DELIM)[0];
					blackStageInstants[bsgi] = CASCADE.size();
					bsgi += 1;
					if (bsgi < NUM_BLACK_STAGES)
						next_black = Integer.parseInt(blackStageNameAndLocList.get(bsgi).split(""+STAGENAME_LOC_DELIM)[1]);
				}
			}
			
			cri++; 
		}
		
		System.out.println("Diachronic rules extracted. "); 
		
	}

	
}
