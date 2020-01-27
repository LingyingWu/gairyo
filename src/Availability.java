import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Availability implements Comparable<Availability> {
// Availability of a certain date.	
	final int date;
	int num_morning, num_afternoon;
	int morning_ch, afternoon_ch;

	Map<Character, List<Integer>> candidates;

	public Availability(int date) {
		this.date = date;

		num_morning = 0;
		num_afternoon = 0;
		morning_ch = 0;
		afternoon_ch = 0;

		candidates = new HashMap<>();
		candidates.put('A', new ArrayList<Integer>());
		candidates.put('B', new ArrayList<Integer>());
		candidates.put('C', new ArrayList<Integer>());
		candidates.put('D', new ArrayList<Integer>());
		candidates.put('H', new ArrayList<Integer>());
	}

	public int getMorningCandidates() {
		List<Integer> result = new ArrayList<>(candidates.get('A'));
		List<Integer> two = new ArrayList<>(candidates.get('B'));
		two.removeAll(result);
		result.addAll(two);

		two = new ArrayList<>(candidates.get('H'));
		two.removeAll(result);
		result.addAll(two);

		return result.size();
	}

	public int getAfternoonCandidates() {
		List<Integer> result = new ArrayList<>(candidates.get('C'));
		List<Integer> two = new ArrayList<>(candidates.get('D'));
		two.removeAll(result);
		result.addAll(two);

		return result.size();
	}

	public void input(char shift, int employeeNo, boolean chinese) {
		List<Integer> list = candidates.get(shift);
		list.add(employeeNo);
		candidates.put(shift, list);
	}

	public void inputPeople(boolean asaban, boolean chinese) {
		num_morning += asaban ? 1 : 0;
		num_afternoon += !asaban ? 1 : 0;

		morning_ch += (asaban && chinese) ? 1 : 0;
		afternoon_ch += (!asaban && chinese) ? 1 : 0;
	}

	public int compareTo(Availability other) {
		// Higher priority to dates with less availabilities.
		return this.num_morning + this.num_afternoon - other.num_morning - other.num_afternoon;
	}
}