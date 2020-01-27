import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Member implements Comparable<Member> {

	final int no;
	final String name;
	boolean chineseStaff;

	String[] availability;
	private int requestedDays;
	private int scheduledDays;

	public Map<Integer, Character> scheduledShifts;

	public Member(int no, String name, String[] languages) {
		this.no = no;
		this.name = name;

		chineseStaff = false;
		for (String s : languages) {
			if (s == "chinese")
				chineseStaff = true;
		}
		reset();
	}

	public void setAvailability(String[] a, int days) {
		if (this.availability != null)
			System.out.println("No." + no + " " + name + "'s availability is not empty.");
		this.availability = a;
		requestedDays = days;
	}

	public boolean schedule(int date, char shift) {
		// ALL-DAY shift not allowed!!
		if (scheduledShifts.containsKey(date))
			return false;

		scheduledShifts.put(date, shift);
		scheduledDays++;
		return true;
	}

	public String output(int days) {
		List<Integer> sortedKeys = new ArrayList<>(scheduledShifts.keySet());
		Collections.sort(sortedKeys);

		StringBuilder sb = new StringBuilder();
		// sb.append(", ," + name + ",");
		sb.append(name + ",");
		for (int i = 0; i < days; i++) {
			sb.append(scheduledShifts.getOrDefault(i, ' ') + ",");
		}
		sb.append(requestedDays + ",");
		sb.append("\n");

		return sb.toString();
	}

	public void reset() {
		requestedDays = 0;
		scheduledDays = 0;
		availability = null;
		scheduledShifts = new HashMap<>();
	}

	public int compareTo(Member other) {
		if (this.requestedDays < 6 && other.requestedDays >= 6)
			return -2; // higher priority
		else if (this.requestedDays >= 6 && other.requestedDays <= 6)
			return 2;
		return this.scheduledDays / this.requestedDays - other.scheduledDays - other.requestedDays;
	}

//	private Language valueOf(String value) {
//		for (Language language : Language.values()) {
//			if (language == Language.valueOf(value))
//				return language;
//		}
//		return null;
//	}
}
