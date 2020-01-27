import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
	static List<Integer> outputOrder = new ArrayList<>();

	public static void outputResult(String filename) {
		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			writer.write(headline.substring(17) + "\n");
			for (int no : outputOrder) {
				Member member = employees.get(no);
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
				outputOrder.add(no);
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
		int vac_b = vacancy.get("B");
		int vac_h = vacancy.get("H");

		// 希望者不足
		if (cand_morning <= vac_morning) {
			if (cand_h.size() <= vacancy.get("H")) {
				System.out.println("h <= 4");
				for (int no : cand_h) {
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
					vac_h -= done ? 1 : 0;
				}
				while (vac_morning > 0 && cand_b.size() > 0) {
					int no = getRandomNum(cand_b);
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
					vac_b -= done ? 1 : 0;
				}
			} else if (cand_b.size() < vacancy.get("B")) {
				System.out.println("b <= 4");
				for (int no : cand_b) {
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
					vac_b -= done ? 1 : 0;
				}
				while (vac_morning > 0 && cand_h.size() > 0) {
					int no = getRandomNum(cand_h);
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
					vac_h -= done ? 1 : 0;
				}
			}
		}

		if (!cand_a.isEmpty()) {
			int no = getRandomNum(cand_a);
			boolean done = employees.get(no).schedule(date, 'A');
			if (done) {
				vac_morning--;
				cand_morning--;
				cand_b.remove(Integer.valueOf(no));
				cand_h.remove(Integer.valueOf(no));
			}
		}

		// Chinese Staff (2 ppl)
		List<Integer> chinese_staff = new ArrayList<>();
		for (int no : cand_b) {
			if (employees.get(no).chineseStaff)
				chinese_staff.add(no);
		}
		for (int no : cand_h) {
			if (employees.get(no).chineseStaff && !chinese_staff.contains(Integer.valueOf(no)))
				chinese_staff.add(no);
		}
		for (int i = 0; i < vacancy.get("chinese") && chinese_staff.size() > 0;) {
			int no = getRandomNum(chinese_staff);
			cand_b.remove(Integer.valueOf(no));
			cand_h.remove(Integer.valueOf(no));

			String availability = employees.get(no).getAvailability(date);
			if (availability.indexOf('B') < 0 && availability.indexOf('H') < 0)
				continue;

			if (availability.length() >= 2) {
				if (vac_h > vac_b) {
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
					vac_h -= done ? 1 : 0;
					i += done ? 1 : 0;
				} else {
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
					vac_b -= done ? 1 : 0;
					i += done ? 1 : 0;
				}

			} else if (availability.length() == 1) {
				if (availability.charAt(0) == 'H') {
					boolean done = employees.get(no).schedule(date, 'H');
					vac_morning -= done ? 1 : 0;
					vac_h -= done ? 1 : 0;
					i += done ? 1 : 0;
				} else {
					boolean done = employees.get(no).schedule(date, 'B');
					vac_morning -= done ? 1 : 0;
					vac_b -= done ? 1 : 0;
					i += done ? 1 : 0;
				}
			} else
				throw new IllegalArgumentException("Error occurs when arranging chinese staff. " + date);
		}

		PriorityQueue<Member> pqb = new PriorityQueue<>();
		PriorityQueue<Member> pqh = new PriorityQueue<>();
		for (int no : cand_h) pqh.add(employees.get(no));
		for (int no : cand_b) pqb.add(employees.get(no));

		while (vac_morning > 0 && !pqb.isEmpty() && !pqh.isEmpty()) {
			if (vac_b > 0) {
				boolean done = false;
				while (!pqb.isEmpty() && !done) {
					Member member = pqb.remove();
					pqh.remove(member);
					done = member.schedule(date, 'B');
					if (done) {
						vac_b--;
						vac_morning--;
					}
				}
			}
			if (vac_h > 0) {
				boolean done = false;
				while (!pqh.isEmpty() && !done) {
					Member member = pqh.remove();
					pqb.remove(member);
					done = member.schedule(date, 'H');
					if (done) {
						vac_h--;
						vac_morning--;
					}
				}
			}
			// 缺少 A
			if (vac_b + vac_h == 0 && vac_morning == 1) {
				boolean done = false;
				while (!pqh.isEmpty() && !done) {
					Member member = pqh.remove();
					pqb.remove(member);
					done = member.schedule(date, 'H');
					if (done) {
						vac_h--;
						vac_morning--;
					}
				}
				while (!pqb.isEmpty() && !done) {
					Member member = pqb.remove();
					pqh.remove(member);
					done = member.schedule(date, 'B');
					if (done) {
						vac_b--;
						vac_morning--;
					}
				}
			}
		}
		pqh.clear();
		pqb.clear();

		// Afternoon
		List<Integer> cand_c = timetable.getAvailabilityOf(date, 'C');
		List<Integer> cand_d = timetable.getAvailabilityOf(date, 'D');
		int vac_c = vacancy.get("C");
		int vac_d = vacancy.get("D");

		if (cand_afternoon <= vac_afternoon) {
			// 希望人数 < 定員
			if (cand_c.size() <= vacancy.get("C")) {
				System.out.println("c <= 2");
				for (int no : cand_c) {
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
					vac_c -= done ? 1 : 0;
				}
				while (vac_afternoon > 0 && cand_d.size() > 0) {
					int no = getRandomNum(cand_d);
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
					vac_d -= done ? 1 : 0;
				}
			} else if (cand_d.size() <= vacancy.get("D")) {
				System.out.println("d <= 4");
				for (int no : cand_d) {
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
					vac_d -= done ? 1 : 0;
				}
				while (vac_afternoon > 0 && cand_c.size() > 0) {
					int no = getRandomNum(cand_c);
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
					vac_c -= done ? 1 : 0;
				}
			}
		}

		// Chinese Staff (2 ppl)
		chinese_staff.clear();
		for (int no : cand_c) {
			if (employees.get(no).chineseStaff)
				chinese_staff.add(no);
		}
		for (int no : cand_d) {
			if (employees.get(no).chineseStaff && !chinese_staff.contains(Integer.valueOf(no)))
				chinese_staff.add(no);
		}
		for (int i = 0; i < vacancy.get("chinese") && chinese_staff.size() > 0;) {
			int no = getRandomNum(chinese_staff);
			cand_c.remove(Integer.valueOf(no));
			cand_d.remove(Integer.valueOf(no));

			String availability = employees.get(no).getAvailability(date);
			if (availability.indexOf('C') < 0 && availability.indexOf('D') < 0)
				continue;

			if (availability.length() >= 2) {
				if (vac_c > vac_d) {
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
					vac_c -= done ? 1 : 0;
					i += done ? 1 : 0;
				} else {
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
					vac_d -= done ? 1 : 0;
					i += done ? 1 : 0;
				}

			} else if (availability.length() == 1) {
				if (availability.charAt(0) == 'C') {
					boolean done = employees.get(no).schedule(date, 'C');
					vac_afternoon -= done ? 1 : 0;
					vac_c -= done ? 1 : 0;
					i += done ? 1 : 0;
				} else {
					boolean done = employees.get(no).schedule(date, 'D');
					vac_afternoon -= done ? 1 : 0;
					vac_d -= done ? 1 : 0;
					i += done ? 1 : 0;
				}
			} else
				throw new IllegalArgumentException("Error occurs when arranging chinese staff. " + date);
		}

		PriorityQueue<Member> pqc = new PriorityQueue<>();
		PriorityQueue<Member> pqd = new PriorityQueue<>();
		for (int no : cand_c) pqc.add(employees.get(no));
		for (int no : cand_d) pqd.add(employees.get(no));

		while (vac_afternoon > 0 && !pqc.isEmpty() && !pqd.isEmpty()) {
			if (vac_d > 0) {
				boolean done = false;
				while (!pqd.isEmpty() && !done) {
					Member member = pqd.remove();
					pqc.remove(member);
					done = member.schedule(date, 'D');
					if (done) {
						vac_d--;
						vac_afternoon--;
					}
				}
			}
			if (vac_c > 0) {
				boolean done = false;
				while (!pqc.isEmpty() && !done) {
					Member member = pqc.remove();
					pqd.remove(member);
					done = member.schedule(date, 'C');
					if (done) {
						vac_c--;
						vac_afternoon--;
					}
				}
			}
		}
		pqc.clear();
		pqd.clear();
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
		vacancy.put("chinese", 2);

		PriorityQueue<Availability> pq = new PriorityQueue<>();
		for (int i = 0; i < days; i++)
			pq.add(timetable.getAvailability(i));

		while (!pq.isEmpty())
			execute(pq.remove().date);

		outputResult("勤務表.csv");
		System.out.println("DONE");
	}
}
