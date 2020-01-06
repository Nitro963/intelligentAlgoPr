import java.util.*;

public class Table implements Cloneable {
    private ArrayList<Exam> list;//Exams Table
    private Exam currentExam;
    private HashSet<Watcher>[][] hashSet;//at Day i and time j is the watcher x Taken?
    private HashSet<ClassRoom>[][] classRoomHashSet;//at Day i and time j is the classroom x taken?
    private LinkedList<Subject> pendingSubjects;//pending subjects to add to the exam list
    //pending watchers to take a watch(fair distribution)
    private LinkedList<Teacher> pendingTeachers;
    private LinkedList<TeacherAssistant> pendingTeachersAssistant;
    private LinkedList<Employee> pendingEmployees;
    private LinkedList<MasterStudent> pendingStudents;
    private int[] watchesCount;
    private int g;//Traversing cost(Graph edge)

    public Exam getCurrentExam() {
        return currentExam;
    }

    public Table() {
        list = new ArrayList<>();
        hashSet = new HashSet[10][5];
        classRoomHashSet = new HashSet[10][5];
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 5; j++) {
                hashSet[i][j] = new HashSet<>();
                classRoomHashSet[i][j] = new HashSet<>();
            }
        watchesCount = new int[25];
        pendingSubjects = new LinkedList<>();
        pendingTeachers = new LinkedList<>();
        pendingTeachersAssistant = new LinkedList<>();
        pendingStudents = new LinkedList<>();
        pendingEmployees = new LinkedList<>();
    }

    private int h1() {
        return pendingSubjects.size();
    }

    private int h2() {
        return 0;
    }

    private void addW(Watcher w) {
        currentExam.addWatcher(w);
        watchesCount[w.getId()]++;
        hashSet[currentExam.getSubject().getDay()][currentExam.getSubject().getTime()].add(w);
    }

    private boolean checkConDay(Watcher w) {
        if (w.getConstrain().getConDay() == -1)
            return true;
        int cnt = 0;
        for (int j = 1; j <= 3; j++)
            if (hashSet[currentExam.getSubject().getDay()][j].contains(w))
                cnt++;
            else
                cnt = 0;
        return cnt <= w.getConstrain().getConDay();
    }

    private ArrayList<Table> selectClassRoom(ArrayList<ClassRoom> classRooms) {
        ArrayList<Table> ret = new ArrayList<>();
        Subject s = pendingSubjects.peek();
        for (ClassRoom r : classRooms) {
            if (!classRoomHashSet[s.getDay()][s.getTime()].contains(r)) {
                Table t = (Table) clone();
                Subject subject = (Subject) s.clone();
                t.classRoomHashSet[s.getDay()][s.getTime()].add(r);
                Exam e = new Exam(r, subject);
                //TODO check classroom constrains (same floor, size , etc)
                t.currentExam = e;
                ret.add(t);
            }
        }
        return ret;
    }

    private ArrayList<Table> selectTeacher(LinkedList<? extends Teacher> teachers) {
        ArrayList<Table> ret = new ArrayList<>();
        for (int i = 0; i < teachers.size(); i++) {
            Teacher teacher = teachers.get(i);
            if (!this.checkConDay(teacher)) {
                System.out.println("con day");
                continue;
            }
            if (watchesCount[teacher.id] + 1 <= teacher.getCntMax())
                if (!hashSet[currentExam.getSubject().getDay()][currentExam.getSubject().getTime()].contains(teacher))
                    if (teacher.getConstrain().isAvailableAtDay(currentExam.getSubject().getDay())) {
                        if (teacher.getConstrain().isAvailableAtTime(currentExam.getSubject().getTime())) {
                            teachers.remove(teacher);
                            Table t = (Table) clone();
                            if (teacher instanceof TeacherAssistant) {
                                LinkedList<TeacherAssistant> linkedList = (LinkedList<TeacherAssistant>) teachers;
                                linkedList.add((TeacherAssistant) teacher);
                            } else {
                                LinkedList<Teacher> linkedList = (LinkedList<Teacher>) teachers;
                                linkedList.add(teacher);
                            }
                            t.addW(teacher);
                            if (teacher.getConstrain().isPreferTime(currentExam.getSubject().getTime())) {
                                t.g = 1;
                            } else {
                                currentExam.addConstrainBreak(teacher.getName() + " dose not prefer time " + currentExam.getSubject().getTime());
                                t.g = 2;
                            }
                            if (!teacher.getConstrain().isPreferDay(currentExam.getSubject().getDay())) {
                                t.currentExam.addConstrainBreak(teacher.getName() + "dose not prefer day " + currentExam.getSubject().getDay());
                                t.g += 2;
                            }
                            t.g += t.checkWatchesCount(teacher);
                            ret.add(t);
                        }
                    }
        }
        return ret;
    }

    private int checkWatchesCount(Watcher w) {
        if (w instanceof Employee) {
            int cnt = 0;
            for (int j = 1; j <= 3; j++)
                if (hashSet[currentExam.getSubject().getDay()][j].contains(w))
                    cnt++;
            if (cnt >= 1) {
                currentExam.addConstrainBreak("Employee " + w.getName() +
                        " has more than 1 watch at day" + currentExam.getSubject().getDay());
                return 1;
            }
            return 0;
        } else {
            int cnt = 0;
            for (int j = 1; j <= 3; j++)
                if (hashSet[currentExam.getSubject().getDay()][j].contains(w))
                    cnt++;
            if (cnt >= 2) {
                if (w.getConstrain().getCntDay() < cnt) {
                    currentExam.addConstrainBreak("watcher " + w.getName() +
                            " has more than 2 watch at day" + currentExam.getSubject().getDay());
                    return 1;
                }
            }
            return 0;
        }
    }

    private ArrayList<Table> selectEmployee() {
        ArrayList<Table> ret = new ArrayList<>();
        for (int i = 0; i < pendingEmployees.size(); i++) {
            Employee emp = pendingEmployees.get(i);
            if (watchesCount[emp.id] + 1 <= emp.getCntMax()) {
                pendingEmployees.remove(i);
                Table t = (Table) clone();
                pendingEmployees.add(i, emp);
                t.addW(emp);
                t.g = 1;
                t.g += t.checkWatchesCount(emp);

                ret.add(t);
            }
        }
        return ret;
    }

    private ArrayList<Table> selectStudent() {
        ArrayList<Table> ret = new ArrayList<>();
        for (int i = 0; i < pendingStudents.size(); i++) {
            MasterStudent student = pendingStudents.get(i);
            if (!this.checkConDay(student))
                continue;

            if (watchesCount[student.getId()] <= student.getCntMax()) {
                if (!hashSet[currentExam.getSubject().getDay()][currentExam.getSubject().getTime()].contains(student)) {
                    if (student.getConstrain().isAvailableAtDay(currentExam.getSubject().getDay())) {
                        if (student.getConstrain().isAvailableAtTime(currentExam.getSubject().getTime())) {
                            pendingStudents.remove(i);
                            Table t = (Table) clone();
                            pendingStudents.add(i, student);

                            t.addW(student);
                            if (student.getConstrain().isPreferTime(currentExam.getSubject().getTime())) {
                                t.g = 1;
                            } else {
                                currentExam.addConstrainBreak(student.getName() + " dose not prefer time " + currentExam.getSubject().getTime());
                                t.g = 2;
                            }
                            if (!student.getConstrain().isPreferDay(currentExam.getSubject().getDay())) {
                                t.currentExam.addConstrainBreak(student.getName() + "dose not prefer day " + currentExam.getSubject().getDay());
                                t.g += 2;
                            }
                            t.g += t.checkWatchesCount(student);
                            ret.add(t);
                        }
                    }
                }
            }
        }
        return ret;
    }

    private ArrayList<Table> selectWatcher() {
        ArrayList<Table> ret = new ArrayList<>();
        if (currentExam.getWatcherNeed() <= currentExam.getWatchers().size()) {
            return ret;
        }
        System.out.println(currentExam.getNextWatcher());
        switch (currentExam.getNextWatcher()) {
            case "Teacher": {
                if (pendingTeachers.isEmpty())
                    pendingTeachers.addAll(Main.teachers);
                if (pendingTeachersAssistant.isEmpty())
                    pendingTeachersAssistant.addAll(Main.teacherAssistants);
                ret.addAll(selectTeacher(pendingTeachers));

                //ret.addAll(selectTeacher(pendingTeachersAssistant));
                break;
            }
            case "Employee": {
                if (pendingEmployees.isEmpty())
                    pendingEmployees.addAll(Main.employees);
                ret.addAll(selectEmployee());
                break;
            }
            case "Watcher": {
                if (pendingStudents.isEmpty())
                    pendingStudents.addAll(Main.students);
                ArrayList<Table> tables = selectStudent();
                if (tables.isEmpty()) {
                    if (pendingEmployees.isEmpty())
                        pendingEmployees.addAll(Main.employees);
                    tables = selectEmployee();
                    for (Table t : tables) {
                        t.currentExam.addConstrainBreak("Add an employee rather than a student");
                        t.g += 2;
                        ret.add(t);
                    }
                } else
                    ret.addAll(tables);
                break;
            }

        }
        return ret;
    }

    private ArrayList<Table> generateNext(ArrayList<ClassRoom> classRooms) {
        if (currentExam == null) {
            ArrayList<Table> tables = selectClassRoom(classRooms);
            return tables;
        }
        ArrayList<Table> tables = selectWatcher();
        System.out.println(tables.size());
        return tables;
    }

    public boolean isFinal() {
        if (!pendingSubjects.isEmpty()) {
            if (currentExam != null) {
                if (currentExam.isValid()) {
                    list.add(currentExam);
                /*
                //possible bug be careful
                for (Watcher w: currentExam.getWatchers()) {
                    if(w instanceof TeacherAssistant)
                        pendingTeachersAssistant.remove(w);
                    else
                        if(w instanceof Teacher)
                            pendingTeachers.remove(w);
                    if(w instanceof MasterStudent)
                        pendingStudents.remove(w);
                    if(w instanceof Employee)
                        pendingEmployees.remove(w);
                }
                 */
                    Subject s = pendingSubjects.peek();
                    s.setStudentsCnt(Math.min(s.getStudentsCnt() - currentExam.getClassRoom().getCap(), 0));
                    if (s.getStudentsCnt() == 0)
                        pendingSubjects.poll();
                    currentExam = null;
                }
                return pendingSubjects.isEmpty();
            }
        }
        return pendingSubjects.isEmpty();
    }

    //(deep/shallow) cloning
    @Override
    protected Object clone() {
        Table t = new Table();
        t.list = (ArrayList<Exam>) t.list.clone();
        t.pendingTeachers = (LinkedList<Teacher>) pendingTeachers.clone();
        t.pendingEmployees = (LinkedList<Employee>) pendingEmployees.clone();
        t.pendingStudents = (LinkedList<MasterStudent>) pendingStudents.clone();
        t.pendingTeachersAssistant = (LinkedList<TeacherAssistant>) pendingTeachersAssistant.clone();
        t.pendingSubjects = (LinkedList<Subject>) pendingSubjects.clone();
        for (int i = 0; i < t.classRoomHashSet.length; i++)
            for (int j = 0; j < t.classRoomHashSet[i].length; j++)
                t.classRoomHashSet[i][j] = (HashSet<ClassRoom>) t.classRoomHashSet[i][j].clone();

        for (int i = 0; i < t.hashSet.length; i++)
            for (int j = 0; j < t.hashSet[i].length; j++)
                t.hashSet[i][j] = (HashSet<Watcher>) t.hashSet[i][j].clone();

        t.g = g;
        if (t.currentExam != null) {
            System.out.println("cloned");
            t.currentExam = (Exam) currentExam.clone();
        }
        else
            t.currentExam = null;
        watchesCount = Arrays.copyOf(watchesCount, watchesCount.length);
        return t;
    }

    @Override
    public String toString() {
        return "Table{" +
                "list=" + list +
                '}';
    }

    public ArrayList<Exam> getList() {
        return list;
    }

    public HashSet<Watcher>[][] getHashSet() {
        return hashSet;
    }

    public HashSet<ClassRoom>[][] getClassRoomHashSet() {
        return classRoomHashSet;
    }

    public LinkedList<Subject> getPendingSubjects() {
        return pendingSubjects;
    }

    public LinkedList<Teacher> getPendingTeachers() {
        return pendingTeachers;
    }

    public LinkedList<TeacherAssistant> getPendingTeachersAssistant() {
        return pendingTeachersAssistant;
    }

    public LinkedList<Employee> getPendingEmployees() {
        return pendingEmployees;
    }

    public LinkedList<MasterStudent> getPendingStudents() {
        return pendingStudents;
    }

    public int[] getWatchesCount() {
        return watchesCount;
    }

    public int getG() {
        return g;
    }

    //uniform search
    public static Table solve() {
        PriorityQueue<PqPair<Table>> pq = new PriorityQueue<>();
        Table table = new Table();
        table.pendingSubjects.add(Main.subjects.get(0));
        pq.add(new PqPair<>(0, table));

        HashMap<Table, Integer> mp = new HashMap<>();
        mp.put(table, 0);

        while (!pq.isEmpty()) {

            PqPair<Table> p = pq.poll();
            Integer cost = p.first;
            Table t = p.second;

            if (t.isFinal()) {
                //TODO printing Table content and constrains break
                return t;
            }

            if (mp.containsKey(t))
                if (cost > mp.get(t))
                    continue;

            ArrayList<Table> list = t.generateNext(Main.ClassRooms);

            for (Table child : list) {
                if (mp.containsKey(child)) {
                    int oldCost = mp.get(child);
                    if (oldCost > cost + child.g) {
                        mp.put(child, cost + child.g);
                        pq.add(new PqPair<>(child.g, child));
                    }
                } else {
                    mp.put(child, child.g + cost);
                    pq.add(new PqPair<>(child.g + cost, child));
                }
            }
        }
        return null;
    }

    public static void AStar() {
        PriorityQueue<PqPair<PqPair<Table>>> pq = new PriorityQueue<>();
        Table table = new Table();
        table.pendingSubjects.addAll(Main.subjects);
        pq.add(new PqPair<>(0, new PqPair<>(0, table)));

        HashMap<Table, Integer> mp = new HashMap<>();
        mp.put(table, 0);
        HashMap<Table, Integer> gmp = new HashMap<>();
        mp.put(table, 0);
        while (!pq.isEmpty()) {

            PqPair<PqPair<Table>> p = pq.poll();
            Integer fCost = p.first;
            Integer gCost = p.second.first;
            Table t = p.second.second;


            if (t.isFinal()) {
                //TODO printing Table content and constrains break
                return;
            }

            if (mp.containsKey(t))
                if (fCost > mp.get(t))
                    continue;

            ArrayList<Table> list = t.generateNext(Main.ClassRooms);

            for (Table child : list) {
                if (mp.containsKey(child)) {
                    int oldCost = mp.get(child);
                    int newCost = fCost + child.g + child.h1();
                    if (oldCost > newCost) {
                        mp.put(child, newCost);
                        mp.put(child, child.g + gCost);
                        pq.add(new PqPair<>(newCost, new PqPair<>(gCost + child.g, child)));
                    }
                } else {
                    int f = fCost + child.g + child.h1();
                    mp.put(child, f);
                    mp.put(child, gCost + child.g);
                    pq.add(new PqPair<>(f, new PqPair<>(gCost + child.g, child)));
                }
            }
        }
    }
}
