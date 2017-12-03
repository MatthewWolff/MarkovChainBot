import java.util.*;
import java.io.*;

public class Chatbot{
	private static String filename = "./WARC201709_wid.txt";
	private static ArrayList<Integer> corpus;
	private static Integer[] slick_corpus;

	private static ArrayList<Integer> readCorpus(){
		ArrayList<Integer> corpus = new ArrayList<Integer>();
		try{
			File f = new File(filename);
			Scanner sc = new Scanner(f);
			while(sc.hasNext()){
				if(sc.hasNextInt()){
					int i = sc.nextInt();
					corpus.add(i);
				}
				else{
					sc.next();
				}
			}
			sc.close();
		}
		catch(FileNotFoundException ex){
			System.out.println("File Not Found.");
		}
		return corpus;
	}
	static public void main(String[] args){
		corpus = readCorpus();
		int flag = Integer.valueOf(args[0]);

		if(flag == 100) {
			int w = Integer.valueOf(args[1]);
			int count = 0;
			for(int word : corpus) if(word == w) count++;
			System.out.println(count);
			System.out.println(String.format("%.7f",count/(double)corpus.size()));
		}
		else if(flag == 200){
			int n1 = Integer.valueOf(args[1]);
			int n2 = Integer.valueOf(args[2]);
			double rand = n1/(double) n2;

			Double[] prob_distrib = new Double[4700];
			Arrays.fill(prob_distrib, 0.0);
			for(int i : corpus) 
				prob_distrib[i] += 1/(double)corpus.size();

			double sum = 0; int i;
			for(i = 0; i < prob_distrib.length && sum <= rand; i++)
				sum += prob_distrib[i];
			// print (plus 0 special case)
			if(n1 == 0) System.out.format("%d\n%.7f\n%.7f\n", --i /*index*/, 0.0 /*lower*/, sum /*upper*/);
			else System.out.format("%d\n%.7f\n%.7f\n", --i, sum - prob_distrib[i], sum);

		}
		else if(flag == 300){
			int h = Integer.valueOf(args[1]);
			int w = Integer.valueOf(args[2]);
			int count = 0, words_after_h = 0;
			ListIterator<Integer> it = corpus.listIterator();
			while(it.hasNext()) // might need to check that there IS an element after h
				if(it.next() == h && ++words_after_h > 0 && corpus.get(it.nextIndex()) == w)
					count++;
			//output 
			System.out.println(count);
			System.out.println(words_after_h);
			System.out.println(String.format("%.7f",count/(double)words_after_h));
		}
		else if(flag == 400){
			int n1 = Integer.valueOf(args[1]);
			int n2 = Integer.valueOf(args[2]);
			int h = Integer.valueOf(args[3]);
			double rand = n1/(double) n2;
			Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>(4700);
			ListIterator<Integer> it = corpus.listIterator();
			while(it.hasNext()) {
				int i = it.nextIndex(), word = it.next(); // better than retrieving word from index each time
				if(map.containsKey(word))
					map.get(word).add(i); // create list of all occurrences of each word
				else
					map.put((Integer) word, new ArrayList<Integer>(Arrays.asList((Integer) i)));
			}

			slick_corpus = corpus.toArray(new Integer[corpus.size()]); // constant time access
			// count up
			Double[] prob_distrib = new Double[4700]; Arrays.fill(prob_distrib, 0.0);
			List<Integer> loci = map.containsKey(h) ? map.get(h) : null;
			if(loci != null) 
				for(int index : loci) // list of all h's
					if(index < corpus.size()) // if the word after is w, increment
						prob_distrib[slick_corpus[index+1]] += 1/(double)loci.size();

			// calculate
			double sum = 0; int i;
			for(i = 0; i < prob_distrib.length && sum <= rand; i++)
				sum += prob_distrib[i];
			// print (plus 0 special case)
			if(n1 == 0) System.out.format("%d\n%.7f\n%.7f\n", --i /*index*/, 0.0 /*lower*/, sum /*upper*/);
			else System.out.format("%d\n%.7f\n%.7f\n", --i /*index*/, sum - prob_distrib[i] /*lower*/, sum /*upper*/);
		}
		else if(flag == 500){
			int h1 = Integer.valueOf(args[1]);
			int h2 = Integer.valueOf(args[2]);
			int w = Integer.valueOf(args[3]);
			int count = 0, words_after_h1h2 = 0;
			ListIterator<Integer> it = corpus.listIterator();
			while(it.hasNext() && it.nextIndex() < corpus.size() - 1) 
				if(it.next() == h1 && corpus.get(it.nextIndex()) == h2 && ++words_after_h1h2 > 0 // lowkey increment
				&& corpus.get(it.nextIndex()+1) == w)
					count++;
			//output 
			System.out.println(count);
			System.out.println(words_after_h1h2);
			if(words_after_h1h2 == 0)
				System.out.println("undefined");
			else
				System.out.println(String.format("%.7f",count/(double) words_after_h1h2));
		}
		else if(flag == 600){
			int n1 = Integer.valueOf(args[1]);
			int n2 = Integer.valueOf(args[2]);
			int h1 = Integer.valueOf(args[3]);
			int h2 = Integer.valueOf(args[4]);
			double rand = n1/(double) n2;
			//Generate map of all occurrences
			Map<String, List<Integer>> map = new HashMap<String, List<Integer>>(4700);
			slick_corpus = corpus.toArray(new Integer[corpus.size()]); // constant time access
			ListIterator<Integer> it = corpus.listIterator();
			while(it.hasNext() && it.nextIndex() < corpus.size()-1)
			{
				int word1 = it.next(), word2 = slick_corpus[it.nextIndex()];
				if(map.containsKey(word1 + " " + word2)) // new key format
					map.get(word1 + " " + word2).add(it.nextIndex()); // create list of all occurrences of each word pair
				else
					map.put(word1 + " " + word2, new ArrayList<Integer>(Arrays.asList((Integer) it.nextIndex())));
			}

			// count up
			Double[] prob_distrib = new Double[4700]; Arrays.fill(prob_distrib, 0.0);
			List<Integer> loci = map.containsKey(h1 + " " + h2) ? map.get(h1 + " " + h2) : null;
			if(loci != null) 
				for(int index : loci) // list of all h's
					if(index < corpus.size()) // if the word after is w, increment
						prob_distrib[slick_corpus[index+1]]++;

			// calculate
			try{
				prob_distrib = Arrays.stream(prob_distrib).map(f -> f/loci.size()).toArray(Double[]::new);
			} catch(NullPointerException e){
				System.out.println("undefined");
				return;
			}
			double sum = 0; int i;
			for(i = 0; i < prob_distrib.length && sum <= rand; i++) // this one fucks up... java Chatbot 600 33 100 2591 2473
				sum += prob_distrib[i];
			// print (plus 0 special case)
			if(n1 == 0)System.out.format("%d\n%.7f\n%.7f\n", --i /*index*/, 0.0 /*lower*/, sum /*upper*/);
			else System.out.format("%d\n%.7f\n%.7f\n", --i /*index*/, sum - prob_distrib[i] /*lower*/, sum /*upper*/);
		}
		else if(flag == 700){
			long startTime = System.nanoTime();
			int seed = Integer.valueOf(args[1]);
			int t = Integer.valueOf(args[2]);
			int h1=0, h2=0;
			slick_corpus = corpus.toArray(new Integer[corpus.size()]); // constant time access
			// compute these once instead of every time
			Double[] prob_distrib_one = survey_one_word();
			Map<Integer, List<Integer>> prob_distrib_two = survey_two_word();
			Map<String, List<Integer>> prob_distrib_three = survey_three_word();

			Random rng = new Random();
			if (seed != -1) rng.setSeed(seed);

			if(t == 0){
				double r = rng.nextDouble();
				h1 = one_word(prob_distrib_one, r);
				System.out.println(h1);
				if(h1 == 9 || h1 == 10 || h1 == 12){
					return;
				}
				r = rng.nextDouble();
				h2 = two_word(prob_distrib_two, r, h1);
				System.out.println(h2);
			}
			else if(t == 1){
				h1 = Integer.valueOf(args[3]);
				double r = rng.nextDouble();
				h2 = two_word(prob_distrib_two, r, h1);
				System.out.println(h2);
			}
			else if(t == 2){
				h1 = Integer.valueOf(args[3]);
				h2 = Integer.valueOf(args[4]);
			}

			while(h2 != 9 && h2 != 10 && h2 != 12){
				double r = rng.nextDouble();
				int w  = 0;
				w = three_word(prob_distrib_three, r, h1, h2);
				System.out.println(w);
				h1 = h2;
				h2 = w;
			}
			long endTime = System.nanoTime();
			System.out.println((endTime - startTime)/1000000.0/1000.0 /*seconds*/);
		}
		return;
	}
	private static Double[] survey_one_word() {
		Double[] my_ret = new Double[4700];
		Arrays.fill(my_ret, 0.0);
		for(int i : corpus) 
			my_ret[i] += 1/(double)corpus.size();
		return my_ret;
	}
	private static int one_word(Double[] prob_distrib, double rand) {
		double sum = 0; int i;
		for(i = 0; i < prob_distrib.length && sum <= rand; i++)
			sum += prob_distrib[i];
		return --i;
	}
	private static Map<Integer, List<Integer>> survey_two_word(){
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>(4700);
		ListIterator<Integer> it = corpus.listIterator();
		while(it.hasNext()) {
			int i = it.nextIndex(), word = it.next(); // better than retrieving word from index each time
			if(map.containsKey(word))
				map.get(word).add(i); // create list of all occurrences of each word
			else
				map.put((Integer) word, new ArrayList<Integer>(Arrays.asList((Integer) i)));
		}
		return map;
	}
	private static int two_word(Map<Integer, List<Integer>> map, double rand, int h){
		// count up
		Double[] prob_distrib = new Double[4700]; Arrays.fill(prob_distrib, 0.0);
		List<Integer> loci = map.containsKey(h) ? map.get(h) : null;
		if(loci != null) 
			for(int index : loci) // list of all h's
				if(index < corpus.size()) // if the word after is w, increment
					prob_distrib[slick_corpus[index+1]] += 1/(double)loci.size();

		// calculate
		double sum = 0; int i;
		for(i = 0; i < prob_distrib.length && sum <= rand; i++)
			sum += prob_distrib[i];
		return --i;
	}
	private static Map<String, List<Integer>> survey_three_word(){
		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>(4700);
		ListIterator<Integer> it = corpus.listIterator();
		while(it.hasNext() && it.nextIndex() < corpus.size()-1)
		{
			int word1 = it.next(), word2 = slick_corpus[it.nextIndex()];
			if(map.containsKey(word1 + " " + word2)) // new key format
				map.get(word1 + " " + word2).add(it.nextIndex()); // create list of all occurrences of each word pair
			else
				map.put(word1 + " " + word2, new ArrayList<Integer>(Arrays.asList((Integer) it.nextIndex())));
		}
		return map;
	}
	private static int three_word(Map<String, List<Integer>> map, double rand, int h1, int h2){
		// count up
		Double[] prob_distrib = new Double[4700]; Arrays.fill(prob_distrib, 0.0);
		List<Integer> loci = map.containsKey(h1 + " " + h2) ? map.get(h1 + " " + h2) : null;
		if(loci != null) 
			for(int index : loci) // list of all h's
				if(index < corpus.size()) // if the word after is w, increment
					prob_distrib[slick_corpus[index+1]] += 1/(double)loci.size();

		// calculate
		double sum = 0; int i;
		for(i = 0; i < prob_distrib.length && sum <= rand; i++)
			sum += prob_distrib[i];
		return --i;
	}
}
