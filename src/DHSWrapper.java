import java.util.ArrayList;
import java.util.List; 


/**
 * "wrapper" class for process of modifying cascade file. 
 * @author Clayton Marr
 *
 */

public class DHSWrapper {
	
	public final String INV_RESP_MSG =  "Invalid response. Please enter a valid response. Returning to hypothesis testing menu.";
	
	private List<SChange> hypCASC, baseCASC; 
	// hypCASC -- new cascade that we are progressively constructing while continually comparing against the "baseline", @varbl baseCASC
	
	private List<String[]> proposedChanges; 
	//TODO important variable here, explanation follows
	// each indexed String[] is form [curr time step, operation details]
	// this object is *kept sorted* by current form index
		// (IMPORTANT: equivalent to iterator hci, for hypCASC later)
	// operation may be either deletion or insertion 
	// both relocdation and modification are handled as deletion then insertion pairs. 
	// for deletion, the second slot simply holds the string "deletion"
	// whereas for insertion, the second index holds the string form of the SChange 
		// that is inserted there in hypCASC. 
	
	private List<String> propChNotes;
	// will be used to keep notes on changes that will be used in case they are "finalized" 
		// i.e. using automatic modification of the cascade file. 

	private int[] RULE_IND_MAP; //easy access maps indices of CASCADE to those in hypCASCADE.
		// -1 means deleted. 
	private int[] hypGoldLocs, hypBlackLocs; 
	private int originalLastMoment,
		NUM_GOLD_STAGES, NUM_BLACK_STAGES;
	private String[] goldStageNames, blackStageNames; 
		
	
	
	public DHSWrapper(Simulation baseSim)
	{
		proposedChanges = new ArrayList<String[]>(); 
		propChNotes = new ArrayList<String>(); 
		hypCASC = new ArrayList<SChange>(baseSim.CASCADE()); 
		baseCASC = new ArrayList<SChange>(baseSim.CASCADE());
		originalLastMoment = baseCASC.size(); 
		NUM_GOLD_STAGES = baseSim.NUM_GOLD_STAGES();
		NUM_BLACK_STAGES = baseSim.NUM_BLACK_STAGES(); 
		RULE_IND_MAP = new int[originalLastMoment + 1]; 
		hypGoldLocs = new int[NUM_GOLD_STAGES]; hypBlackLocs = new int[NUM_BLACK_STAGES]; 
		for (int i = 0; i < originalLastMoment+1; i++)	RULE_IND_MAP[i] = i; //initialize each.
		for (int i = 0; i < NUM_GOLD_STAGES; i++)	hypGoldLocs[i] = baseSim.getStageInstant(true, i);
		for (int i = 0; i < NUM_BLACK_STAGES; i++)	hypBlackLocs[i] = baseSim.getStageInstant(false, i); 
		
		
		
	}

}
