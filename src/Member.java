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
	private int minDays = 5;
	private int maxDays = 15;

	public Map<Integer, Character> scheduledShifts;

	public Member(int no, String name, String[] languages) {
		this.no = no;
		this.name = name;

		chineseStaff = false;
		for (String s : languages) {
			if (s.equals("chinese"))
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

	public String getAvailability(int date) {
		return availability[date];
	}

	public boolean schedule(int date, char shift) {
		// System.out.println("******Scheduling date 11: " + no + " shift-" + shift);

		// ALL-DAY shift or four-continuous-shifts not allowed!!
		if (scheduledShifts.containsKey(date) || !checkContinuity(date))
			return false;

		scheduledShifts.put(date, shift);
		scheduledDays++;
		return true;
	}

	private boolean checkContinuity(int date) {
		int count = 0;
		for (int i = date >= 3 ? date - 3 : 0; i < date; i++) {
			count += scheduledShifts.containsKey(i) ? 1 : 0;
		}
		if (count == 3)
			return false;

		for (int i = date + 1; i < date + 4; i++) {
			count -= scheduledShifts.containsKey(i - 4) ? 1 : 0;
			count += scheduledShifts.containsKey(i) ? 1 : 0;
			if (count == 3)
				return false;
		}

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
		sb.append(scheduledDays + ",");
		sb.append(requestedDays + ",");

		double ratio = requestedDays == 0 ? 0 : (double) scheduledDays / requestedDays * 100;
		sb.append(String.format("%.2f\n", ratio));

		return sb.toString();
	}

	public void reset() {
		requestedDays = 0;
		scheduledDays = 0;
		availability = null;
		scheduledShifts = new HashMap<>();
	}

	public int compareTo(Member other) {
		if (this.scheduledDays > maxDays)
			return 3;
		if (this.requestedDays >= minDays && other.requestedDays < minDays)
			return 2;
		else if (this.requestedDays < minDays && other.requestedDays >= minDays)
			return -2; // higher priority

		if (this.scheduledDays > 8 && other.scheduledDays > 8)
			return this.scheduledDays / this.requestedDays - other.scheduledDays / other.requestedDays;

		return this.scheduledDays - other.scheduledDays;
	}

//	private Language valueOf(String value) {
//		for (Language language : Language.values()) {
//			if (language == Language.valueOf(value))
//				return language;
//		}
//		return null;
//	}
}
