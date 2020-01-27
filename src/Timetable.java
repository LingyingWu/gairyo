import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Timetable {
	List<Map<Character, List<Integer>>> result;

	Map<Integer, Availability> availabilities;

	final String morningShift = "ABH";
	final String afternoonShift = "CD";

	public Timetable(int days) {
		availabilities = new HashMap<>();
		for (int i = 0; i < days; i++)
			availabilities.put(i, new Availability(i));
	}

	public List<Integer> getAvailabilityOf(int date, char shift) {
		return getAvailability(date).candidates.get(shift);
	}

	public int getAvailabilityMorning(int date) {
		return getAvailability(date).getMorningCandidates();
	}

	public int getAvailabilityAfternoon(int date) {
		return getAvailability(date).getAfternoonCandidates();
	}

	public Availability getAvailability(int date) {
		if (!availabilities.containsKey(date))
			throw new IllegalArgumentException("Null availability: " + date);
		return availabilities.get(date);
	}

	public int getRandomFrom(List<Integer> list) {
		int rnd = new Random().nextInt(list.size());
		return list.get(rnd);
	}

	public void input(int date, String shifts, int employeeNo, boolean chinese) {
		Availability cand = availabilities.get(date);
		for (char shift : shifts.toCharArray()) {
			if (shift == '-')
				continue;
			cand.input(shift, employeeNo, chinese);
		}
		if (shifts.contains("A") || shifts.contains("B") || shifts.contains("H"))
			cand.inputPeople(true, chinese);
		if (shifts.contains("C") || shifts.contains("D"))
			cand.inputPeople(false, chinese);
	}
}
