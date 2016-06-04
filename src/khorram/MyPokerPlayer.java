package khorram;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pokergame.*;

public class MyPokerPlayer implements PokerPlayer {
	// Changed to Map with key index and value list<PokerCard>
	private static List<List<PokerCard>> allPossibleHoleCards;
	// Keep track of how many other cards could exist.
	private static Map<Integer, Integer> stats;
	private int numChips;
	private String id;
	private List<PokerCard> holeCards;
	private List<PokerCard> communityCards;
	private static Map<String, ArrayList<Double>> preFlopPossibleOptions;
	private int sizeOfPot;
	static {
		
		// initialize HashMap, Synchronize it for thread-safety.
		// HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH
		stats = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		
		allPossibleHoleCards = new ArrayList<List<PokerCard>>();
		// All possible first cards
		for(int i = 0; i < 51; i++){
			// All all possible second cards
			for(int j = i + 1; j < 52; j++){
				
				// Set up first card
				PokerCard firstCard = new PokerCard(i);
				
				// Set up second card.
				PokerCard secondCard = new PokerCard(j);
				
				// Set up new list to merge firstCard and secondCard
				List<PokerCard> combo = new ArrayList<PokerCard>();
				
				// Add first card
				combo.add(firstCard);
				
				// Add second card
				combo.add(secondCard);
				
				// Add combo to main ArrayList
				allPossibleHoleCards.add(combo);
			}
		}
		preFlopPossibleOptions = new HashMap<String, ArrayList<Double>>();

		String flop = "AA 85.3 73.4 63.9 55.9 49.2 43.6 38.8 34.7 31.1; AKs 67.0 50.7 41.4 35.4 31.1 27.7 25.0 22.7 20.7; AKo 65.4 48.2 38.6 32.4 27.9 24.4 21.6 19.2 17.2; AQs 66.1 49.4 39.9 33.7 29.4 26.0 23.3 21.1 19.3; AQo 64.5 46.8 36.9 30.4 25.9 22.5 19.7 17.5 15.5; AJs 65.4 48.2 38.5 32.2 27.8 24.5 22.0 19.9 18.1; AJo 63.6 45.6 35.4 28.9 24.4 21.0 18.3 16.1 14.3; ATs 64.7 47.1 37.2 31.0 26.7 23.5 21.0 18.9 17.3; ATo 62.9 44.4 34.1 27.6 23.1 19.8 17.2 15.1 13.4; A9s 63.0 44.8 34.6 28.4 24.2 21.1 18.8 16.9 15.4; A9o 60.9 41.8 31.2 24.7 20.3 17.1 14.7 12.8 11.2; A8s 62.1 43.7 33.6 27.4 23.3 20.3 18.0 16.2 14.8; A8o 60.1 40.8 30.1 23.7 19.4 16.2 13.9 12.0 10.6; A7s 61.1 42.6 32.6 26.5 22.5 19.6 17.4 15.7 14.3; A7o 59.1 39.4 28.9 22.6 18.4 15.4 13.2 11.4 10.1; A6s 60.0 41.3 31.4 25.6 21.7 19.0 16.9 15.3 14.0; A6o 57.8 38.0 27.6 21.5 17.5 14.7 12.6 10.9 9.6; A5s 59.9 41.4 31.8 26.0 22.2 19.6 17.5 15.9 14.5; A5o 57.7 38.2 27.9 22.0 18.0 15.2 13.1 11.5 10.1; A4s 58.9 40.4 30.9 25.3 21.6 19.0 17.0 15.5 14.2; A4o 56.4 36.9 26.9 21.1 17.3 14.7 12.6 11.0 9.8; A3s 58.0 39.4 30.0 24.6 21.0 18.5 16.6 15.1 13.9; A3o 55.6 35.9 26.1 20.4 16.7 14.2 12.2 10.7 9.5; A2s 57.0 38.5 29.2 23.9 20.4 18.0 16.1 14.6 13.4; A2o 54.6 35.0 25.2 19.6 16.1 13.6 11.7 10.2 9.1; KK 82.4 68.9 58.2 49.8 43.0 37.5 32.9 29.2 26.1; KQs 63.4 47.1 38.2 32.5 28.3 25.1 22.5 20.4 18.6; KQo 61.4 44.4 35.2 29.3 25.1 21.8 19.1 16.9 15.1; KJs 62.6 45.9 36.8 31.1 26.9 23.8 21.3 19.3 17.6; KJo 60.6 43.1 33.6 27.6 23.5 20.2 17.7 15.6 13.9; KTs 61.9 44.9 35.7 29.9 25.8 22.8 20.4 18.5 16.9; KTo 59.9 42.0 32.5 26.5 22.3 19.2 16.7 14.7 13.1; K9s 60.0 42.4 32.9 27.2 23.2 20.3 18.1 16.3 14.8; K9o 58.0 39.5 29.6 23.6 19.5 16.5 14.1 12.3 10.8; K8s 58.5 40.2 30.8 25.1 21.3 18.6 16.5 14.8 13.5; K8o 56.3 37.2 27.3 21.4 17.4 14.6 12.5 10.8 9.4; K7s 57.8 39.4 30.1 24.5 20.8 18.1 16.0 14.5 13.2; K7o 55.4 36.1 26.3 20.5 16.7 13.9 11.8 10.2 9.0; K6s 56.8 38.4 29.1 23.7 20.1 17.5 15.6 14.0 12.8; K6o 54.3 35.0 25.3 19.7 16.0 13.3 11.3 9.8 8.6; K5s 55.8 37.4 28.2 23.0 19.5 17.0 15.2 13.7 12.5; K5o 53.3 34.0 24.5 19.0 15.4 12.9 11.0 9.5 8.3; K4s 54.7 36.4 27.4 22.3 19.0 16.6 14.8 13.4 12.3; K4o 52.1 32.8 23.4 18.1 14.7 12.3 10.5 9.1 8.0; K3s 53.8 35.5 26.7 21.7 18.4 16.2 14.5 13.1 12.1; K3o 51.2 31.9 22.7 17.6 14.2 11.9 10.2 8.9 7.8; K2s 52.9 34.6 26.0 21.2 18.1 15.9 14.3 13.0 11.9; K2o 50.2 30.9 21.8 16.9 13.7 11.5 9.8 8.6 7.6; QQ 79.9 64.9 53.5 44.7 37.9 32.5 28.3 24.9 22.2; QJs 60.3 44.1 35.6 30.1 26.1 23.0 20.7 18.7 17.1; QJo 58.2 41.4 32.6 26.9 22.9 19.8 17.3 15.3 13.7; QTs 59.5 43.1 34.6 29.1 25.2 22.3 19.9 18.1 16.6; QTo 57.4 40.2 31.3 25.7 21.6 18.6 16.3 14.4 12.9; Q9s 57.9 40.7 31.9 26.4 22.5 19.7 17.6 15.9 14.5; Q9o 55.5 37.6 28.5 22.9 19.0 16.1 13.8 12.1 10.7; Q8s 56.2 38.6 29.7 24.4 20.7 18.0 16.0 14.4 13.2; Q8o 53.8 35.4 26.2 20.6 16.9 14.1 12.1 10.5 9.2; Q7s 54.5 36.7 27.9 22.7 19.2 16.7 14.8 13.3 12.1; Q7o 51.9 33.2 24.0 18.6 15.1 12.5 10.6 9.2 8.0; Q6s 53.8 35.8 27.1 21.9 18.5 16.1 14.3 12.9 11.7; Q6o 51.1 32.3 23.2 17.9 14.4 12.0 10.1 8.8 7.6; Q5s 52.9 34.9 26.3 21.4 18.1 15.8 14.1 12.7 11.6; Q5o 50.2 31.3 22.3 17.3 13.9 11.6 9.8 8.5 7.4; Q4s 51.7 33.9 25.5 20.7 17.6 15.4 13.7 12.4 11.3; Q4o 49.0 30.2 21.4 16.4 13.3 11.0 9.4 8.1 7.1; Q3s 50.7 33.0 24.7 20.1 17.0 14.9 13.3 12.1 11.1; Q3o 47.9 29.2 20.7 15.9 12.8 10.7 9.1 7.9 6.9; Q2s 49.9 32.2 24.0 19.5 16.6 14.6 13.1 11.9 10.9; Q2o 47.0 28.4 19.9 15.3 12.3 10.3 8.8 7.7 6.8; JJ 77.5 61.2 49.2 40.3 33.6 28.5 24.6 21.6 19.3; JTs 57.5 41.9 33.8 28.5 24.7 21.9 19.7 17.9 16.5; JTo 55.4 39.0 30.7 25.3 21.5 18.6 16.3 14.5 13.1; J9s 55.8 39.6 31.3 26.1 22.4 19.7 17.6 15.9 14.6; J9o 53.4 36.5 27.9 22.5 18.7 15.9 13.8 12.1 10.8; J8s 54.2 37.5 29.1 24.0 20.5 17.9 15.9 14.4 13.2; J8o 51.7 34.2 25.6 20.4 16.8 14.1 12.2 10.7 9.5; J7s 52.4 35.4 27.1 22.2 18.9 16.4 14.6 13.2 12.0; J7o 49.9 32.1 23.5 18.3 14.9 12.4 10.6 9.2 8.1; J6s 50.8 33.6 25.4 20.6 17.4 15.2 13.5 12.1 11.1; J6o 47.9 29.8 21.4 16.5 13.2 11.0 9.3 8.0 7.0; J5s 50.0 32.8 24.7 20.0 17.0 14.7 13.1 11.8 10.8; J5o 47.1 29.1 20.7 15.9 12.8 10.6 8.9 7.7 6.7; J4s 49.0 31.8 24.0 19.4 16.4 14.3 12.8 11.5 10.6; J4o 46.1 28.1 19.9 15.3 12.3 10.2 8.6 7.5 6.5; J3s 47.9 30.9 23.2 18.8 16.0 14.0 12.5 11.3 10.4; J3o 45.0 27.1 19.1 14.6 11.7 9.8 8.3 7.2 6.3; J2s 47.1 30.1 22.6 18.3 15.6 13.7 12.2 11.1 10.2; J2o 44.0 26.2 18.4 14.1 11.3 9.4 8.0 7.0 6.2; TT 75.1 57.7 45.2 36.4 30.0 25.3 21.8 19.2 17.2; T9s 54.3 38.9 31.0 26.0 22.5 19.8 17.8 16.2 14.9; T9o 51.7 35.7 27.7 22.5 18.9 16.2 14.1 12.6 11.3; T8s 52.6 36.9 29.0 24.0 20.6 18.1 16.2 14.8 13.6; T8o 50.0 33.6 25.4 20.4 16.9 14.4 12.5 11.0 9.9; T7s 51.0 34.9 27.0 22.2 19.0 16.6 14.8 13.5 12.4; T7o 48.2 31.4 23.4 18.4 15.1 12.8 11.0 9.7 8.6; T6s 49.2 32.8 25.1 20.5 17.4 15.2 13.6 12.3 11.2; T6o 46.3 29.2 21.2 16.5 13.4 11.2 9.5 8.3 7.3; T5s 47.2 30.8 23.3 18.9 16.0 13.9 12.4 11.2 10.2; T5o 44.2 27.1 19.3 14.8 11.9 9.9 8.4 7.2 6.4; T4s 46.4 30.1 22.7 18.4 15.6 13.6 12.1 11.0 10.0; T4o 43.4 26.4 18.7 14.3 11.5 9.5 8.1 7.0 6.2; T3s 45.5 29.3 22.0 17.8 15.1 13.2 11.8 10.7 9.8; T3o 42.4 25.5 18.0 13.7 11.0 9.1 7.8 6.8 6.0; T2s 44.7 28.5 21.4 17.4 14.8 13.0 11.6 10.5 9.7; T2o 41.5 24.7 17.3 13.2 10.6 8.8 7.5 6.6 5.8; 99 72.1 53.5 41.1 32.6 26.6 22.4 19.4 17.2 15.6; 98s 51.1 36.0 28.5 23.6 20.2 17.8 15.9 14.5 13.4; 98o 48.4 32.9 25.1 20.1 16.6 14.2 12.3 10.9 9.9; 97s 49.5 34.2 26.8 22.1 18.9 16.6 14.9 13.6 12.5; 97o 46.7 30.9 23.1 18.4 15.1 12.8 11.1 9.8 8.8; 96s 47.7 32.3 24.9 20.4 17.4 15.3 13.7 12.4 11.4; 96o 44.9 28.8 21.2 16.6 13.5 11.4 9.8 8.7 7.8; 95s 45.9 30.4 23.2 18.8 16.0 13.9 12.4 11.3 10.3; 95o 42.9 26.7 19.2 14.8 12.0 10.0 8.5 7.4 6.6; 94s 43.8 28.4 21.3 17.3 14.6 12.7 11.3 10.3 9.4; 94o 40.7 24.6 17.3 13.2 10.5 8.7 7.3 6.4 5.6; 93s 43.2 27.8 20.8 16.8 14.3 12.5 11.1 10.1 9.2; 93o 39.9 23.9 16.7 12.7 10.1 8.3 7.1 6.1 5.4; 92s 42.3 27.0 20.2 16.4 13.9 12.2 10.9 9.9 9.1; 92o 38.9 22.9 16.0 12.1 9.6 8.0 6.8 5.9 5.2; 88 69.1 49.9 37.5 29.4 24.0 20.3 17.7 15.8 14.4; 87s 48.2 33.9 26.6 22.0 18.9 16.7 15.0 13.7 12.7; 87o 45.5 30.6 23.2 18.5 15.4 13.1 11.5 10.3 9.3; 86s 46.5 32.0 25.0 20.6 17.6 15.6 14.1 12.9 11.9; 86o 43.6 28.6 21.3 16.9 13.9 11.8 10.4 9.2 8.3; 85s 44.8 30.2 23.2 19.1 16.3 14.3 12.9 11.8 10.9; 85o 41.7 26.5 19.4 15.2 12.4 10.5 9.1 8.1 7.3; 84s 42.7 28.1 21.4 17.4 14.8 13.0 11.7 10.6 9.8; 84o 39.6 24.4 17.5 13.4 10.8 9.0 7.8 6.8 6.1; 83s 40.8 26.3 19.8 16.0 13.6 11.9 10.7 9.7 8.9; 83o 37.5 22.4 15.7 11.9 9.5 7.9 6.7 5.8 5.1; 82s 40.3 25.8 19.4 15.7 13.3 11.7 10.5 9.6 8.8; 82o 36.8 21.7 15.1 11.4 9.1 7.5 6.4 5.6 4.9; 77 66.2 46.4 34.4 26.8 21.9 18.6 16.4 14.8 13.7; 76s 45.7 32.0 25.1 20.8 18.0 15.9 14.4 13.2 12.3; 76o 42.7 28.5 21.5 17.1 14.2 12.2 10.8 9.6 8.8; 75s 43.8 30.1 23.4 19.4 16.7 14.8 13.4 12.3 11.4; 75o 40.8 26.5 19.7 15.5 12.8 11.0 9.7 8.7 7.9; 74s 41.8 28.2 21.7 17.9 15.3 13.5 12.2 11.2 10.4; 74o 38.6 24.5 17.9 13.9 11.4 9.7 8.5 7.6 6.8; 73s 40.0 26.3 20.0 16.4 14.0 12.3 11.1 10.1 9.3; 73o 36.6 22.4 16.0 12.3 9.9 8.4 7.2 6.4 5.7; 72s 38.1 24.5 18.4 15.0 12.8 11.2 10.1 9.2 8.5; 72o 34.6 20.4 14.2 10.7 8.6 7.2 6.1 5.4 4.8; 66 63.3 43.2 31.5 24.5 20.1 17.3 15.4 14.0 13.1; 65s 43.2 30.2 23.7 19.7 17.0 15.2 13.8 12.7 11.9; 65o 40.1 26.7 20.0 15.9 13.3 11.5 10.2 9.2 8.5; 64s 41.4 28.5 22.1 18.4 15.9 14.2 12.9 11.9 11.1; 64o 38.0 24.7 18.2 14.4 12.0 10.3 9.2 8.3 7.6; 63s 39.4 26.5 20.4 16.8 14.5 12.9 11.7 10.8 10.0; 63o 35.9 22.7 16.4 12.8 10.6 9.1 8.0 7.2 6.5; 62s 37.5 24.8 18.8 15.4 13.3 11.8 10.7 9.8 9.1; 62o 34.0 20.7 14.6 11.2 9.1 7.8 6.8 6.0 5.4; 55 60.3 40.1 28.8 22.4 18.5 16.0 14.4 13.2 12.3; 54s 41.1 28.8 22.6 18.9 16.5 14.8 13.5 12.5 11.7; 54o 37.9 25.2 18.8 15.0 12.6 11.0 9.8 8.9 8.2; 53s 39.3 27.1 21.1 17.5 15.2 13.7 12.5 11.6 10.8; 53o 35.8 23.3 17.1 13.6 11.4 9.9 8.8 8.0 7.3; 52s 37.5 25.3 19.5 16.1 14.0 12.5 11.4 10.6 9.8; 52o 33.9 21.3 15.3 12.0 10.0 8.6 7.6 6.8 6.2; 44 57.0 36.8 26.3 20.6 17.3 15.2 13.9 12.9 12.1; 43s 38.0 26.2 20.3 16.9 14.7 13.1 12.0 11.1 10.3; 43o 34.4 22.3 16.3 12.8 10.7 9.3 8.3 7.5 6.8; 42s 36.3 24.6 18.8 15.7 13.7 12.3 11.2 10.4 9.6; 42o 32.5 20.5 14.7 11.5 9.5 8.3 7.3 6.6 6.0; 33 53.7 33.5 23.9 19.0 16.2 14.6 13.5 12.6 12.0; 32s 35.1 23.6 18.0 14.9 13.0 11.7 10.7 9.9 9.2; 32o 31.2 19.5 13.9 10.8 8.9 7.7 6.8 6.1 5.6; 22 50.3 30.7 22.0 17.8 15.5 14.2 13.3 12.5 12.0";
		String[] prefloparr = flop.split("; ");
		System.out.println(Arrays.toString(prefloparr[0].split(" ")));
		int sum = 0;
		for(int i = 0; i < prefloparr.length; i++){
			sum++;
			// line is holds (e.g.) : [AA, 85.3, 73.4, 63.9, 55.9, 49.2, 43.6, 38.8, 34.7, 31.1]
			String[] line = prefloparr[i].split(" ");

			// Create a key variable
			String key = line[0];

			// Create  a probablityHold list of values (e.g.) : 85.3, 73.4, 63.9, 55.9, 49.2, 43.6, 38.8, 34.7, 31.1 (Double)
			ArrayList<Double> probablityHold = new ArrayList<Double>();


			// Loop through each item in line, we can skip the first element
			for(int j = 1; j < line.length; j++){
				double parseString = Double.parseDouble(line[j]);
				probablityHold.add(parseString);
			}

			// Done looping through all items in row, add to map.
			preFlopPossibleOptions.put(key, probablityHold);
		}
		System.out.println(preFlopPossibleOptions);
		System.out.println("How many times called: " + sum);
	}

	public MyPokerPlayer(){
		holeCards = new ArrayList<PokerCard>();
		communityCards = new ArrayList<PokerCard>();
	}


	@Override
	public void onEvent(PokerEvent e) {
		if(e instanceof PokerNewCardEvent){
			PokerNewCardEvent card = (PokerNewCardEvent) e;
			if(card.faceUp()){ // True for community card
				communityCards.add(card.card());
			} else {
				holeCards.add(card.card());
			}
		} else if(e instanceof PokerNewHandEvent){
			// Clear out all cards from all sets
			holeCards.clear();
			communityCards.clear();
		}
	}

	@Override
	public int numChips() {
		return numChips;
	}
	/*
	 * (non-Javadoc)
	 * @see khorram.PokerPlayer#collectChips(int)
	 * 
	 * Removing chips
	 */
	@Override
	public void collectChips(int numChips) {
		this.numChips -= numChips;
	}
	/*
	 * (non-Javadoc)
	 * @see khorram.PokerPlayer#acceptChips(int)
	 * 
	 * adding chips
	 */
	@Override
	public void acceptChips(int numChips) {
		this.numChips += numChips;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public PokerDecision decide(PokerGameDetails game, int betRequiredToCall) {
		
		try {
			// Get the size of the pot
			sizeOfPot = game.sizePot();

			System.out.println("bet required to call: " + betRequiredToCall + " sizeof pot: " + sizeOfPot + " my chips: " + numChips);

			// Sort all hole cards
			Collections.sort(holeCards, Collections.reverseOrder());

			// Sort all community cards
			Collections.sort(communityCards, Collections.reverseOrder());

			// Get array of current players in match
			PokerPlayerDetails[] numberOfPlayers = game.remainingPlayer();

			// Get the exact amount of all players still playing
			int opponents = numberOfPlayers.length - 2; // - 1 To remove self from list of _all_ players. -2 because of 0 based index

			// probability rating, to be used at the end to determine what move to play.
			// 100 = MAX RISK
			// 0 = LOWEST RISK
			Double probability = 0.00;

			// Calculate what to raise by.
			// Should be multiple of betRequiredToCall
			boolean highRisk = false;

			if(game.round().toString().equals("PREFLOP")){ // Game is in PREFLOP

				// First Hole Card
				PokerCard firstCard = holeCards.get(0);

				// Second hole card
				PokerCard secondCard = holeCards.get(1);


				// String hand to build, key for hashmap.
				String generateKey = "";

				// Check both ranks
				if(firstCard.rank() == secondCard.rank()){

					// Ranks are the same, just add both ranks straight up.
					generateKey = firstCard.rank() + "" + secondCard.rank();
				} else if (firstCard.suit() == secondCard.suit()){

					// Both cards have same suit, just append as 's' to generateKey and good to go.
					generateKey = firstCard.rank() + "" + secondCard.rank() + "s";
				} else {
					// Cards are _not_ of the same rank or suit
					generateKey = firstCard.rank() + "" + secondCard.rank() + "o";				
				}

				// Use generateKey to unlock possible plays
				probability = preFlopPossibleOptions.get(generateKey).get(opponents);

				// If betRequiredToCall is set at 0, then set this to the big blind.
				if(betRequiredToCall == 0){
					betRequiredToCall = game.bigBlind();
				}


				// If probability is too low, then just fold.
				if(probability < 20.0){
					return new PokerDecision(PokerDecision.TYPE.FOLD);
				}

				// Risk option, be careful.
				if(numChips < 50){
					highRisk = true;
				}

				if(probability > 50.0){ // Very confident

					// But still high risk.
					if(highRisk){

						// Still play it safe, Call.
						return new PokerDecision(PokerDecision.TYPE.CALL);
					}

					// not high risk, raise with good cards.
					// If I can raise blind by *[4-0), do it.
					int raiseAmount = raiseDecision(betRequiredToCall); // betRequiredToCall will ALWAYS be > 0
					
					// Can't raise any further, only option..
					if(raiseAmount - betRequiredToCall == 0){
						return new PokerDecision(PokerDecision.TYPE.CALL);
					}

					return new PokerDecision(PokerDecision.TYPE.RAISE, raiseAmount - betRequiredToCall);
				} // End of high-probability

				// probability less than 50% and more than 20%
				// Never raise from this, only Call or Fold.
				else if(probability > 20){

					// Check again if high risk
					if(highRisk && numChips > 450){
						// If high risk and this low of probability, just fold.
						return new PokerDecision(PokerDecision.TYPE.FOLD);
					}

					// Otherwise, call is okay in this case.
					return new PokerDecision(PokerDecision.TYPE.CALL);
				} else { // Probability super low, less than 20%
					return new PokerDecision(PokerDecision.TYPE.FOLD);
				}

			} else { // For Flop, River or Turn [0 - 8]
				
				// Get my best hand
				PokerHand bestHand = bestHand();
				
				// parallel processing example:
				allPossibleHoleCards.stream()
				.parallel()
				.map(set -> bestHandCustom(set)) // creates a list of PokerHand combinations.
				.forEach(e -> stats.compute(e.Category().ordinal(), (k, v) -> v == null ? 1 : v + 1));
				
				
				System.out.println(stats);				
				// If I can check, do that instead.
				if(betRequiredToCall == 0){
					return new PokerDecision(PokerDecision.TYPE.CALL);
				}

				// I'm running low on chips.
				if(numChips < 200 && numChips > 0){
					// Check the size of the pot, might be worth it.
					// > 600, large pot.
					if(game.sizePot() > 1000){
						
						// Go all in.
						// set raiseAll amount.
						int raiseAll = numChips - betRequiredToCall;
						
						// Check if raiseAll is 0
						if(raiseAll < 1){
							
							// can't raise by 0, numChips is same as betRequiredToCall. Just call instead
							return new PokerDecision(PokerDecision.TYPE.CALL);
						}
						
						// raiseAll is > 0, now we can go all in.
						return new PokerDecision(PokerDecision.TYPE.RAISE, numChips - betRequiredToCall);
					}

					// counldn't check, just fold.
					return new PokerDecision(PokerDecision.TYPE.CALL);
				}

				// Awful hand, just fold. very low chance of success, and still have good amount of chips in stack.
				if(bestHand.Category().ordinal() < 1){
					// Wait, I actually have quite a few chips..
					if(numChips > 250){
						return new PokerDecision(PokerDecision.TYPE.CALL);
					}
					
					return new PokerDecision(PokerDecision.TYPE.CALL);

				} else if (bestHand.Category().ordinal() < 4){ // > 1 and < 4. OKAY cards

					// Just Call
					return new PokerDecision(PokerDecision.TYPE.CALL);

				} else { // >= 4, GREAT cards.
					int raiseAmount = raiseDecision(betRequiredToCall); // betRequiredToCall will ALWAYS be > 0

					return new PokerDecision(PokerDecision.TYPE.RAISE, raiseAmount - betRequiredToCall);
				}
			}


		} catch (Exception e){
			e.printStackTrace();
			return null;
		}

	}

	public int raiseDecision(int betRequiredToCall){
		System.out.println(betRequiredToCall);
		int raiseN = 0;
		if(numChips >= betRequiredToCall * 4){
			raiseN = betRequiredToCall * 4;
		} else if(numChips >= betRequiredToCall * 3){
			raiseN = betRequiredToCall * 3;
		} else if(numChips >= betRequiredToCall * 2){
			raiseN = betRequiredToCall * 2;
		} else {
			raiseN = betRequiredToCall;
		}
		return raiseN;
	}

	/*
	 * SizeOfPot && Amount bet
	 * Looks at amount of cards player still has, and returns a calculated risk.
	 */


	@Override
	public PokerHand bestHand() {
		try {
			int handSize = communityCards.size() + holeCards.size();
			PokerHand StrongestPossiblePokerHand = null;
			if (handSize == 5) {
				String hand = PokerHandStringify(Stream.concat(communityCards.stream(), holeCards.stream()).collect(Collectors.toList()));
				StrongestPossiblePokerHand = new PokerHand(hand);
				System.out.println(StrongestPossiblePokerHand + " in handSize 5!!");
				return StrongestPossiblePokerHand;
			} else if (handSize == 6){
				// Get strongest hand and put in single PokerHand
				StrongestPossiblePokerHand = getAllCardsFromSixCards();
				System.out.println(StrongestPossiblePokerHand + " in handSize 6!!");
				return new PokerHand(StrongestPossiblePokerHand.toString().substring(0, StrongestPossiblePokerHand.toString().indexOf(",")).trim());
			} else { // (hopefully) 7
				StrongestPossiblePokerHand = getAllCardsFromSevenCards();
				System.out.println(StrongestPossiblePokerHand + " in handSize 7!!");
				return new PokerHand(StrongestPossiblePokerHand.toString().substring(0, StrongestPossiblePokerHand.toString().indexOf(",")).trim());
			}
		} catch (Exception E){
			System.out.println(E);
			return null;
		}
	}
	
	public PokerHand bestHandCustom(List<PokerCard> holeCards) {
		try {
			int handSize = communityCards.size() + holeCards.size();
			PokerHand StrongestPossiblePokerHand = null;
			if (handSize == 5) {
				String hand = PokerHandStringify(Stream.concat(communityCards.stream(), holeCards.stream()).collect(Collectors.toList()));
				StrongestPossiblePokerHand = new PokerHand(hand);
//				System.out.println(StrongestPossiblePokerHand + " in handSize 5!!");
				return StrongestPossiblePokerHand;
			} else if (handSize == 6){
				// Get strongest hand and put in single PokerHand
				StrongestPossiblePokerHand = getAllCardsFromSixCards();
//				System.out.println(StrongestPossiblePokerHand + " in handSize 6!!");
				return new PokerHand(StrongestPossiblePokerHand.toString().substring(0, StrongestPossiblePokerHand.toString().indexOf(",")).trim());
			} else { // (hopefully) 7
				StrongestPossiblePokerHand = getAllCardsFromSevenCards();
//				System.out.println(StrongestPossiblePokerHand + " in handSize 7!!");
				return new PokerHand(StrongestPossiblePokerHand.toString().substring(0, StrongestPossiblePokerHand.toString().indexOf(",")).trim());
			}
		} catch (Exception E){
			System.out.println(E);
			return null;
		}
	}

	public String PokerHandStringify(List<PokerCard> allCards){
		String buildUp = "";
		for(PokerCard card : allCards){
			if (buildUp.equals("")){
				buildUp += card.toString();
			} else {
				buildUp += " " + card.toString();
			}
		}
		return buildUp;
	}

	public PokerHand getAllCardsFromSevenCards(){
		// Set up hands, these will include all possible variations.
		List<PokerHand> hands = new ArrayList<PokerHand>();

		// compile complete list of hands
		List<PokerCard> allCards = Stream.concat(communityCards.stream(), holeCards.stream()).collect(Collectors.toList());

		// Get all variations
		// 7 possible cards, 
		for(int i = 0; i < 7; i++){ // Loop through all 7 cards
			// Remove i-th card from set.
			PokerCard tempI = allCards.remove(i);
			for(int j = 0; j < 6; j++){ // i-th card taken out, only have 6 cards.
				// Remove j-th card from set
				PokerCard tempJ = allCards.remove(j);

				// Add to list of possible hands
				hands.add(new PokerHand(PokerHandStringify(allCards)));

				// Add j-th card back.
				allCards.add(j, tempJ);
			}
			allCards.add(i, tempI);
		}

		// Sort list of possible hands
		Collections.sort(hands, Collections.reverseOrder());

		// return strongest hand
		return hands.get(0);
	}

	public PokerHand getAllCardsFromSixCards(){
		// Set up hands, these will include all possible variations.
		List<PokerHand> hands = new ArrayList<PokerHand>();

		// compile complete list of hands
		List<PokerCard> allCards = Stream.concat(communityCards.stream(), holeCards.stream()).collect(Collectors.toList());

		// Get all variations
		// 6 possible cards, 6 possible variations of cards
		for(int i = 0; i < 6; i++){
			PokerCard temp = allCards.remove(i); // Pull it out
			hands.add(new PokerHand(PokerHandStringify(allCards))); // Add it to list of possible hands
			allCards.add(i, temp); // Add it back.
		}

		// Sort all hands
		Collections.sort(hands, Collections.reverseOrder());
		return hands.get(0); // Highest pair
	}

	public static void readFlops() throws FileNotFoundException{
		String buildUp = "";
		Scanner console = new Scanner(new File("winning_perc_sheet.csv"));
		console.nextLine(); // Remove first line
		while(console.hasNextLine()){
			buildUp += console.nextLine() + "; ";
		}
	}
}
