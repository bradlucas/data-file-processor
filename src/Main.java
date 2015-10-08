import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class Story {
    String ref;
    String date;
    List<String> refs = new ArrayList<String>();
    
    public Story(String line) {
        String[] tokens = line.split(" ");
        ref = tokens[0];
        date = tokens[1];
        int numTokens = tokens.length;
        for (int j = 2; j < numTokens; j++) {
            String ref = tokens[j];
            refs.add(ref);
        }
    }
    
    public Boolean containsRef(String ref) {
        // return True if ref is contained inside of refs list
        return refs.contains(ref);
    }
    
    public static Story getStoryFromRef(String ref, List<Story> stories) {
        for (Story s : stories) {
            if (s.ref.equals(ref)) {
                return s;
            }
        }
        return null;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String ref : refs) {
            sb.append(ref);
            sb.append(" ");
        }
        return String.format("%s %s %s", ref, date, sb.toString());
    }
}

class InputData {
    public List<Float> weights;
    public int numStories;
    public List<Story> stories = new ArrayList<Story>();
    public int numTopStories;
    public String startDate;
    public String endDate;
    
    public static InputData create() throws Exception {
        InputData data = new InputData();
        List<String> lines = data.readStandardInput();
        data.parseWeights(lines.get(0));
        data.parseNumStories(lines.get(1));
        data.parseCommandLine(lines.get(lines.size() - 1));
        data.parseStories(lines);
        return data;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InputData:");
        sb.append("weights: ");
        for (Float i : weights) {
            sb.append(String.format("%.2f ", i));
        }
        sb.append("\n");
        sb.append(String.format("numStories: %d\n", numStories));
        sb.append("Stories");
        if (stories != null) {
            for (Story s : stories) {
                sb.append(s);
            }
        }
        sb.append(String.format("numTopStories: %d\n", numTopStories));
        sb.append(String.format("startDate: %s\n", startDate));
        sb.append(String.format("endDate: %s\n", endDate));
        return sb.toString();
    }
    
    List<String> readStandardInput() throws Exception {
        List<String> lines = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while ((s = in.readLine()) != null && s.length() != 0) {
            lines.add(s);
        }
        return lines;
    }
    
    void parseWeights(String str) {
        List<Float> list = new ArrayList<Float>();
        for (String s : str.split("\\s")) {
            list.add(Float.parseFloat(s));
        }
        weights = list;
    }
    
    void parseNumStories(String str) {
        numStories = Integer.parseInt(str);
    }
    
    void parseCommandLine(String str) {
        String[] tokens = str.split(" ");
        numTopStories = Integer.parseInt(tokens[0]);
        startDate = tokens[1];
        endDate = tokens[2];
    }
    
    void parseStories(List<String> list) {
        // parse story lines get(2), get(len-2)
        int len = list.size();
        int x = len - 1;
        for (int i = 2; i < x; i++) {
            String line = list.get(i);
            stories.add(new Story(line));
        }
    }
    
    List<Story> filteredByDateRange() {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        
        List<Story> rtn = new ArrayList<Story>();
        for (Story s : stories) {
            // if story date in range add to new list
            Date d = parseDate(s.date);
            if (inDateRange(d, start, end)) {
                rtn.add(s);
            }
        }
        return rtn;
    }
    
    Date parseDate(String str) {
        Date d = null;
        try {
            // Dates are in 20140801 format
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            d = (java.util.Date) formatter.parse(str);
        } catch (ParseException pe) {
            System.out.format("Error parsing data %s\n", str);
        }
        return d;
    }
    
    Boolean inDateRange(Date date, Date start, Date end) {
        Boolean rtn = false;
        // if date >= start and date < end
        if (date.equals(start) || date.after(start)) {
            if (date.before(end)) {
                rtn = true;
            }
        }
        return rtn;
    }
}

class ValueComparator implements Comparator<String> {
    Map<String, Float> base;
    
    public ValueComparator(Map<String, Float> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b)) {
            return -1;
        } else {
            if (base.get(a) < base.get(b)) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

class Processor {
    
    public void run(List<Story> stories, List<Float> weights, int numTopStories) {
        // For each story process it's references
        TreeMap<String, Float> results = processReferences(stories, weights);
        // System.out.println("sorted results: "+counts);
        
        // Print only the numTopStories
        printResults(numTopStories, results);
    }
    
    TreeMap<String, Float> processReferences(List<Story> stories, List<Float> weights) {
        Hashtable<String, Float> results = new Hashtable<String, Float>();
        
        for (Story s : stories) {
            float result = 0;
            String ref = s.ref;
            // go through and get the number of first level references
            int firstLevelCount = getFirstLevelCount(ref, stories);
            
            // go through and get the number of second level references
            int secondLevelCount = getSecondLevelCount(ref, stories);
            
            // go through and get the number of third level references
            int thirdLevelCount = getThirdLevelCount(ref, stories);
            
            // calculate the results by applying the weights
            result = (firstLevelCount * weights.get(0)) + (secondLevelCount * weights.get(1))
                + (thirdLevelCount * weights.get(2));
            
            results.put(ref, result);
        }
        TreeMap<String, Float> sorted_counts = sortMapByValues(results);
        return sorted_counts;
    }
    
    TreeMap<String, Float> sortMapByValues(Map<String, Float> map) {
        ValueComparator comp = new ValueComparator(map);
        TreeMap<String, Float> sorted_map = new TreeMap<String, Float>(comp);
        sorted_map.putAll(map);
        return sorted_map;
    }
    
    void printResults(int numStories, TreeMap<String, Float> map) {
        int cnt = 0;
        for (String key : map.keySet()) {
            cnt += 1;
            if (cnt > numStories)
                break;
            // Print to 1 decimal point. Don't print .0
            Float val = map.get(key);
            if (val == val.intValue()) {
                System.out.format("%s %d\n", key, val.intValue());
            } else {
                System.out.format("%s %.1f\n", key, val);
            }
        }
    }
    
    int getFirstLevelCount(String ref, List<Story> stories) {
        // return the number of instances of ref who are in stories refs list
        int cnt = 0;
        for (Story s : stories) {
            if (s.containsRef(ref)) {
                cnt += 1;
            }
        }
        return cnt;
    }
    
    int getSecondLevelCount(String ref, List<Story> stories) {
        // item in list has ref as it's first level reference
        int cnt = 0;
        for (Story s : stories) {
            if (!s.ref.equals(ref)) { // Don't look at ref's story
                for (String nextRef : s.refs) {
                    if (!nextRef.equals(ref)) { // Ignore nextRef which is ref
                        Story nextStory = Story.getStoryFromRef(nextRef, stories);
                        if (nextStory != null && !nextStory.ref.equals(ref)) {
                            if (nextStory.containsRef(ref)) {
                                cnt += 1;
                            }
                        }
                    }
                }
            }
        }
        return cnt;
    }
    
    int getThirdLevelCount(String ref, List<Story> stories) {
        // item in list has a reference which then has ref as it's first level
        // reference
        int cnt = 0;
        for (Story s : stories) {
            if (!s.ref.equals(ref)) { // Don't look at ref's story
                for (String nextRef : s.refs) {
                    if (!nextRef.equals(ref)) { // Ignore nextRef which is ref
                        Story nextStory = Story.getStoryFromRef(nextRef, stories);
                        if (nextStory != null && !nextStory.ref.equals(ref)) {
                            for (String nextNextRef : nextStory.refs) {
                                // Ignore nextNextRef which is ref
                                if (!nextNextRef.equals(ref)) {
                                    Story nextNextStory = Story.getStoryFromRef(nextNextRef, stories);
                                    if (nextNextStory != null && !nextNextStory.ref.equals(ref)) {
                                        if (nextNextStory.containsRef(ref)) {
                                            cnt += 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return cnt;
    }
    
}

public class Main {
    public static void main(String args[]) throws Exception {
        // Read STDIN and parse into an InputData instance
        InputData data = InputData.create();
        
        // Process the InputData
        new Processor().run(data.filteredByDateRange(), data.weights, data.numTopStories);
    }
}
