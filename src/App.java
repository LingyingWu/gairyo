import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class App {

	static Map<Integer, Member> employees = new HashMap<>();
	static Map<String, Integer> vacancy; // 定員 shift : number

	static Timetable timetable;
	static String headline;
	static int days = 31;

	public static void outputResult(String filename) {
		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			writer.write(headline.substring(17) + "\n");
			for (Member member : employees.values()) {
				writer.write(member.output(days));
				System.out.print(member.output(days));
			}

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void inputShiftRequest() {

		for (Member member : employees.values())
			member.reset();

		String csvFile = "shift_requestcopy.csv";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(csvFile));
			String line = br.readLine();
			headline = line;
			days = headline.split(",").length - 3;
			timetable = new Timetable(days);

			while ((line = br.readLine()) != null) {
				String[] availability = new String[days];
				String[] temp = line.split(",");
				String[] info = Arrays.copyOfRange(temp, 2, temp.length);

				int no = 0;
				String name = "";
				for (char c : info[0].toCharArray()) {
					if (c >= '0' && c <= '9')
						no = no * 10 + (c - '0');
					else
						name += c;
				}
				name = name.trim();
//				int no = Integer.parseInt(info[0].substring(0, 2).replaceAll("\\uFEFF", ""));
//				String name = info[0].substring(2, info[0].length()).trim();
				if (!employees.containsKey(no))
					throw new IllegalArgumentException("まだ登録されてないメンバーです: no." + no);

				int count = 0;
				for (int i = 1; i < info.length; i++) {
					String request = info[i];
					availability[i - 1] = request;
					if (!request.equals("-"))
						count++;
					if (request.equals("O"))
						request = "ABHCD";
					timetable.input(i - 1, request, no, employees.get(no).chineseStaff);
				}

				employees.get(no).setAvailability(availability, count);
				count = 0;

				System.out.println(no + "--" + name);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void inputMemberInfo() {
		String csvFile = "member.csv";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(csvFile));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] info = line.split(",");
				int no = Integer.parseInt(info[0].replaceAll("\\uFEFF", ""));
				Member member = new Member(no, info[1], Arrays.copyOfRange(info, 2, info.length));
				employees.put(no, member);
			}
//			for (Member member : employees.values()) {
//				if (member.chineseStaff)
//					chinese_staff.add(member.no);
//			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static int getRandomNum(List<Integer> list) {

		if (list.size() == 0)
			throw new IllegalArgumentException("Candidate list EMPTY");

		int n = list.size();
		int index = (int) (Math.random() * n);
		int num = list.get(index);

		list.set(index, list.get(n - 1));
		list.remove(n - 1);

		// Return the removed number
		return num;
	}

	public static void execute(int date) {

		System.out.format("Scheduling date : %d \t[午前] %d (%d + %d + %d) \t[午後] %d (%d + %d)\n",
				date, timetable.getAvailability(date).num_morning, timetable.getAvailabilityOf(date, 'A').size(),
				timetable.getAvailabilityOf(date, 'H').size(), timetable.getAvailabilityOf(date, 'B').size(),
				timetable.getAvailability(date).num_afternoon, timetable.getAvailabilityOf(date, 'C').size(),
				timetable.getAvailabilityOf(date, 'D').size());

		int cand_morning = timetable.getAvailability(date).num_morning;
		int cand_afternoon = timetable.getAvailability(date).num_afternoon;
		int vac_morning = vacancy.get("morning");
		int vac_afternoon = vacancy.get("afternoon");

		// Morning
		List<Integer> cand_a = timetable.getAvailabilityOf(date, 'A');
		List<Integer> cand_b = timetable.getAvailabilityOf(date, 'B');
		List<Integer> cand_h = timetable.getAvailabilityOf(date, 'H');

		// 希望者不足
		if (cand_morning <= vac_morning) {
			if (cand_h.size() <= vacancy.get("H")) {
				System.out.println("h < 3");
				for (int no : cand_h) {
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
				}
				while (vac_morning > 0 && cand_b.size() > 0) {
					int no = getRandomNum(cand_b);
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
				}
			} else if (cand_b.size() < vacancy.get("B")) {
				System.out.println("b < 6");
				for (int no : cand_b) {
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
				}
				while (vac_morning > 0 && cand_h.size() > 0) {
					int no = getRandomNum(cand_h);
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
				}
			}
		} else {
			if (!cand_a.isEmpty()) {
				int no = getRandomNum(cand_a);
				boolean done = employees.get(no).schedule(date, 'A');
				if (done) {
					vac_morning--; // System.out.println(" A-SCHEDULED!! to no." + no);
					cand_morning--;
					cand_b.remove(Integer.valueOf(no));
					cand_h.remove(Integer.valueOf(no));
				}
			}

			PriorityQueue<Member> pqb = new PriorityQueue<>();
			PriorityQueue<Member> pqh = new PriorityQueue<>();

			for (int no : cand_h)
				pqh.add(employees.get(no));
			for (int no : cand_b)
				pqb.add(employees.get(no));

			int vac_b = vacancy.get("B");
			int vac_h = vacancy.get("H");
			while (vac_morning > 0) {
				if (vac_b > 0) {
					boolean done = false;
					while (!pqb.isEmpty() && !done) {
						Member member = pqb.remove();
						done = member.schedule(date, 'B');
						if (done) {
							vac_b--;
							vac_morning--;
							pqh.remove(member);
						}
					}
				}
				if (vac_h > 0) {
					boolean done = false;
					while (!pqh.isEmpty() && !done) {
						Member member = pqh.remove();
						done = member.schedule(date, 'H');
						if (done) {
							vac_h--;
							vac_morning--;
							pqb.remove(member);
						}
					}
				}
			}
			pqh.clear();
			pqb.clear();
		}

		// Afternoon
		List<Integer> cand_c = timetable.getAvailabilityOf(date, 'C');
		List<Integer> cand_d = timetable.getAvailabilityOf(date, 'D');

		if (cand_afternoon <= vac_afternoon) {
			// 希望人数 < 定員
			if (cand_c.size() <= vacancy.get("C")) {
				System.out.println("c <= 2");
				for (int no : cand_c) {
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
				}
				while (vac_afternoon > 0 && cand_d.size() > 0) {
					int no = getRandomNum(cand_d);
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
				}
			} else if (cand_d.size() <= vacancy.get("D")) {
				System.out.println("d <= 4");
				for (int no : cand_d) {
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
				}
				while (vac_afternoon > 0 && cand_c.size() > 0) {
					int no = getRandomNum(cand_c);
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
				}
			}
		} else {
			PriorityQueue<Member> pq = new PriorityQueue<>();

			for (int no : cand_d)
				pq.add(employees.get(no));
			int vac_d = vacancy.get("D");
			while (vac_afternoon > 0 && vac_d > 0 && !pq.isEmpty()) {
				Member member = pq.remove();
				boolean done = member.schedule(date, 'D');
				if (done) {
					vac_d--;
					vac_afternoon--;
					cand_d.remove(Integer.valueOf(member.no));
				}
			}
			pq.clear();

			for (int no : cand_c)
				pq.add(employees.get(no));
			int vac_c = vacancy.get("C");
			while (vac_afternoon > 0 && vac_c > 0 && !pq.isEmpty()) {
				Member member = pq.remove();
				boolean done = member.schedule(date, 'C');
				if (done) {
					vac_c--;
					vac_afternoon--;
				}
			}
			pq.clear();
		}
	}

	public static void main(String[] args) {

		System.out.println("member.csv から名簿を入力します。");
		inputMemberInfo();

		System.out.println("shift_request.csv からシフト希望を入力します。");
		inputShiftRequest();

		vacancy = new HashMap<>();
		// (min) A:1, H:2, B:5, C:2, D:4
		vacancy.put("A", 1);
		vacancy.put("B", 4);
		vacancy.put("H", 4);
		vacancy.put("C", 2);
		vacancy.put("D", 4);
		vacancy.put("morning", 9); // 1+4+4 or 1+5+3
		vacancy.put("afternoon", 6); // 2+4 or 3+3

		PriorityQueue<Availability> pq = new PriorityQueue<>();
		for (int i = 0; i < days; i++)
			pq.add(timetable.getAvailability(i));

		while (!pq.isEmpty())
			execute(pq.remove().date);

		outputResult("勤務表.csv");
		System.out.println("DONE");
	}
}
