import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;


public class Exam implements Comparable<Exam>, Cloneable, Serializable {
    private Subject subject;
    private ClassRoom classRoom;
    private Teacher head;
    private Employee secretary;
    private HashSet<Watcher> watchers;
    private ArrayList<String> constrainBreak;
    private LinkedList<String> watchersTypes;

    private Exam() {
    }

    public Exam(ClassRoom classRoom, Subject subject) {
        this.classRoom = classRoom;
        this.subject = subject;
        watchers = new HashSet<>();
        constrainBreak = new ArrayList<>();
        watchersTypes = new LinkedList<>();
        switch (classRoom.getCap()) {
            case 30: {
                watchersTypes.add("Teacher");
                watchersTypes.add("Employee");
                watchersTypes.add("Watcher");
                break;
            }
            case 50: {
                watchersTypes.add("Teacher");
                watchersTypes.add("Employee");
                watchersTypes.add("Watcher");
                watchersTypes.add("Watcher");
                break;
            }
            case 70: {
                watchersTypes.add("Teacher");
                watchersTypes.add("Employee");
                watchersTypes.add("Watcher");
                watchersTypes.add("Watcher");
                watchersTypes.add("Watcher");
                watchersTypes.add("Watcher");
                break;
            }
        }

    }

    public int getWatcherNeed() {
        return watchersTypes.size();
    }

    public String getNextWatcher() {
        if (watchersTypes.size() == watchers.size()) {
            System.out.println("full");
            return null;
        }
        return watchersTypes.get(watchers.size());
    }

    public void addConstrainBreak(String s) {
        constrainBreak.add(s);
    }

    public void addWatcher(Watcher w){
        if (w instanceof Teacher)
            if (head == null)
                head = (Teacher) w;
        if (w instanceof Employee)
            if (secretary == null)
                secretary = (Employee) w;
        watchers.add(w);
    }

    public Teacher getHead() {
        return head;
    }

    public Employee getSecretary() {
        return secretary;
    }

    public HashSet<Watcher> getWatchers() {
        return watchers;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public int compareTo(Exam o) {
        return subject.compareTo(o.subject);
    }

    @Override
    protected Object clone() {
        Exam exam = new Exam();
        exam.classRoom = (ClassRoom) classRoom.clone();
        exam.subject = (Subject) subject.clone();
        exam.constrainBreak = (ArrayList<String>) constrainBreak.clone();
        if (head != null)
            exam.head = (Teacher) head.clone();
        if (secretary != null)
            exam.secretary = (Employee) secretary.clone();
        exam.watchers = (HashSet<Watcher>) watchers.clone();
        exam.watchersTypes = (LinkedList<String>) watchersTypes.clone();
        return exam;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "subject=" + subject +
                ", classRoom=" + classRoom +
                ", watchers=" + watchers +
                ", constrainBreak=" + constrainBreak +
                '}';
    }

    public boolean isValid() {
        return watchersTypes.size() == watchers.size();
        /*
        LinkedList<String> list = new LinkedList<>();
        Collections.copy(list, watchersTypes);
        for (Watcher w : watchers) {
            if (list.contains(w.getType()))
                list.removeFirstOccurrence(w.getType());
            else if (w.getType().equals("Employee"))
                if (list.contains("Watcher"))
                    list.removeFirstOccurrence("Watcher");
                else
                    return false;
            else
                return false;
        }
        return true;

         */
    }
}
