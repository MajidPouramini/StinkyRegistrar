package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
		for (CSE o : courses) {
            if (checkPassed(o.getCourse(), transcript)) {
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
            }

            checkExamTimeCollision(courses, o);
            checkRepeatedRequest(courses, o);
        }
		for (CSE o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            for (Course pre : prereqs) {
                if (!checkPassed(pre, transcript)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
                }
            }
        }
        checkGPA(courses, transcript);
        for (CSE o : courses)
			s.takeCourse(o.getCourse(), o.getSection());
	}

	private boolean checkPassed(Course course, Map<Term, Map<Course, Double>> transcript) {
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                if (r.getKey().equals(course) && r.getValue() >= 10) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkRepeatedRequest(List<CSE> courses, CSE o) throws EnrollmentRulesViolationException {
        for (CSE o2 : courses) {
            if (o == o2)
                continue;
            if (o.getCourse().equals(o2.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
        }
    }

    private void checkExamTimeCollision(List<CSE> courses, CSE o) throws EnrollmentRulesViolationException {
        for (CSE o2 : courses) {
            if (o == o2)
                continue;
            if (o.getExamTime().equals(o2.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
        }
    }

    private void checkGPA(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        int unitsRequested = 0;
        for (CSE o : courses)
            unitsRequested += o.getCourse().getUnits();
        double points = 0;
        int totalUnits = 0;
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                points += r.getValue() * r.getKey().getUnits();
                totalUnits += r.getKey().getUnits();
            }
		}
        double gpa = points / totalUnits;
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
    }
}
