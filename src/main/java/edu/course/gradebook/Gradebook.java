package edu.course.gradebook;

import java.util.*;

public class Gradebook {

    private final Map<String, List<Integer>> gradesByStudent = new HashMap<>();
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();
    private final LinkedList<String> activityLog = new LinkedList<>();

    public Optional<List<Integer>> findStudentGrades(String name) {
        return Optional.ofNullable(gradesByStudent.get(name));
    }

    public boolean addStudent(String name) {
        if (gradesByStudent.containsKey(name)) {
            return false;
        }

        gradesByStudent.put(name, new ArrayList<>());

        activityLog.add("Added student " + name);
        return true;
    }

    public boolean addGrade(String name, int grade) {
        var grades = findStudentGrades(name);

        if (grades.isEmpty()) {
            return false;
        }
        grades.get().add(grade);

        undoStack.push(gradebook -> {
            var list  = gradebook.gradesByStudent.get(name);
            if (list != null && !list.isEmpty()) {
                list.remove(list.size() - 1);
            }
        });

        activityLog.add("Added grade " + grade + " for " + name);
        return true;
    }

    public boolean removeStudent(String name) {
        var grades = findStudentGrades(name);

        if (grades.isEmpty()) {
            return false;
        }

        var gradesCopy = new ArrayList<>(grades.get());

        gradesByStudent.remove(name);

        undoStack.push(gradebook -> {
            gradebook.gradesByStudent.put(name, new ArrayList<>(gradesCopy));
        });

        activityLog.add("Removed student " + name);
        return true;
    }

    public Optional<Double> averageFor(String name) {
        var grades = findStudentGrades(name);
        if (grades.isEmpty()) {
            return Optional.empty();
        }

        if (grades.get().isEmpty()) {
            return Optional.empty();
        }

        double sum = 0;
        for (var grade : grades.get()) {
            sum += grade;
        }

        double avg = sum / grades.get().size();
        return Optional.of(avg);
    }

    public Optional<String> letterGradeFor(String name) {
        var avg = averageFor(name);
        if (avg.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                switch (avg.get().intValue()) {
                    case 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100 -> "A";
                    case 80, 81, 82, 83, 84, 85, 86, 87, 88, 89 -> "B";
                    case 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> "C";
                    case 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 -> "D";
                    default -> "F";
                }
        );
    }

    public Optional<Double> classAverage() {
        double sum = 0;
        int count = 0;

        for(var grades : gradesByStudent.values()){
            for(var grade : grades){
                sum += grade;
                count++;
            }
        }

        if (count == 0){
            return Optional.empty();
        }

        return Optional.of(sum/count);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }

        var actionToUndo = undoStack.pop();
        actionToUndo.undo(this);
        activityLog.add("Undo completed");

        return true;
    }

    public List<String> recentLog(int maxItems) {
        int size = activityLog.size();

        if (size == 0) {
            return Collections.emptyList();
        }

        int startIndex = Math.max(0, size - maxItems);
        return new ArrayList<>(activityLog.subList(startIndex, size));
    }
}
